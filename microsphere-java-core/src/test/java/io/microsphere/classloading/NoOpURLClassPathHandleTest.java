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
package io.microsphere.classloading;

import org.junit.jupiter.api.Test;

import static io.microsphere.lang.Prioritized.MIN_PRIORITY;
import static io.microsphere.net.URLUtils.EMPTY_URL_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NoOpURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
public class NoOpURLClassPathHandleTest extends BaseURLClassPathHandleTest {

    @Override
    protected URLClassPathHandle createHandle() {
        return new NoOpURLClassPathHandle();
    }

    @Override
    @Test
    public void testSupports() {
        assertTrue(handle.supports());
    }

    @Test
    public void testGetURLs() {
        assertSame(EMPTY_URL_ARRAY, handle.getURLs(null));
    }

    @Test
    public void testRemoveURL() {
        assertFalse(handle.removeURL(null,null));
    }

    @Override
    @Test
    public void testGetPriority() {
        assertEquals(MIN_PRIORITY, handle.getPriority());
    }
}
