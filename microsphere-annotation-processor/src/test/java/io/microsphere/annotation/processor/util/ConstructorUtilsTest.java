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
import org.springframework.core.env.Environment;

import javax.lang.model.element.ExecutableElement;
import java.io.Serializable;
import java.util.List;

import static io.microsphere.annotation.processor.util.ConstructorUtils.findConstructor;
import static io.microsphere.annotation.processor.util.ConstructorUtils.findDeclaredConstructors;
import static io.microsphere.annotation.processor.util.ConstructorUtils.getDeclaredConstructors;
import static io.microsphere.annotation.processor.util.ElementUtils.matchParameterTypes;
import static io.microsphere.lang.function.Predicates.alwaysFalse;
import static io.microsphere.lang.function.Predicates.alwaysTrue;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConstructorUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConstructorUtils
 * @since 1.0.0
 */
class ConstructorUtilsTest extends AbstractAnnotationProcessingTest {

    @Test
    void testGetDeclaredConstructors() {
        List<ExecutableElement> constructors = getDeclaredConstructors(this.testTypeElement);
        assertTestServiceImplConstructors(constructors);

        constructors = getDeclaredConstructors(this.testDeclaredType);
        assertTestServiceImplConstructors(constructors);
    }

    @Test
    void testGetDeclaredConstructorsOnNull() {
        assertSame(emptyList(), getDeclaredConstructors(NULL_TYPE_ELEMENT));
        assertSame(emptyList(), getDeclaredConstructors(NULL_TYPE_MIRROR));
    }

    @Test
    void testFindConstructor() {
        assertTestServiceImpl1stConstructor(ConstructorUtils.findConstructor(this.testTypeElement));
        assertTestServiceImpl1stConstructor(findConstructor(this.testDeclaredType));

        assertTestServiceImpl2ndConstructor(ConstructorUtils.findConstructor(this.testTypeElement, Environment.class));
        assertTestServiceImpl2ndConstructor(findConstructor(this.testDeclaredType, Environment.class));
    }

    @Test
    void testFindConstructorOnNull() {
        assertNull(ConstructorUtils.findConstructor(NULL_TYPE_ELEMENT));
        assertNull(findConstructor(NULL_TYPE_MIRROR));

        assertNull(ConstructorUtils.findConstructor(this.testTypeElement, null));
        assertNull(findConstructor(this.testDeclaredType, null));
    }

    @Test
    void testFindConstructorOnMismatch() {
        assertNull(ConstructorUtils.findConstructor(NULL_TYPE_ELEMENT, Object.class));
        assertNull(findConstructor(NULL_TYPE_MIRROR, Object.class));

        assertNull(ConstructorUtils.findConstructor(NULL_TYPE_ELEMENT, Object.class, String.class));
        assertNull(findConstructor(NULL_TYPE_MIRROR, Object.class, String.class));

        assertNull(ConstructorUtils.findConstructor(NULL_TYPE_ELEMENT, Object.class, String.class, Integer.class));
        assertNull(findConstructor(NULL_TYPE_MIRROR, Object.class, String.class, Integer.class));
    }

    @Test
    void testFindConstructors() {
        List<ExecutableElement> constructors = findDeclaredConstructors(this.testTypeElement);
        assertTestServiceImplConstructors(constructors);

        constructors = findDeclaredConstructors(this.testDeclaredType);
        assertTestServiceImplConstructors(constructors);

        constructors = findDeclaredConstructors(this.testTypeElement, alwaysTrue());
        assertTestServiceImplConstructors(constructors);

        constructors = findDeclaredConstructors(this.testDeclaredType, alwaysTrue());
        assertTestServiceImplConstructors(constructors);
    }

    @Test
    void testFindConstructorsOnNull() {
        assertSame(emptyList(), findDeclaredConstructors(NULL_TYPE_ELEMENT));
        assertSame(emptyList(), findDeclaredConstructors(NULL_TYPE_MIRROR));

        assertSame(emptyList(), findDeclaredConstructors(this.testTypeElement, null));
        assertSame(emptyList(), findDeclaredConstructors(this.testDeclaredType, null));
    }

    @Test
    void testFindConstructorsOnMismatch() {
        assertSame(emptyList(), findDeclaredConstructors(this.testTypeElement, alwaysFalse()));
        assertSame(emptyList(), findDeclaredConstructors(this.testDeclaredType, alwaysFalse()));
    }

    @Test
    void testFindConstructorsOnNotFound() {
        assertSame(emptyList(), findDeclaredConstructors(getTypeElement(Serializable.class)));
        assertSame(emptyList(), findDeclaredConstructors(getDeclaredType(Serializable.class)));

        assertSame(emptyList(), findDeclaredConstructors(getTypeElement(List.class)));
        assertSame(emptyList(), findDeclaredConstructors(getDeclaredType(List.class)));
    }

    void assertTestServiceImplConstructors(List<? extends ExecutableElement> constructors) {
        assertEquals(2, constructors.size());
        assertTestServiceImpl1stConstructor(constructors.get(0));
        assertTestServiceImpl2ndConstructor(constructors.get(1));
    }

    void assertTestServiceImpl1stConstructor(ExecutableElement constructor) {
        assertEquals(this.testTypeElement, constructor.getEnclosingElement());
        assertEquals(emptyList(), constructor.getParameters());
    }

    void assertTestServiceImpl2ndConstructor(ExecutableElement constructor) {
        assertEquals(this.testTypeElement, constructor.getEnclosingElement());
        assertEquals(1, constructor.getParameters().size());
        assertTrue(matchParameterTypes(constructor, Environment.class));
    }
}