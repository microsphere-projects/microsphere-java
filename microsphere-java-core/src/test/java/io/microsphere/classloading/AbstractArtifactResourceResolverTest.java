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
import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;

import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link AbstractArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since AbstractArtifactResourceResolver
 */
public abstract class AbstractArtifactResourceResolverTest<A extends AbstractArtifactResourceResolver> {

    protected A resolver;

    @BeforeEach
    public void init() throws Throwable {
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
    public void testNonDefaultConstructor() throws Throwable {
        Class<?> resolveClass = resolver.getClass();

        Constructor constructor = resolveClass.getConstructor(int.class);
        assertNotNull(constructor.newInstance(0));

        constructor = resolveClass.getConstructor(ClassLoader.class, int.class);
        assertNotNull(constructor.newInstance(getDefaultClassLoader(), 0));
    }

    @Test
    public final void testLogger() {
        assertNotNull(this.resolver.logger);
        assertEquals(this.resolver.getClass().getName(), this.resolver.logger.getName());
    }

    @Test
    public final void testClassLoader() {
        assertNotNull(this.resolver.classLoader);
    }

    @Test
    public final void testGetPriority() {
        assertEquals(this.resolver.priority, this.resolver.getPriority());
    }

    @Test
    public final void testToString() {
        assertNotNull(this.resolver.toString());
    }

    @Test
    public void testResolve() throws Throwable {
        testResolveForFile();
        testResolveForFileOnNotFound();
        testResolveForDirectory();
        testResolveForDirectoryOnNotFound();
        testResolveOnResource();
        testResolveOnNull();
    }

    protected void testResolveForFile() throws Throwable {
        assertArtifact(Nonnull.class);
    }

    protected void testResolveForFileOnNotFound() throws Throwable {
        URL resourceURL = resolveResourceURL(String.class);
        this.resolver.resolve(resourceURL);

        resourceURL = resolveResourceURL(ResolvableType.class);
        // the maven metadata resource can't be found in the module "spring-core",
        // except "META-INF/MANIFEST.MF"
        this.resolver.resolve(resourceURL);
    }

    protected void testResolveForDirectory() throws Throwable {
        assertArtifact(AbstractArtifactResourceResolverTest.class);
    }

    protected void testResolveForDirectoryOnNotFound() throws Throwable {
        URL resourceURL = resolveResourceURL(AbstractArtifactResourceResolver.class);
        assertNull(this.resolver.resolve(resourceURL));
    }

    protected void testResolveOnResource() throws Throwable {
        URL resourceURL = new URL("http://localhost/not-found/");
        assertNull(this.resolver.resolve(resourceURL));
    }

    protected void testResolveOnNull() throws Throwable {
        assertNull(this.resolver.resolve(null));
    }

    void assertArtifact(Class<?> targetClass) throws Throwable {
        URL resourceURL = resolveResourceURL(targetClass);
        Artifact artifact = this.resolver.resolve(resourceURL);
        assertArtifact(artifact);
    }

    URL resolveResourceURL(Class<?> targetClass) throws MalformedURLException {
        URL classResource = getClassResource(this.resolver.classLoader, targetClass);
        File archiveFile = resolveArchiveFile(classResource);
        return archiveFile == null ? null : archiveFile.toURI().toURL();
    }


    protected abstract void assertArtifact(Artifact artifact) throws Throwable;

}
