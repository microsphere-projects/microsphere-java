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

import org.springframework.core.ResolvableType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link StreamArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since AbstractArtifactResourceResolver
 */
public abstract class StreamArtifactResourceResolverTest<A extends StreamArtifactResourceResolver> extends AbstractArtifactResourceResolverTest<A> {

    void testResolve(A resolver) throws Throwable {
        testResolveForFile(resolver);
        testResolveForFileOnNotFound(resolver);
        testResolveForDirectory(resolver);
        testResolveForDirectoryOnNotFound(resolver);
        testResolveOnResource(resolver);
        testResolveOnNull(resolver);
    }

    void testResolveForFile(A resolver) throws Throwable {
        assertArtifact(resolver, TEST_ANNOTATION_CLASS);
    }

    void testResolveForFileOnNotFound(A resolver) throws Throwable {
        URL resourceURL = resolveResourceURL(String.class);
        resolver.resolve(resourceURL);

        resourceURL = resolveResourceURL(ResolvableType.class);
        // the maven metadata resource can't be found in the module "spring-core",
        // except "META-INF/MANIFEST.MF"
        resolver.resolve(resourceURL);
    }

    void testResolveForDirectory(A resolver) throws Throwable {
        assertArtifact(resolver, StreamArtifactResourceResolverTest.class);
    }

    void testResolveForDirectoryOnNotFound(A resolver) throws Throwable {
        URL resourceURL = resolveResourceURL(StreamArtifactResourceResolver.class);
        assertNull(resolver.resolve(resourceURL));
    }

    void testResolveOnResource(A resolver) throws Throwable {
        URL resourceURL = new URL("http://localhost/not-found/");
        assertNull(resolver.resolve(resourceURL));
    }

    void testResolveOnNull(A resolver) throws Throwable {
        assertNull(resolver.resolve(null));
    }

    void assertArtifact(A resolver, Class<?> targetClass) throws Throwable {
        URL resourceURL = resolveResourceURL(targetClass);
        Artifact artifact = resolver.resolve(resourceURL);
        assertArtifact(artifact);
    }

    URL resolveResourceURL(Class<?> targetClass) throws MalformedURLException {
        URL classResource = getClassResource(resolver.classLoader, targetClass);
        File archiveFile = resolveArchiveFile(classResource);
        return archiveFile == null ? null : archiveFile.toURI().toURL();
    }

    abstract void assertArtifact(Artifact artifact) throws Throwable;

}
