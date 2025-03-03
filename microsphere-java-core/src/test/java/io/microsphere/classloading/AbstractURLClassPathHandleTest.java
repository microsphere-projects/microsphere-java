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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Set;

import static io.microsphere.classloading.AbstractURLClassPathHandle.DEFAULT_PRIORITY;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AbstractURLClassPathHandle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractURLClassPathHandle
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractURLClassPathHandleTest extends BaseURLClassPathHandleTest<AbstractURLClassPathHandle> {

    @Test
    public abstract void testGetURLClassPathClassName();

    @Test
    public abstract void testGetUrlsFieldName();

    @Override
    @Test
    public void testGetPriority() {
        assertEquals(DEFAULT_PRIORITY, handle.getPriority());
    }
}
