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

import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.util.StopWatch.Task.start;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class StopWatchTest {

    private static final Logger logger = LoggerFactory.getLogger(StopWatchTest.class);

    private StopWatch stopWatch;

    @BeforeEach
    void setUp() {
        stopWatch = new StopWatch("test");
    }

    @Test
    void test() throws InterruptedException {
        stopWatch.start("1");
        Thread.sleep(100);
        stopWatch.start("2");
        Thread.sleep(10);
        stopWatch.stop();
        stopWatch.stop();
        StopWatch.Task currentTask = stopWatch.getCurrentTask();
        assertNull(currentTask);
        assertEquals("test", stopWatch.getId());
        assertEquals(0, stopWatch.getRunningTasks().size());
        assertEquals(2, stopWatch.getCompletedTasks().size());
        StopWatch.Task task = stopWatch.getCompletedTasks().get(1);
        assertEquals("1", task.getTaskName());
        assertFalse(task.isReentrant());
        assertTrue(task.getStartTimeNanos() > 0);
        assertTrue(task.getElapsedNanos() > 0);
        assertTrue(stopWatch.getTotalTimeNanos() > 0);
        assertTrue(stopWatch.getTotalTime(MILLISECONDS) > 0);
        logger.info(stopWatch.toString());
    }

    @Test
    void testTask() {
        String testName = "test";
        StopWatch.Task task = start(testName);
        task.stop();
        assertSame(testName, task.getTaskName());
        assertFalse(task.isReentrant());
        assertTrue(task.getStartTimeNanos() > 0);
        assertTrue(task.getElapsedNanos() > 0);
        assertEquals(task, start(testName));
        assertEquals(task.hashCode(), start(testName).hashCode());

    }


    @Test
    void testStartOnNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> stopWatch.start(null));
    }

    @Test
    void testStartOnEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> stopWatch.start(""));
    }

    @Test
    void testStartOnBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> stopWatch.start(SPACE));
    }

    @Test
    void testStartOnAlreadyRunning() {
        assertThrows(IllegalStateException.class, () -> {
            stopWatch.start("1");
            stopWatch.start("1");
        });
    }

    @Test
    void testStartOnReentrant() {
        stopWatch.start("1", true);
        stopWatch.start("1");
    }

    @Test
    void testStopOnNoTaskRunning() {
        assertThrows(IllegalStateException.class, stopWatch::stop);
    }
}
