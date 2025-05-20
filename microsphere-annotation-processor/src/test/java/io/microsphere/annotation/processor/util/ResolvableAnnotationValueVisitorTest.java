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
import io.microsphere.annotation.processor.GenericTestService;
import io.microsphere.annotation.processor.TestService;
import io.microsphere.annotation.processor.annotation.TestAnnotation;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValue;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static java.util.Objects.deepEquals;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ResolvableAnnotationValueVisitor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResolvableAnnotationValueVisitor
 * @since 1.0.0
 */
public class ResolvableAnnotationValueVisitorTest extends AbstractAnnotationProcessingTest {

    private ResolvableAnnotationValueVisitor visitor;

    private ResolvableAnnotationValueVisitor visitor1;

    private ResolvableAnnotationValueVisitor visitor2;

    static final boolean BOOLEAN_VALUE = true;

    static final byte BYTE_VALUE = 1;

    static final char CHAR_VALUE = 1;

    static final double DOUBLE_VALUE = 1.0D;

    static final float FLOAT_VALUE = 1.0F;

    static final int INT_VALUE = 1;

    static final long LONG_VALUE = 1L;

    static final short SHORT_VALUE = 1;

    static final String STRING_VALUE = "testService";

    static final Class<?> TYPE_VALUE = GenericTestService.class;

    static final Class<?>[] TYPES_VALUE = {TestService.class, AutoCloseable.class, Serializable.class};

    private Map<ExecutableElement, AnnotationValue> testAnnotationAttributes;

    protected void beforeTest() {
        super.beforeTest();
        this.visitor = new ResolvableAnnotationValueVisitor();
        this.visitor1 = new ResolvableAnnotationValueVisitor(true);
        this.visitor2 = new ResolvableAnnotationValueVisitor(true, true);
        this.testAnnotationAttributes = getElementValues(testTypeElement, TestAnnotation.class);
    }

    @Test
    void testVisitBoolean() {
        assertEquals(BOOLEAN_VALUE, visitor.visitBoolean(BOOLEAN_VALUE, null));
        assertVisit(this.visitor, "z", BOOLEAN_VALUE);
        assertVisit(this.visitor1, "z", BOOLEAN_VALUE);
        assertVisit(this.visitor2, "z", BOOLEAN_VALUE);
    }

    @Test
    void testVisitByte() {
        assertEquals(BYTE_VALUE, visitor.visitByte(BYTE_VALUE, null));
        assertVisit(this.visitor, "b", BYTE_VALUE);
        assertVisit(this.visitor1, "b", BYTE_VALUE);
        assertVisit(this.visitor2, "b", BYTE_VALUE);
    }

    @Test
    void testVisitChar() {
        assertEquals(CHAR_VALUE, visitor.visitChar(CHAR_VALUE, null));
        assertVisit(this.visitor, "c", CHAR_VALUE);
        assertVisit(this.visitor1, "c", CHAR_VALUE);
        assertVisit(this.visitor2, "c", CHAR_VALUE);
    }

    @Test
    void testVisitDouble() {
        assertEquals(DOUBLE_VALUE, visitor.visitDouble(DOUBLE_VALUE, null));
        assertVisit(this.visitor, "d", DOUBLE_VALUE);
        assertVisit(this.visitor1, "d", DOUBLE_VALUE);
        assertVisit(this.visitor2, "d", DOUBLE_VALUE);
    }

    @Test
    void testVisitFloat() {
        assertEquals(FLOAT_VALUE, visitor.visitFloat(FLOAT_VALUE, null));
        assertVisit(this.visitor, "f", FLOAT_VALUE);
        assertVisit(this.visitor1, "f", FLOAT_VALUE);
        assertVisit(this.visitor2, "f", FLOAT_VALUE);
    }

    @Test
    void testVisitInt() {
        assertEquals(INT_VALUE, visitor.visitInt(INT_VALUE, null));
        assertVisit(this.visitor, "i", INT_VALUE);
        assertVisit(this.visitor1, "i", INT_VALUE);
        assertVisit(this.visitor2, "i", INT_VALUE);
    }

    @Test
    void testVisitLong() {
        assertEquals(LONG_VALUE, visitor.visitLong(LONG_VALUE, null));
        assertVisit(this.visitor, "l", LONG_VALUE);
        assertVisit(this.visitor1, "l", LONG_VALUE);
        assertVisit(this.visitor2, "l", LONG_VALUE);
    }

    @Test
    void testVisitShort() {
        assertEquals(SHORT_VALUE, visitor.visitShort(SHORT_VALUE, null));
        assertVisit(this.visitor, "s", SHORT_VALUE);
        assertVisit(this.visitor1, "s", SHORT_VALUE);
        assertVisit(this.visitor2, "s", SHORT_VALUE);
    }

    @Test
    void testVisitString() {
        assertEquals(STRING_VALUE, visitor.visitString(STRING_VALUE, null));
        assertVisit(this.visitor, "string", STRING_VALUE);
        assertVisit(this.visitor1, "string", STRING_VALUE);
        assertVisit(this.visitor1, "string", STRING_VALUE);
    }

    @Test
    void testVisitType() {
        assertEquals(TYPE_VALUE, visitor.visitType(getTypeMirror(TYPE_VALUE), null));
        assertVisit(this.visitor, "type", TYPE_VALUE);
        assertVisit(this.visitor1, "type", TYPE_VALUE.getName());
        assertVisit(this.visitor2, "type", TYPE_VALUE.getName());
    }

    @Test
    void testVisitEnumConstant() {
        assertVisit(this.visitor, "timeUnit", HOURS);
        assertVisit(this.visitor1, "timeUnit", HOURS);
        assertVisit(this.visitor2, "timeUnit", HOURS);
    }

    @Test
    void testVisitAnnotation() {
        String attributeName = "since";

        Map<String, Object> attributesMap = new LinkedHashMap<>();
        attributesMap.put("module", "");
        attributesMap.put("value", "1.0.0");

        TestAnnotation testAnnotation = testClass.getAnnotation(TestAnnotation.class);

        assertVisit(this.visitor, attributeName, testAnnotation.since());
        assertVisit(this.visitor1, attributeName, testAnnotation.since());
        assertVisit(this.visitor2, attributeName, attributesMap);
    }

    @Test
    void testVisitArray() {
        assertVisit(this.visitor, "types", TYPES_VALUE);
        assertVisit(this.visitor1, "types", of(TYPES_VALUE).map(Class::getName).toArray(String[]::new));
        assertVisit(this.visitor2, "types", of(TYPES_VALUE).map(Class::getName).toArray(String[]::new));
    }

    @Test
    void testVisitUnknown() {
        for (Entry<ExecutableElement, AnnotationValue> elementValue : this.testAnnotationAttributes.entrySet()) {
            ExecutableElement attributeMethod = elementValue.getKey();
            AnnotationValue annotationValue = elementValue.getValue();
            assertSame(annotationValue, visitor.visitUnknown(annotationValue, attributeMethod));
        }
    }

    void assertVisit(AnnotationValueVisitor visitor, String attributeName, Object expectedValue) {
        Entry<ExecutableElement, AnnotationValue> elementValue = getElementValue(this.testAnnotationAttributes, attributeName);
        ExecutableElement attributeMethod = elementValue.getKey();
        AnnotationValue annotationValue = elementValue.getValue();
        assertTrue(deepEquals(expectedValue, annotationValue.accept(visitor, attributeMethod)));
    }
}
