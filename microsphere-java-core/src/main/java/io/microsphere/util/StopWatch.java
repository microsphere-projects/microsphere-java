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

import static io.microsphere.util.StringUtils.isBlank;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;

/**
 * Stop Watch supports the nest tasks, the default task can't be reentrant, unless {@link Task#isReentrant()} is true by
 * {@link #start(String, boolean)} method setting.
 * <p>
 * Note : {@link StopWatch} is not thread-safe
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
        return TimeUnit.NANOSECONDS.convert(this.totalTimeNanos, timeUnit);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StopWatch.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
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
            this.startTimeNanos = System.nanoTime();
        }

        public static Task start(String taskName) {
            return start(taskName, false);
        }

        public static Task start(String taskName, boolean reentrant) {
            return new Task(taskName, reentrant);
        }

        public void stop() {
            this.elapsedNanos = System.nanoTime() - this.startTimeNanos;
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
            return new StringJoiner(", ", "Task" + "[", "]").add("name='" + taskName + "'").add("elapsed(ns)=" + elapsedNanos).toString();
        }
    }
}
