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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getAttributeName;
import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementValues;
import static io.microsphere.annotation.processor.util.ClassUtils.loadClass;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.ArrayUtils.newArray;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.Enum.valueOf;
import static java.lang.reflect.Array.set;

/**
 * A visitor for resolving annotation values into their corresponding runtime representations.
 *
 * <p>This class extends {@link SimpleAnnotationValueVisitor6} to process annotation values and convert
 * them into appropriate Java objects such as primitives, strings, enums, classes, annotations, and arrays.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create an instance with default settings
 * ResolvableAnnotationValueVisitor visitor = new ResolvableAnnotationValueVisitor();
 *
 * // Visit an annotation value
 * AnnotationValue annotationValue = ...; // Obtain from an annotation mirror
 * Object resolvedValue = annotationValue.accept(visitor, executableElement);
 * }</pre>
 *
 * <h4>Custom behavior examples</h4>
 * <pre>{@code
 * // Create an instance that represents Class values as strings
 * ResolvableAnnotationValueVisitor visitor = new ResolvableAnnotationValueVisitor(true);
 *
 * // Create an instance that handles nested annotations as maps
 * ResolvableAnnotationValueVisitor visitor = new ResolvableAnnotationValueVisitor(false, true);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationValueVisitor
 * @since 1.0.0
 */
public class ResolvableAnnotationValueVisitor extends SimpleAnnotationValueVisitor6<Object, ExecutableElement> {

    private static final Class<?> ANNOTATION_PARSER_CLASS = resolveClass("sun.reflect.annotation.AnnotationParser");

    private static final Method annotationForMapMethod = findMethod(ANNOTATION_PARSER_CLASS, "annotationForMap", Class.class, Map.class);

    private static final boolean DEFAULT_CLASS_VALUES_AS_STRING = false;

    private static final boolean DEFAULT_NESTED_ANNOTATIONS_AS_MAP = false;

    private final boolean classValuesAsString;

    private final boolean nestedAnnotationsAsMap;

    public ResolvableAnnotationValueVisitor() {
        this(DEFAULT_CLASS_VALUES_AS_STRING, DEFAULT_NESTED_ANNOTATIONS_AS_MAP);
    }

    public ResolvableAnnotationValueVisitor(boolean classValuesAsString) {
        this(classValuesAsString, DEFAULT_NESTED_ANNOTATIONS_AS_MAP);
    }

    public ResolvableAnnotationValueVisitor(boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
        this.classValuesAsString = classValuesAsString;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public Object visitBoolean(boolean b, ExecutableElement attributeMethod) {
        return b;
    }

    @Override
    public Object visitByte(byte b, ExecutableElement attributeMethod) {
        return b;
    }

    @Override
    public Object visitChar(char c, ExecutableElement attributeMethod) {
        return c;
    }

    @Override
    public Object visitDouble(double d, ExecutableElement attributeMethod) {
        return d;
    }

    @Override
    public Object visitFloat(float f, ExecutableElement attributeMethod) {
        return f;
    }

    @Override
    public Object visitInt(int i, ExecutableElement attributeMethod) {
        return i;
    }

    @Override
    public Object visitLong(long i, ExecutableElement attributeMethod) {
        return i;
    }

    @Override
    public Object visitShort(short s, ExecutableElement attributeMethod) {
        return s;
    }

    @Override
    public Object visitString(String s, ExecutableElement attributeMethod) {
        return s;
    }

    @Override
    public Object visitType(TypeMirror t, ExecutableElement attributeMethod) {
        return classValuesAsString ? t.toString() : loadClass(t);
    }

    @Override
    public Object visitEnumConstant(VariableElement c, ExecutableElement attributeMethod) {
        Class enumClass = loadClass(c.asType());
        return valueOf(enumClass, c.toString());
    }

    @Override
    public Object visitAnnotation(AnnotationMirror a, ExecutableElement attributeMethod) {
        Map<ExecutableElement, AnnotationValue> elementValues = getElementValues(a);
        Map<String, Object> attributesMap = newFixedLinkedHashMap(elementValues.size());
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            ExecutableElement method = entry.getKey();
            String attributeName = getAttributeName(method);
            Object attributeValue = entry.getValue().accept(this, method);
            attributesMap.put(attributeName, attributeValue);
        }

        if (nestedAnnotationsAsMap) {
            return attributesMap;
        }

        Class annotationClass = loadClass(a.getAnnotationType());
        return invokeStaticMethod(annotationForMapMethod, annotationClass, attributesMap);
    }

    @Override
    public Object visitArray(List<? extends AnnotationValue> values, ExecutableElement attributeMethod) {
        int size = values.size();
        Class<?> componentType = getComponentType(attributeMethod);
        Object array = newArray(componentType, size);
        for (int i = 0; i < size; i++) {
            AnnotationValue value = values.get(i);
            Object attributeValue = value.accept(this, attributeMethod);
            set(array, i, attributeValue);
        }
        return array;
    }

    Class<?> getComponentType(ExecutableElement attributeMethod) {
        if (classValuesAsString) {
            return String.class;
        }
        ArrayType arrayType = (ArrayType) attributeMethod.getReturnType();
        return loadClass(arrayType.getComponentType());
    }

    @Override
    public Object visitUnknown(AnnotationValue av, ExecutableElement attributeMethod) {
        return av;
    }
}
