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

import io.microsphere.annotation.processor.model.element.StringAnnotationValue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesAttributeMethod;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesDefaultAttributeValue;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeName;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE_CHAR;
import static io.microsphere.json.JSONUtils.append;

/**
 * A concrete implementation of {@link AnnotatedElementJSONElementVisitor} that processes elements
 * annotated with {@link io.microsphere.annotation.ConfigurationProperty} and generates JSON
 * representations for them.
 *
 * <p>This class is responsible for visiting variable elements (fields) annotated with the
 * {@link io.microsphere.annotation.ConfigurationProperty} annotation and converting their
 * metadata into a structured JSON format. It captures key information such as the property name,
 * type, source type, and source field.</p>
 *
 * <h3>Example Usage</h3>
 *
 * <p>Assume the following annotation usage:</p>
 *
 * <pre>{@code
 * @ConfigurationProperty(name = "customName", type = String.class)
 * private String myProperty;
 * }</pre>
 *
 * <p>When this field is processed by the visitor, it generates JSON output similar to the following:</p>
 *
 * <pre>{@code
 * {
 *   "name": "customName",
 *   "type": "java.lang.String",
 *   "sourceType": "com.example.MyClass",
 *   "sourceField": "myProperty"
 * }
 * }</pre>
 *
 * <p>If the {@code name} attribute is not explicitly provided, the visitor uses the field's constant value.
 * Similarly, if the {@code type} attribute is not specified, it defaults to the field's actual type.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see io.microsphere.annotation.ConfigurationProperty
 * @see AnnotatedElementJSONElementVisitor
 * @since 1.0.0
 */
public class ConfigurationPropertyJSONElementVisitor extends AnnotatedElementJSONElementVisitor {

    public static final String ANNOTATION_CLASS_NAME = "io.microsphere.annotation.ConfigurationProperty";

    private static final String SOURCE_TYPE_PROPERTY_NAME = "sourceType";

    private static final String SOURCE_FILED_PROPERTY_NAME = "sourceField";

    public ConfigurationPropertyJSONElementVisitor(ProcessingEnvironment processingEnv) {
        super(processingEnv, ANNOTATION_CLASS_NAME);
    }

    /**
     * Processes the {@link VariableElement} annotated with
     * {@link io.microsphere.annotation.ConfigurationProperty}, converting its annotation values into
     * a JSON structure within the provided {@link StringBuilder}.
     *
     * <p>This method extracts the annotation attributes and appends them as key-value pairs in JSON format.
     * If the "name" attribute is empty, it uses the variable's constant value as the name. Additionally,
     * it appends metadata such as the source type and field name.</p>
     *
     * @param field       the variable element being visited
     * @param jsonBuilder the string builder used to accumulate JSON content
     * @return always returns {@code null} as no result needs to be propagated up the visitor chain
     */
    @Override
    public Boolean visitVariableAsField(VariableElement field, StringBuilder jsonBuilder) {
        AnnotationMirror annotation = getAnnotation(field, getAnnotationClassName());
        if (annotation != null) {
            JSONAnnotationValueVisitor visitor = new JSONAnnotationValueVisitor(jsonBuilder);
            jsonBuilder.append(LEFT_CURLY_BRACE_CHAR);
            Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(annotation);
            for (Entry<ExecutableElement, AnnotationValue> elementValue : elementValues.entrySet()) {
                ExecutableElement attributeMethod = elementValue.getKey();
                AnnotationValue annotationValue = elementValue.getValue();
                if (matchesAttributeMethod(attributeMethod, "name") && matchesDefaultAttributeValue(attributeMethod, annotationValue)) {
                    annotationValue = new StringAnnotationValue((String) field.getConstantValue());
                }
                if (matchesAttributeMethod(attributeMethod, "type") && matchesDefaultAttributeValue(attributeMethod, annotationValue)) {
                    annotationValue = new StringAnnotationValue(getTypeName(field.asType()));
                }
                visitor.visit(annotationValue, attributeMethod);
                jsonBuilder.append(COMMA_CHAR);
            }
            append(jsonBuilder, SOURCE_TYPE_PROPERTY_NAME, getTypeName(field.getEnclosingElement().asType()));
            jsonBuilder.append(COMMA_CHAR);
            append(jsonBuilder, SOURCE_FILED_PROPERTY_NAME, field.toString());
            jsonBuilder.append(RIGHT_CURLY_BRACE_CHAR);
            jsonBuilder.append(COMMA_CHAR);
            return true;
        }
        return false;
    }
}
