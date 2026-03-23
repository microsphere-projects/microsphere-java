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

    /**
     * Constructs a new {@code StopWatch} with the given identifier.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("serviceTimer");
     * }</pre>
     *
     * @param id the identifier for this stop watch, used to distinguish output from multiple stop watches
     * @since 1.0.0
     */
    public StopWatch(String id) {
        this.id = id;
    }

    /**
     * Starts a new non-reentrant task with the given name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("databaseQuery");
     *   // perform the database query
     *   stopWatch.stop();
     * }</pre>
     *
     * @param taskName the name of the task to start; must not be blank
     * @throws IllegalArgumentException if {@code taskName} is blank
     * @throws IllegalStateException if a non-reentrant task with the same name is already running
     * @since 1.0.0
     */
    public void start(String taskName) throws IllegalArgumentException, IllegalStateException {
        start(taskName, false);
    }

    /**
     * Starts a new task with the given name and reentrancy control.
     * <p>If {@code reentrant} is {@code true} and a task with the same name is already running,
     * the call is silently ignored. If {@code reentrant} is {@code false} and the task is already
     * running, an {@link IllegalStateException} is thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("cacheRefresh", true);
     *   // nested call is safely ignored because the task is reentrant
     *   stopWatch.start("cacheRefresh", true);
     *   stopWatch.stop();
     * }</pre>
     *
     * @param taskName  the name of the task to start; must not be blank
     * @param reentrant {@code true} to allow reentrant invocations of the same task name,
     *                  {@code false} to throw an exception on duplicate starts
     * @throws IllegalArgumentException if {@code taskName} is blank
     * @throws IllegalStateException if a non-reentrant task with the same name is already running
     * @since 1.0.0
     */
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

    /**
     * Stops the most recently started (current) running task, records its elapsed time,
     * and moves it to the completed tasks list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("httpRequest");
     *   // perform the HTTP request
     *   stopWatch.stop();
     *   long elapsed = stopWatch.getTotalTimeNanos();
     * }</pre>
     *
     * @throws IllegalStateException if no task is currently running
     * @since 1.0.0
     */
    public void stop() throws IllegalStateException {
        Task currentTask = getCurrentTask(true);
        if (currentTask == null) {
            throw new IllegalStateException("No task is running");
        }
        currentTask.stop();
        this.totalTimeNanos += currentTask.elapsedNanos;
        this.completedTasks.add(currentTask);
    }

    /**
     * Returns the most recently started running task without removing it from the running list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("indexing");
     *   Task current = stopWatch.getCurrentTask();
     *   System.out.println(current.getTaskName()); // "indexing"
     * }</pre>
     *
     * @return the current running {@link Task}, or {@code null} if no task is running
     * @since 1.0.0
     */
    public Task getCurrentTask() {
        return getCurrentTask(false);
    }

    /**
     * Returns the most recently started running task, optionally removing it from the running list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("processing");
     *   // retrieve and remove the current task from the running list
     *   Task task = stopWatch.getCurrentTask(true);
     *   System.out.println(task.getTaskName()); // "processing"
     * }</pre>
     *
     * @param removed {@code true} to remove the task from the running list, {@code false} to only peek
     * @return the current running {@link Task}, or {@code null} if no task is running
     * @since 1.0.0
     */
    protected Task getCurrentTask(boolean removed) {
        List<Task> runningTasks = this.runningTasks;
        int size = runningTasks.size();
        if (size == 0) {
            return null;
        }
        int currentTaskIndex = size - 1;
        return removed ? runningTasks.remove(currentTaskIndex) : runningTasks.get(currentTaskIndex);
    }

    /**
     * Returns the identifier of this stop watch.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("orderService");
     *   String id = stopWatch.getId(); // "orderService"
     * }</pre>
     *
     * @return the stop watch identifier
     * @since 1.0.0
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns an unmodifiable list of tasks that are currently running.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("taskA");
     *   stopWatch.start("taskB");
     *   List<Task> running = stopWatch.getRunningTasks();
     *   System.out.println(running.size()); // 2
     * }</pre>
     *
     * @return an unmodifiable list of currently running {@link Task} instances
     * @since 1.0.0
     */
    public List<Task> getRunningTasks() {
        return unmodifiableList(this.runningTasks);
    }

    /**
     * Returns an unmodifiable list of tasks that have been completed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("init");
     *   stopWatch.stop();
     *   List<Task> completed = stopWatch.getCompletedTasks();
     *   System.out.println(completed.get(0).getTaskName()); // "init"
     * }</pre>
     *
     * @return an unmodifiable list of completed {@link Task} instances
     * @since 1.0.0
     */
    public List<Task> getCompletedTasks() {
        return unmodifiableList(this.completedTasks);
    }

    /**
     * Returns the total elapsed time of all completed tasks in nanoseconds.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("compute");
     *   stopWatch.stop();
     *   long nanos = stopWatch.getTotalTimeNanos();
     *   System.out.println("Total time: " + nanos + " ns");
     * }</pre>
     *
     * @return the total elapsed time in nanoseconds
     * @since 1.0.0
     */
    public long getTotalTimeNanos() {
        return this.totalTimeNanos;
    }

    /**
     * Returns the total elapsed time of all completed tasks, converted to the specified time unit.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("fileIO");
     *   stopWatch.stop();
     *   long millis = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);
     *   System.out.println("Total time: " + millis + " ms");
     * }</pre>
     *
     * @param timeUnit the desired time unit for the result
     * @return the total elapsed time in the specified time unit
     * @since 1.0.0
     */
    public long getTotalTime(TimeUnit timeUnit) {
        return NANOSECONDS.convert(this.totalTimeNanos, timeUnit);
    }

    /**
     * Returns a string representation of this stop watch, including its identifier,
     * running tasks, completed tasks, and total elapsed time in nanoseconds.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch stopWatch = new StopWatch("myWatch");
     *   stopWatch.start("task1");
     *   stopWatch.stop();
     *   System.out.println(stopWatch.toString());
     *   // StopWatch[id='myWatch', running tasks=[], completed tasks=[Task[name='task1', elapsed(ns)=...]], totalTime(ns)=...]
     * }</pre>
     *
     * @return a descriptive string representation of this stop watch
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", StopWatch.class.getSimpleName() + "[", "]")
                .add("id='" + id + QUOTE)
                .add("running tasks=" + runningTasks)
                .add("completed tasks=" + completedTasks)
                .add("totalTime(ns)=" + totalTimeNanos)
                .toString();
    }

    /**
     * Represents a named task tracked by a {@link StopWatch}. Each task records its start time
     * in nanoseconds and calculates the elapsed time when {@link #stop()} is called.
     * <p>Tasks are compared by their {@code taskName} only, making task names unique within a
     * single stop watch's running task list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   StopWatch.Task task = StopWatch.Task.start("parsing");
     *   // perform parsing work
     *   task.stop();
     *   System.out.println(task.getTaskName());    // "parsing"
     *   System.out.println(task.getElapsedNanos()); // elapsed time in nanoseconds
     * }</pre>
     *
     * @since 1.0.0
     */
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

        /**
         * Creates and starts a new non-reentrant task with the given name.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("validation");
         *   // perform validation
         *   task.stop();
         * }</pre>
         *
         * @param taskName the name of the task
         * @return a new running {@link Task} instance
         * @since 1.0.0
         */
        public static Task start(String taskName) {
            return start(taskName, false);
        }

        /**
         * Creates and starts a new task with the given name and reentrancy setting.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("retryableOp", true);
         *   // perform the operation
         *   task.stop();
         *   System.out.println(task.isReentrant()); // true
         * }</pre>
         *
         * @param taskName  the name of the task
         * @param reentrant {@code true} if the task may be started again while running
         * @return a new running {@link Task} instance
         * @since 1.0.0
         */
        public static Task start(String taskName, boolean reentrant) {
            return new Task(taskName, reentrant);
        }

        /**
         * Stops this task and records the elapsed time since it was started.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("encoding");
         *   // perform encoding
         *   task.stop();
         *   System.out.println("Elapsed: " + task.getElapsedNanos() + " ns");
         * }</pre>
         *
         * @since 1.0.0
         */
        public void stop() {
            this.elapsedNanos = nanoTime() - this.startTimeNanos;
        }

        /**
         * Returns the name of this task.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("sorting");
         *   String name = task.getTaskName(); // "sorting"
         * }</pre>
         *
         * @return the task name
         * @since 1.0.0
         */
        public String getTaskName() {
            return this.taskName;
        }

        /**
         * Returns whether this task allows reentrant starts.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("retry", true);
         *   boolean reentrant = task.isReentrant(); // true
         * }</pre>
         *
         * @return {@code true} if the task is reentrant, {@code false} otherwise
         * @since 1.0.0
         */
        public boolean isReentrant() {
            return this.reentrant;
        }

        /**
         * Returns the start time of this task in nanoseconds, as reported by {@link System#nanoTime()}.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("lookup");
         *   long startNanos = task.getStartTimeNanos();
         * }</pre>
         *
         * @return the start time in nanoseconds
         * @since 1.0.0
         */
        public long getStartTimeNanos() {
            return this.startTimeNanos;
        }

        /**
         * Returns the elapsed time of this task in nanoseconds.
         * This value is zero until {@link #stop()} has been called.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("render");
         *   // perform rendering
         *   task.stop();
         *   long elapsed = task.getElapsedNanos();
         *   System.out.println("Render took " + elapsed + " ns");
         * }</pre>
         *
         * @return the elapsed time in nanoseconds, or {@code 0} if the task has not been stopped
         * @since 1.0.0
         */
        public long getElapsedNanos() {
            return this.elapsedNanos;
        }

        /**
         * Compares this task with the specified object for equality.
         * Two tasks are considered equal if they have the same {@code taskName}.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task1 = StopWatch.Task.start("compile");
         *   StopWatch.Task task2 = StopWatch.Task.start("compile");
         *   boolean equal = task1.equals(task2); // true
         * }</pre>
         *
         * @param o the object to compare with
         * @return {@code true} if the given object is a {@link Task} with the same name
         * @since 1.0.0
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Task)) {
                return false;
            }
            Task task = (Task) o;
            return Objects.equals(taskName, task.taskName);
        }

        /**
         * Returns a hash code for this task, based on the task name.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("hash-demo");
         *   int code = task.hashCode();
         * }</pre>
         *
         * @return the hash code of this task
         * @since 1.0.0
         */
        @Override
        public int hashCode() {
            return hash(taskName);
        }

        /**
         * Returns a string representation of this task, including the task name and elapsed time in nanoseconds.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   StopWatch.Task task = StopWatch.Task.start("transform");
         *   task.stop();
         *   System.out.println(task.toString());
         *   // Task[name='transform', elapsed(ns)=...]
         * }</pre>
         *
         * @return a descriptive string representation of this task
         * @since 1.0.0
         */
        @Override
        public String toString() {
            return new StringJoiner(", ", "Task" + "[", "]").add("name='" + taskName + QUOTE).add("elapsed(ns)=" + elapsedNanos).toString();
        }
    }
}
