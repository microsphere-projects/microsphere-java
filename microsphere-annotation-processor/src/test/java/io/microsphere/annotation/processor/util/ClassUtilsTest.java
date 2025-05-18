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

package io.microsphere.annotation.processor.util;

import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import static io.microsphere.annotation.processor.util.ClassUtils.getClassName;
import static io.microsphere.annotation.processor.util.ClassUtils.loadClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ClassUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassUtils
 * @since 1.0.0
 */
public class ClassUtilsTest extends AbstractAnnotationProcessingTest {

    @Test
    void testGetClassName() {
        assertEquals(this.testClassName, getClassName(this.testTypeMirror));
    }

    @Test
    void testLoadClassOnTypeMirror() {
        assertSame(this.testClass, loadClass(this.testTypeMirror));
    }

    @Test
    void testLoadClass() {
        assertSame(ComponentScan.Filter.class, loadClass("org.springframework.context.annotation.ComponentScan.Filter"));
    }
}
