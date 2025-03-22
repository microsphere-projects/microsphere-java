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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * Abstract {@link Annotation} Processing Test case
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(CompilerInvocationInterceptor.class)
public abstract class AbstractAnnotationProcessingTest {

    static ThreadLocal<AbstractAnnotationProcessingTest> testInstanceHolder = new ThreadLocal<>();

    protected ProcessingEnvironment processingEnv;

    protected Elements elements;

    protected Types types;

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
