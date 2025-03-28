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
package io.microsphere.annotation.processor;

import io.microsphere.annotation.processor.util.TypeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.annotation.processor.util.TypeUtils.ofDeclaredType;

/**
 * Abstract {@link Annotation} Processing Test case
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(CompilerInvocationInterceptor.class)
public abstract class AbstractAnnotationProcessingTest {

    protected static final TypeMirror NULL_TYPE_MIRROR = null;

    protected static final TypeMirror[] EMPTY_TYPE_MIRROR_ARRAY = new TypeMirror[0];

    protected static final TypeMirror[] NULL_TYPE_MIRROR_ARRAY = null;

    protected static final Collection[] EMPTY_COLLECTION_ARRAY = new Collection[0];

    protected static final Collection NULL_COLLECTION = null;

    protected static final List NULL_LIST = null;

    protected static final Element NULL_ELEMENT = null;

    protected static final ElementKind NULL_ELEMENT_KIND = null;

    protected static final Element[] EMPTY_ELEMENT_ARRAY = new Element[0];

    protected static final Element[] NULL_ELEMENT_ARRAY = null;

    protected static final TypeElement NULL_TYPE_ELEMENT = null;

    protected static final Type[] NULL_TYPE_ARRAY = null;

    protected static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    protected static final Type NULL_TYPE = null;

    protected static final ProcessingEnvironment NULL_PROCESSING_ENVIRONMENT = null;

    protected static final String NULL_STRING = null;

    protected static final String[] NULL_STRING_ARRAY = null;

    protected static final Class NULL_CLASS = null;

    protected static final Class[] NULL_CLASS_ARRAY = null;

    protected static final AnnotatedConstruct NULL_ANNOTATED_CONSTRUCT = null;

    protected static final Predicate[] NULL_PREDICATE_ARRAY = null;

    protected static final VariableElement NULL_FIELD = null;

    protected static final Modifier NULL_MODIFIER = null;

    protected static final Modifier[] NULL_MODIFIER_ARRAY = null;

    protected static final ExecutableElement NULL_METHOD = null;

    protected static final ExecutableElement[] NULL_METHOD_ARRAY = null;

    static ThreadLocal<AbstractAnnotationProcessingTest> testInstanceHolder = new ThreadLocal<>();

    protected ProcessingEnvironment processingEnv;

    protected Elements elements;

    protected Types types;

    protected Class<?> testClass;

    protected String testClassName;

    protected TypeElement testTypeElement;

    protected TypeMirror testTypeMirror;

    protected DeclaredType testDeclaredType;

    @BeforeEach
    public final void init() {
        testInstanceHolder.set(this);
    }

    @AfterEach
    public final void destroy() {
        testInstanceHolder.remove();
    }

    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
    }

    protected void beforeTest() {
        this.testClass = TestServiceImpl.class;
        this.testClassName = TestServiceImpl.class.getName();
        this.testTypeElement = getTypeElement(testClass);
        this.testTypeMirror = this.testTypeElement.asType();
        this.testDeclaredType = ofDeclaredType(this.testTypeElement);
    }

    protected void afterTest() {
    }

    protected List<TypeMirror> getTypeMirrors(Type... types) {
        return TypeUtils.getTypeMirrors(processingEnv, types);
    }

    protected TypeMirror getTypeMirror(Type type) {
        return TypeUtils.getTypeMirror(processingEnv, type);
    }

    protected List<TypeElement> getTypeElements(Type... types) {
        return TypeUtils.getTypeElements(processingEnv, types);
    }

    protected TypeElement getTypeElement(Type type) {
        return TypeUtils.getTypeElement(processingEnv, type);
    }

    protected Element[] getElements(Type... types) {
        return getTypeMirrors(types).stream().map(TypeUtils::ofTypeElement).toArray(Element[]::new);
    }

    protected DeclaredType getDeclaredType(Type type) {
        return TypeUtils.getDeclaredType(processingEnv, type);
    }

}
