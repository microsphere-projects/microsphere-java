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

package io.microsphere.test.annotation.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link AbstractAnnotationProcessingTest} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractAnnotationProcessingTest
 * @since 1.0.0
 */
public class AnnotationProcessingTest extends AbstractAnnotationProcessingTest implements TestExecutionExceptionHandler {

    @Test
    void test() {
        assertNull(NULL_TYPE_MIRROR);
        assertArrayEquals(new TypeMirror[0], EMPTY_TYPE_MIRROR_ARRAY);
        assertNull(NULL_TYPE_MIRROR_ARRAY);
        assertArrayEquals(new Collection[0], EMPTY_COLLECTION_ARRAY);
        assertNull(NULL_COLLECTION);
        assertNull(NULL_LIST);
        assertNull(NULL_ELEMENT);
        assertNull(NULL_ELEMENT_KIND);
        assertArrayEquals(new Element[0], EMPTY_ELEMENT_ARRAY);
        assertNull(NULL_ELEMENT_ARRAY);
        assertNull(NULL_TYPE_ELEMENT);
        assertNull(NULL_TYPE_ARRAY);
        assertArrayEquals(new Type[0], EMPTY_TYPE_ARRAY);
        assertNull(NULL_TYPE);
        assertNull(NULL_PROCESSING_ENVIRONMENT);
        assertNull(NULL_STRING);
        assertNull(NULL_STRING_ARRAY);
        assertNull(NULL_CLASS);
        assertNull(NULL_CLASS_ARRAY);
        assertNull(NULL_ANNOTATED_CONSTRUCT);
        assertNull(NULL_PREDICATE_ARRAY);
        assertNull(NULL_FIELD);
        assertNull(NULL_MODIFIER);
        assertNull(NULL_MODIFIER_ARRAY);
        assertNull(NULL_METHOD);
        assertNull(NULL_METHOD_ARRAY);
        assertNull(NULL_ANNOTATION_MIRROR);
    }

    @Test
    @ExtendWith(AnnotationProcessingTest.class)
    void testOnFailure() {
        throw new RuntimeException("For testing");
    }

    @Override
    protected void afterTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, Object result, Throwable failure) {
        super.afterTest(invocationContext, extensionContext, result, failure);
        if (failure != null) {
            assertEquals("For testing", failure.getMessage());
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (throwable != null) {
            Method method = context.getTestMethod().get();
            if ("testOnFailure".equals(method.getName())) {
                // ingnore
            }
        }
    }
}