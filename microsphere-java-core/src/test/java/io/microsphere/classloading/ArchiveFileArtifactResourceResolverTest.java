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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static io.microsphere.net.URLUtils.ofURL;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ArchiveFileArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArchiveFileArtifactResourceResolver
 * @since 1.0.0
 */
public class ArchiveFileArtifactResourceResolverTest extends AbstractArtifactResourceResolverTest<ArchiveFileArtifactResourceResolver> {

    @Override
    protected void testResolve(ArchiveFileArtifactResourceResolver resolver) throws Throwable {
        testResolveOnNull(resolver);
        testResolveOnArchiveDirectory(resolver);
        testResolveOnNullArchiveFile(resolver);
        testResolveOnInvalidFormatArchiveFile(resolver);
        testResolveOnArchiveFile(resolver);
    }

    private void testResolveOnNull(ArchiveFileArtifactResourceResolver resolver) {
        assertNull(resolver.resolve(null));
    }

    private void testResolveOnArchiveDirectory(ArchiveFileArtifactResourceResolver resolver) {
        URL classResource = getClassResource(resolver.classLoader, getClass());
        Artifact artifact = resolver.resolve(classResource);
        assertNull(artifact);
    }

    private void testResolveOnNullArchiveFile(ArchiveFileArtifactResourceResolver resolver) {
        URL url = ofURL("file:///non-exists");
        Artifact artifact = resolver.resolve(url);
        assertNull(artifact);
    }

    private void testResolveOnInvalidFormatArchiveFile(ArchiveFileArtifactResourceResolver resolver) throws MalformedURLException {
        URL resourceURL = resolver.classLoader.getResource("META-INF/.jar");
        File file = new File(resourceURL.getFile());
        assertTrue(file.exists());
        resourceURL = ofURL("jar:"+file.toURI().toURL() +"!/com/acme/Test.class");
        Artifact artifact = resolver.resolve(resourceURL);
        assertNull(artifact);
    }

    protected void testResolveOnArchiveFile(ArchiveFileArtifactResourceResolver resolver) {
        URL classResource = getClassResource(resolver.classLoader, TEST_ANNOTATION_CLASS);
        Artifact artifact = resolver.resolve(classResource);
        assertEquals(TEST_ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(TEST_VERSION, artifact.getVersion());
    }
}
