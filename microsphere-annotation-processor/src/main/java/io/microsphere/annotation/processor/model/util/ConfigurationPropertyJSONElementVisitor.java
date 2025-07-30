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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.beans.ConfigurationProperty.Metadata;
import io.microsphere.metadata.ConfigurationPropertyJSONGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getAnnotation;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttributeName;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static io.microsphere.annotation.processor.util.AnnotationUtils.matchesDefaultAttributeValue;
import static io.microsphere.annotation.processor.util.ClassUtils.getClassName;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeName;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.util.ServiceLoaderUtils.loadFirstService;

/**
 * {@link ConfigurationProperty @ConfigurationProperty}'s {@link AnnotatedElementJSONElementVisitor} based on
 * {@link ConfigurationPropertyJSONGenerator} generating the JSON representation of the configuration property metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedElementJSONElementVisitor
 * @see ConfigurationProperty
 * @see io.microsphere.beans.ConfigurationProperty
 * @since 1.0.0
 */
public class ConfigurationPropertyJSONElementVisitor extends AnnotatedElementJSONElementVisitor {

    public static final String CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME = "io.microsphere.annotation.ConfigurationProperty";

    private final ConfigurationPropertyJSONGenerator generator;

    public ConfigurationPropertyJSONElementVisitor(ProcessingEnvironment processingEnv) {
        super(processingEnv, CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME);
        this.generator = loadFirstService(ConfigurationPropertyJSONGenerator.class);
    }

    @Override
    public Boolean visitVariableAsField(VariableElement field, StringBuilder jsonBuilder) {
        AnnotationMirror annotation = getAnnotation(field, getAnnotationClassName());
        if (annotation != null) {
            io.microsphere.beans.ConfigurationProperty configurationProperty = null;
            Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(annotation);
            for (Map.Entry<ExecutableElement, AnnotationValue> elementValue : elementValues.entrySet()) {
                ExecutableElement attributeMethod = elementValue.getKey();
                String attributeName = getAttributeName(attributeMethod);
                AnnotationValue annotationValue = elementValue.getValue();
                if ("name".equals(attributeName)) {
                    String name = resolveName(field, attributeMethod, annotationValue);
                    configurationProperty = new io.microsphere.beans.ConfigurationProperty(name);
                } else if ("type".equals(attributeName)) {
                    String type = resolveType(field, attributeMethod, annotationValue);
                    configurationProperty.setType(type);
                } else if ("defaultValue".equals(attributeName)) {
                    String defaultValue = resolveStringValue(attributeMethod, annotationValue);
                    configurationProperty.setDefaultValue(defaultValue);
                } else if ("required".equals(attributeName)) {
                    boolean required = (boolean) annotationValue.getValue();
                    configurationProperty.setRequired(required);
                } else if ("description".equals(attributeName)) {
                    String description = resolveStringValue(attributeMethod, annotationValue);
                    configurationProperty.setDescription(description);
                } else if ("source".equals(attributeName)) {
                    setSources(configurationProperty, annotationValue);
                }
            }
            setDeclaredClass(configurationProperty, field);
            setDeclaredField(configurationProperty, field);

            String json = generator.generate(configurationProperty);
            jsonBuilder.append(json);
            jsonBuilder.append(COMMA_CHAR);
            return true;
        }
        return false;
    }

    public ConfigurationPropertyJSONGenerator getGenerator() {
        return generator;
    }

    private String resolveName(VariableElement field, ExecutableElement attributeMethod, AnnotationValue annotationValue) {
        Object value = matchesDefaultAttributeValue(attributeMethod, annotationValue) ? field.getConstantValue() : annotationValue.getValue();
        return (String) value;
    }

    private String resolveType(VariableElement field, ExecutableElement attributeMethod, AnnotationValue annotationValue) {
        Object value = matchesDefaultAttributeValue(attributeMethod, annotationValue) ? field.asType() : annotationValue.getValue();
        TypeMirror type = (TypeMirror) value;
        return getTypeName(type);
    }

    private String resolveStringValue(ExecutableElement attributeMethod, AnnotationValue annotationValue) {
        Object value = matchesDefaultAttributeValue(attributeMethod, annotationValue) ? null : annotationValue.getValue();
        return (String) value;
    }

    private void setSources(io.microsphere.beans.ConfigurationProperty configurationProperty, AnnotationValue annotationValue) {
        List<? extends AnnotationValue> sources = (List<? extends AnnotationValue>) annotationValue.getValue();
        Metadata metadata = configurationProperty.getMetadata();
        for (AnnotationValue source : sources) {
            String sourceValue = (String) source.getValue();
            metadata.getSources().add(sourceValue);
        }
    }

    private void setDeclaredClass(io.microsphere.beans.ConfigurationProperty configurationProperty, VariableElement field) {
        Element element = field.getEnclosingElement();
        String declaredClassName = getClassName(element.asType());
        configurationProperty.getMetadata().setDeclaredClass(declaredClassName);
    }

    private void setDeclaredField(io.microsphere.beans.ConfigurationProperty configurationProperty, VariableElement field) {
        String declaredFieldName = field.getSimpleName().toString();
        configurationProperty.getMetadata().setDeclaredField(declaredFieldName);
    }

}
