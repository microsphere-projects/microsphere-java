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
package io.microsphere.test.annotation.processing;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.reflect.Method;
import java.util.Set;

import static io.microsphere.constants.SymbolConstants.WILDCARD;
import static javax.lang.model.SourceVersion.latestSupported;

/**
 * {@link AnnotationProcessingTestProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@SupportedAnnotationTypes(WILDCARD)
class AnnotationProcessingTestProcessor extends AbstractProcessor {

    private final AbstractAnnotationProcessingTest abstractAnnotationProcessingTest;

    private final Invocation<Void> invocation;

    private final ReflectiveInvocationContext<Method> invocationContext;

    private final ExtensionContext extensionContext;

    AnnotationProcessingTestProcessor(AbstractAnnotationProcessingTest abstractAnnotationProcessingTest, Invocation<Void> invocation,
                                      ReflectiveInvocationContext<Method> invocationContext,
                                      ExtensionContext extensionContext) {
        this.abstractAnnotationProcessingTest = abstractAnnotationProcessingTest;
        this.invocation = invocation;
        this.invocationContext = invocationContext;
        this.extensionContext = extensionContext;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ReflectiveInvocationContext<Method> invocationContext = this.invocationContext;
        ExtensionContext extensionContext = this.extensionContext;
        if (!roundEnv.processingOver()) {
            prepare(roundEnv);
            Object result = null;
            Throwable failure = null;
            abstractAnnotationProcessingTest.beforeTest(invocationContext, extensionContext);
            try {
                result = invocation.proceed();
            } catch (Throwable throwable) {
                failure = throwable;
            } finally {
                abstractAnnotationProcessingTest.afterTest(invocationContext, extensionContext, result, failure);
            }
        }
        return false;
    }

    void prepare(RoundEnvironment roundEnv) {
        ProcessingEnvironment processingEnv = super.processingEnv;
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();
        Class<?> testClass = this.invocationContext.getTargetClass();

        abstractAnnotationProcessingTest.roundEnv = roundEnv;
        abstractAnnotationProcessingTest.processingEnv = processingEnv;
        abstractAnnotationProcessingTest.elements = elements;
        abstractAnnotationProcessingTest.types = types;
        abstractAnnotationProcessingTest.initTestClass(testClass);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }
}