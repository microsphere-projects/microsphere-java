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

import io.microsphere.Loggable;
import io.microsphere.util.StopWatch.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.util.StopWatch.Task.start;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * {@link StopWatch} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class StopWatchTest implements Loggable {

    private static final String testName = "test";


    private StopWatch stopWatch;

    @BeforeEach
    void setUp() {
        this.stopWatch = new StopWatch(testName);
    }

    @Test
    void test() throws InterruptedException {
        this.stopWatch.start("1");
        sleep(100);
        this.stopWatch.start("2");
        sleep(10);
        this.stopWatch.stop();
        this.stopWatch.stop();
        Task currentTask = this.stopWatch.getCurrentTask();
        assertNull(currentTask);
        assertEquals(testName, this.stopWatch.getId());
        assertEquals(0, this.stopWatch.getRunningTasks().size());
        assertEquals(2, this.stopWatch.getCompletedTasks().size());
        Task task = this.stopWatch.getCompletedTasks().get(1);
        assertEquals("1", task.getTaskName());
        assertFalse(task.isReentrant());
        assertTrue(task.getStartTimeNanos() > 0);
        assertTrue(task.getElapsedNanos() > 0);
        assertTrue(this.stopWatch.getTotalTimeNanos() > 0);
        assertTrue(this.stopWatch.getTotalTime(MILLISECONDS) > 0);
        log(this.stopWatch.toString());
    }

    @Test
    void testTask() {
        Task task = start(testName);
        task.stop();
        assertSame(testName, task.getTaskName());
        assertFalse(task.isReentrant());
        assertTrue(task.getStartTimeNanos() > 0);
        assertTrue(task.getElapsedNanos() > 0);
    }

    @Test
    void testTaskOnEquals() {
        Task task = start(testName);
        assertEquals(task, task);
        assertEquals(task, start(testName));
    }

    @Test
    void testTaskOnNotEquals() {
        Task task = start(testName);
        assertNotEquals(task, testName);
    }

    @Test
    void testTaskHashCode() {
        Task task = start(testName);
        assertEquals(task.hashCode(), start(testName).hashCode());
    }

    @Test
    void testStartOnNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> this.stopWatch.start(null));
    }

    @Test
    void testStartOnEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> this.stopWatch.start(""));
    }

    @Test
    void testStartOnBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> this.stopWatch.start(SPACE));
    }

    @Test
    void testStartOnAlreadyRunning() {
        assertThrows(IllegalStateException.class, () -> {
            this.stopWatch.start("1");
            this.stopWatch.start("1");
        });
    }

    @Test
    void testStartOnReentrant() {
        this.stopWatch.start("1", true);
        this.stopWatch.start("1");
    }

    @Test
    void testStopOnNoTaskRunning() {
        assertThrows(IllegalStateException.class, this.stopWatch::stop);
    }

    @Test
    void testGetCurrentTask() {
        this.stopWatch.start("1", true);
        Task currentTask = this.stopWatch.getCurrentTask();
        assertEquals("1", currentTask.getTaskName());

        currentTask = this.stopWatch.getCurrentTask(true);
        assertEquals("1", currentTask.getTaskName());

        assertNull(this.stopWatch.getCurrentTask());
        assertNull(this.stopWatch.getCurrentTask(true));
    }
}
