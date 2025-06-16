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

package io.microsphere.annotation.processor.model.util;


import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.annotation.processor.TestAnnotation;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.Map;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValue;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link JSONAnnotationValueVisitor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONAnnotationValueVisitor
 * @since 1.0.0
 */
class JSONAnnotationValueVisitorTest extends AbstractAnnotationProcessingTest {

    private StringBuilder jsonBuilder;

    private JSONAnnotationValueVisitor visitor;

    private Map<ExecutableElement, AnnotationValue> testAnnotationAttributes;

    protected void beforeTest() {
        super.beforeTest();
        this.jsonBuilder = new StringBuilder();
        this.visitor = new JSONAnnotationValueVisitor(jsonBuilder);
        this.testAnnotationAttributes = getElementValues(testTypeElement, TestAnnotation.class);
    }

    @Test
    void testVisitBoolean() {
        testVisit("z", "\"z\":true");
    }

    @Test
    void testVisitByte() {
        testVisit("b", "\"b\":1");
    }

    @Test
    void testVisitChar() {
        testVisit("c", "\"c\":\"b\"");
    }

    @Test
    void testVisitDouble() {
        testVisit("d", "\"d\":1.0");
    }

    @Test
    void testVisitFloat() {
        testVisit("f", "\"f\":1.0");
    }

    @Test
    void testVisitInt() {
        testVisit("i", "\"i\":1");
    }

    @Test
    void testVisitLong() {
        testVisit("l", "\"l\":1");
    }

    @Test
    void testVisitShort() {
        testVisit("s", "\"s\":1");
    }

    @Test
    void testVisitString() {
        testVisit("string", "\"string\":\"testService\"");
    }

    @Test
    void testVisitType() {
        testVisit("type", "\"type\":\"io.microsphere.annotation.processor.GenericTestService\"");
    }

    @Test
    void testVisitEnumConstant() {
        testVisit("timeUnit", "\"timeUnit\":\"HOURS\"");
    }

    @Test
    void testVisitAnnotation() {
        testVisit("since", "\"since\":{\"module\":\"\",\"value\":\"1.0.0\"}");
    }

    @Test
    void testVisitArray() {
        testVisit("properties", "\"properties\":[" +
                "{\"name\":\"key\",\"type\":\"java.lang.String\",\"defaultValue\":\"default-value\",\"required\":true,\"description\":\"description\"}," +
                "{\"name\":\"key2\",\"type\":\"java.lang.Integer\",\"defaultValue\":\"default-value2\",\"required\":true,\"description\":\"description2\"}," +
                "{\"name\":\"key3\",\"type\":\"java.lang.Class\",\"defaultValue\":\"default-value3\",\"required\":true,\"description\":\"description3\"}" +
                "]");
    }

    @Test
    void testVisitUnknown() {
        assertSame(this.jsonBuilder, visitor.visitUnknown(null, null));
    }

    void testVisit(String attributeName, String expectedJson) {
        Map.Entry<ExecutableElement, AnnotationValue> elementValue = getElementValue(this.testAnnotationAttributes, attributeName);
        ExecutableElement attributeMethod = elementValue.getKey();
        AnnotationValue annotationValue = elementValue.getValue();
        StringBuilder jsonBuilder = visitor.visit(annotationValue, attributeMethod);
        assertEquals(expectedJson, jsonBuilder.toString());
    }
}