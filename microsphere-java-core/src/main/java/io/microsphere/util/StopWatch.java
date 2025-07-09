/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static io.microsphere.constants.SymbolConstants.QUOTE;
import static io.microsphere.util.StringUtils.isBlank;
import static java.lang.System.nanoTime;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * <p>{@code StopWatch} provides a simple way to measure execution time for tasks, supporting nested task tracking.
 * Each task can be started with optional reentrancy control via {@link #start(String, boolean)}.</p>
 *
 * <p>By default, tasks are non-reentrant. Attempting to start a non-reentrant task while it's already running will
 * throw an {@link IllegalStateException}. If reentrancy is enabled, subsequent calls to start the same task will
 * be ignored until it is stopped.</p>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * // Basic usage
 * StopWatch stopWatch = new StopWatch("MyStopWatch");
 * stopWatch.start("Task 1");
 * // perform operations
 * stopWatch.stop();
 * System.out.println(stopWatch);  // Outputs: StopWatch[id='MyStopWatch', running tasks=[], completed tasks=[Task[name='Task 1', elapsed(ns)=...]], totalTime(ns)=...]
 *
 * // Nested tasks
 * stopWatch.start("Task A");
 * // do something
 * stopWatch.start("Task B");
 * // do something else
 * stopWatch.stop();  // stops Task B
 * stopWatch.stop();  // stops Task A
 *
 * // Reentrant task example
 * stopWatch.start("Reentrant Task", true);
 * // do something
 * stopWatch.start("Reentrant Task", true);  // this call is ignored
 * // ...
 * stopWatch.stop();  // ends the original task
 * }</pre>
 *
 * <p>Note: This class is not thread-safe and should only be used within a single thread.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StopWatch {

    /**
     * Identifier of this {@code StopWatch}.
     * <p>Handy when we have output from multiple stop watches and need to
     * distinguish between them in log or console output.
     */
    private final String id;

    /**
     * Running tasks(FIFO)
     */
    private final List<Task> runningTasks = new LinkedList<>();

    /**
     * Completed tasks(FILO)
     */
    private final List<Task> completedTasks = new LinkedList<>();

    /**
     * Total running time.
     */
    private long totalTimeNanos;

    public StopWatch(String id) {
        this.id = id;
    }

    public void start(String taskName) throws IllegalArgumentException, IllegalStateException {
        start(taskName, false);
    }

    public void start(String taskName, boolean reentrant) throws IllegalArgumentException, IllegalStateException {
        if (isBlank(taskName)) {
            throw new IllegalArgumentException("The 'taskName' argument must not be blank");
        }

        Task newTask = Task.start(taskName, reentrant);

        List<Task> runningTasks = this.runningTasks;

        int taskIndex = runningTasks.indexOf(newTask);

        if (taskIndex > -1) {
            Task oldTask = runningTasks.get(taskIndex);
            if (oldTask.reentrant) {
                return;
            } else {
                throw new IllegalStateException("StopWatch[id : '" + id + "']'s Task[name : '" + taskName + "' , number=" + (taskIndex + 1) + "] is already running");
            }
        }

        runningTasks.add(newTask);
    }

    public void stop() throws IllegalStateException {
        Task currentTask = getCurrentTask(true);
        if (currentTask == null) {
            throw new IllegalStateException("No task is running");
        }
        currentTask.stop();
        this.totalTimeNanos += currentTask.elapsedNanos;
        this.completedTasks.add(currentTask);
    }

    public Task getCurrentTask() {
        return getCurrentTask(false);
    }

    protected Task getCurrentTask(boolean removed) {
        List<Task> runningTasks = this.runningTasks;
        int size = runningTasks.size();
        if (size == 0) {
            return null;
        }
        int currentTaskIndex = size - 1;
        return removed ? runningTasks.remove(currentTaskIndex) : runningTasks.get(currentTaskIndex);
    }

    public String getId() {
        return this.id;
    }

    public List<Task> getRunningTasks() {
        return unmodifiableList(this.runningTasks);
    }

    public List<Task> getCompletedTasks() {
        return unmodifiableList(this.completedTasks);
    }

    public long getTotalTimeNanos() {
        return this.totalTimeNanos;
    }

    public long getTotalTime(TimeUnit timeUnit) {
        return NANOSECONDS.convert(this.totalTimeNanos, timeUnit);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StopWatch.class.getSimpleName() + "[", "]")
                .add("id='" + id + QUOTE)
                .add("running tasks=" + runningTasks)
                .add("completed tasks=" + completedTasks)
                .add("totalTime(ns)=" + totalTimeNanos)
                .toString();
    }

    public static class Task {

        private final String taskName;

        /**
         * It indicates the task can be reentrant or not
         */
        private final boolean reentrant;

        private final long startTimeNanos;

        private long elapsedNanos;

        private Task(String taskName, boolean reentrant) {
            this.taskName = taskName;
            this.reentrant = reentrant;
            this.startTimeNanos = nanoTime();
        }

        public static Task start(String taskName) {
            return start(taskName, false);
        }

        public static Task start(String taskName, boolean reentrant) {
            return new Task(taskName, reentrant);
        }

        public void stop() {
            this.elapsedNanos = nanoTime() - this.startTimeNanos;
        }

        public String getTaskName() {
            return this.taskName;
        }

        public boolean isReentrant() {
            return this.reentrant;
        }

        public long getStartTimeNanos() {
            return this.startTimeNanos;
        }

        public long getElapsedNanos() {
            return this.elapsedNanos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return Objects.equals(taskName, task.taskName);
        }

        @Override
        public int hashCode() {
            return hash(taskName);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", "Task" + "[", "]").add("name='" + taskName + QUOTE).add("elapsed(ns)=" + elapsedNanos).toString();
        }
    }
}
