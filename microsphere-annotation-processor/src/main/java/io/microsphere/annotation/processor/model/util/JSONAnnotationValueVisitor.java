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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttributeName;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeName;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET_CHAR;
import static io.microsphere.util.JSONUtils.append;
import static io.microsphere.util.JSONUtils.appendName;

/**
 * {@link AnnotationValueVisitor} to assemble JSON content
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JSONAnnotationValueVisitor extends SimpleAnnotationValueVisitor6<StringBuilder, ExecutableElement> {

    private final StringBuilder jsonBuilder;

    public JSONAnnotationValueVisitor(StringBuilder jsonBuilder) {
        super(jsonBuilder);
        this.jsonBuilder = jsonBuilder;
    }


    @Override
    public StringBuilder visitBoolean(boolean value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitByte(byte value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitChar(char value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitDouble(double value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitFloat(float value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitInt(int value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitLong(long value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitShort(short value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitString(String value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value);
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitType(TypeMirror value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), getTypeName(value));
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitEnumConstant(VariableElement value, ExecutableElement attributeMethod) {
        append(jsonBuilder, getAttributeName(attributeMethod), value.getSimpleName().toString());
        return jsonBuilder;
    }

    @Override
    public StringBuilder visitAnnotation(AnnotationMirror value, ExecutableElement attributeMethod) {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(value);
        Iterator<Entry<ExecutableElement, AnnotationValue>> iterator = elementValues.entrySet().iterator();
        StringBuilder annotationJsonBuilder = new StringBuilder();
        annotationJsonBuilder.append(LEFT_CURLY_BRACE_CHAR);
        JSONAnnotationValueVisitor visitor = new JSONAnnotationValueVisitor(annotationJsonBuilder);
        while (iterator.hasNext()) {
            Entry<ExecutableElement, AnnotationValue> entry = iterator.next();
            AnnotationValue annotationValue = entry.getValue();
            visitor.visit(annotationValue, entry.getKey());
            if (iterator.hasNext()) {
                annotationJsonBuilder.append(COMMA_CHAR);
            }
        }
        annotationJsonBuilder.append(RIGHT_CURLY_BRACE_CHAR);

        return doAppend(attributeMethod, annotationJsonBuilder);
    }

    @Override
    public StringBuilder visitArray(List<? extends AnnotationValue> values, ExecutableElement attributeMethod) {
        StringBuilder arrayJsonBuilder = new StringBuilder();

        arrayJsonBuilder.append(LEFT_SQUARE_BRACKET_CHAR);
        int size = values.size();
        JSONAnnotationValueVisitor visitor = new JSONAnnotationValueVisitor(arrayJsonBuilder);
        for (int i = 0; i < size; i++) {
            AnnotationValue annotationValue = values.get(i);
            annotationValue.accept(visitor, null);
            if (i < size - 1) {
                arrayJsonBuilder.append(COMMA_CHAR);
            }
        }
        arrayJsonBuilder.append(RIGHT_SQUARE_BRACKET_CHAR);

        return doAppend(attributeMethod, arrayJsonBuilder);
    }

    @Override
    public StringBuilder visitUnknown(AnnotationValue annotationValue, ExecutableElement attributeMethod) {
        return jsonBuilder;
    }

    protected StringBuilder doAppend(ExecutableElement attributeMethod, StringBuilder value) {
        appendName(this.jsonBuilder, getAttributeName(attributeMethod))
                .append(value);
        return this.jsonBuilder;
    }
}
