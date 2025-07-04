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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
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
public class AnnotationProcessingTestProcessor extends AbstractProcessor {

    private final AbstractAnnotationProcessingTest abstractAnnotationProcessingTest;
    private final InvocationInterceptor.Invocation<Void> invocation;

    private final ReflectiveInvocationContext<Method> invocationContext;

    private final ExtensionContext extensionContext;

    public AnnotationProcessingTestProcessor(AbstractAnnotationProcessingTest abstractAnnotationProcessingTest, InvocationInterceptor.Invocation<Void> invocation,
                                             ReflectiveInvocationContext<Method> invocationContext,
                                             ExtensionContext extensionContext) {
        this.abstractAnnotationProcessingTest = abstractAnnotationProcessingTest;
        this.invocation = invocation;
        this.invocationContext = invocationContext;
        this.extensionContext = extensionContext;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            prepare(roundEnv);
            abstractAnnotationProcessingTest.beforeTest();
            try {
                invocation.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            } finally {
                abstractAnnotationProcessingTest.afterTest();
            }
        }
        return false;
    }

    protected void prepare(RoundEnvironment roundEnv) {
        abstractAnnotationProcessingTest.roundEnv = roundEnv;
        abstractAnnotationProcessingTest.processingEnv = super.processingEnv;
        abstractAnnotationProcessingTest.elements = super.processingEnv.getElementUtils();
        abstractAnnotationProcessingTest.types = super.processingEnv.getTypeUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }
}