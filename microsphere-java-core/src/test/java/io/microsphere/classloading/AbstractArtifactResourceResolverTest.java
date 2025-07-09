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
package io.microsphere.classloading;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AbstractArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractArtifactResourceResolver
 * @since 1.0.0
 */
abstract class AbstractArtifactResourceResolverTest<A extends AbstractArtifactResourceResolver> {

    static final Class<? extends Annotation> TEST_ANNOTATION_CLASS = Nonnull.class;

    static final String TEST_GROUP_ID = "com.google.code.findbugs";

    static final String TEST_ARTIFACT_ID = "jsr305";

    static final String TEST_VERSION = "3.0.2";

    protected A resolver;

    @BeforeEach
    void init() throws Throwable {
        this.resolver = createResolver();
    }

    protected A createResolver() throws Throwable {
        return (A) from(this.getClass())
                .as(AbstractArtifactResourceResolverTest.class)
                .getGenericType(0)
                .toClass()
                .newInstance();
    }

    @Test
    void testNonDefaultConstructor() throws Throwable {
        Class<?> resolveClass = resolver.getClass();

        Constructor constructor = resolveClass.getConstructor(int.class);
        assertNotNull(constructor.newInstance(0));

        constructor = resolveClass.getConstructor(ClassLoader.class, int.class);
        assertNotNull(constructor.newInstance(getDefaultClassLoader(), 0));
    }

    @Test
    final void testLogger() {
        assertNotNull(this.resolver.logger);
        assertEquals(this.resolver.getClass().getName(), this.resolver.logger.getName());
    }

    @Test
    final void testClassLoader() {
        assertNotNull(this.resolver.classLoader);
    }

    @Test
    final void testGetPriority() {
        assertEquals(this.resolver.priority, this.resolver.getPriority());
    }

    @Test
    final void testToString() {
        assertNotNull(this.resolver.toString());
    }

    @Test
    void testResolve() throws Throwable {
        testResolve(this.resolver);
    }

    abstract void testResolve(A resolver) throws Throwable;

}
