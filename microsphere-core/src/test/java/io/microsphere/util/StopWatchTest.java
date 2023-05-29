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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * {@link StopWatch} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StopWatchTest {

    private static final Logger logger = LoggerFactory.getLogger(StopWatchTest.class);

    private StopWatch stopWatch;

    @Before
    public void init() {
        stopWatch = new StopWatch("test");
    }

    @Test
    public void test() throws InterruptedException {
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
        assertTrue(stopWatch.getTotalTime(TimeUnit.MILLISECONDS) > 0);
        logger.info(stopWatch.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartOnNullTaskName() {
        stopWatch.start(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartOnEmptyTaskName() {
        stopWatch.start("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartOnBlankTaskName() {
        stopWatch.start(" ");
    }

    @Test(expected = IllegalStateException.class)
    public void testStartOnAlreadyRunning() {
        stopWatch.start("1");
        stopWatch.start("1");
    }

    @Test
    public void testStartOnReentrant() {
        stopWatch.start("1", true);
        stopWatch.start("1");
    }

    @Test(expected = IllegalStateException.class)
    public void testStopOnNoTaskRunning() {
        stopWatch.stop();
    }

}
