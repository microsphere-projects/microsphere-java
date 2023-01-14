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
package io.github.microsphere.commons.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * Stop Watch
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

    private final List<Task> taskList = new LinkedList<>();

    private int currentTaskIndex = -1;

    /**
     * Total running time.
     */
    private long totalTimeNanos;

    public StopWatch(String id) {
        this.id = id;
    }

    public void start(String taskName) throws IllegalStateException {
        Task currentTask = getCurrentTask();

        if (currentTask != null && Objects.equals(taskName, currentTask.taskName)) {
            throw new IllegalStateException("Task[name : '" + taskName + "'] is already running");
        }

        currentTask = Task.start(taskName);

        taskList.add(currentTask);

        currentTaskIndex++;

    }

    public void stop() {
        Task currentTask = getCurrentTask();
        currentTask.stop();
        this.totalTimeNanos += currentTask.getElapsedNanos();
        currentTaskIndex--;
    }

    public Task getCurrentTask() {
        if (currentTaskIndex < 0) {
            return null;
        }
        List<Task> taskList = this.taskList;
        int taskCount = taskList.size();
        if (taskCount < 1) {
            return null;
        }

        return taskList.get(currentTaskIndex);
    }

    public long getTotalTime(TimeUnit timeUnit) {
        return TimeUnit.NANOSECONDS.convert(this.totalTimeNanos, timeUnit);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StopWatch.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("taskList=" + taskList)
                .add("totalTime(ns)=" + totalTimeNanos)
                .toString();
    }

    private static class Task {

        private final String taskName;

        private final long startTimeNanos;

        private long elapsedNanos;

        private Task(String taskName) {
            this.taskName = taskName;
            this.startTimeNanos = System.nanoTime();
        }

        public static Task start(String taskName) {
            return new Task(taskName);
        }

        public void stop() {
            this.elapsedNanos = System.nanoTime() - this.startTimeNanos;
        }

        public String getTaskName() {
            return taskName;
        }

        public long getStartTimeNanos() {
            return startTimeNanos;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
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
            return Objects.hash(taskName);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", "Task" + "[", "]")
                    .add("name='" + taskName + "'")
                    .add("elapsed(ns)=" + elapsedNanos)
                    .toString();
        }
    }
}
