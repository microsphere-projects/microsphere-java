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

import io.microsphere.annotation.Nonnull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;

import static io.microsphere.annotation.processor.util.AnnotationUtils.getElementTypes;
import static io.microsphere.annotation.processor.util.ElementUtils.matchesElementType;
import static io.microsphere.annotation.processor.util.TypeUtils.getDeclaredType;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * {@link ElementVisitor} to assemble JSON on the annotated elements based on the specified {@link Annotation annotation}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSONElementVisitor
 * @see AnnotatedElement
 * @see Annotation
 * @see ElementType
 * @since 1.0.0
 */
public abstract class AnnotatedElementJSONElementVisitor extends JSONElementVisitor {

    protected final ProcessingEnvironment processingEnv;

    protected final Elements elements;

    protected final String annotationClassName;

    protected final DeclaredType annotationType;

    protected final TypeElement annotationTypeElement;

    protected final ElementType[] elementTypes;

    protected AnnotatedElementJSONElementVisitor(ProcessingEnvironment processingEnv, String annotationClassName) {
        assertNotNull(processingEnv, () -> "The 'processingEnv' argument must not be null");
        assertNotNull(annotationClassName, () -> "The 'annotationClassName' argument must not be null");
        this.processingEnv = processingEnv;
        this.elements = processingEnv.getElementUtils();
        this.annotationClassName = annotationClassName;
        this.annotationType = getDeclaredType(processingEnv, annotationClassName);
        this.annotationTypeElement = this.elements.getTypeElement(annotationClassName);
        assertNotNull(this.annotationTypeElement, () -> "The annotation can't be found by name : " + annotationClassName);
        this.elementTypes = getElementTypes(annotationType);
    }

    /**
     * Get the annotation class name
     *
     * @return the annotation class name
     */
    @Nonnull
    public final String getAnnotationClassName() {
        return this.annotationClassName;
    }

    protected boolean supports(Element e) {
        return matchesElementType(e, this.elementTypes);
    }

}
