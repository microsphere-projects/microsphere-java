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

package io.microsphere.lang.model.util;


import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AnnotatedElementJSONElementVisitor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedElementJSONElementVisitor
 * @since 1.0.0
 */
class AnnotatedElementJSONElementVisitorTest extends UtilTest {

    @Test
    void test() {
        String annotationClassName = Test.class.getName();
        AnnotatedElementJSONElementVisitor visitor = new AnnotatedElementJSONElementVisitor(super.processingEnv, annotationClassName) {
        };

        assertEquals(annotationClassName, visitor.getAnnotationClassName());

        ExecutableElement testMethod = getMethod(AnnotatedElementJSONElementVisitorTest.class, "test");
        assertTrue(visitor.supports(testMethod));
        assertFalse(visitor.supports(super.testTypeElement));
        assertFalse(visitor.supports(NULL_ELEMENT));
    }

}