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

import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static io.microsphere.classloading.MavenArtifactResolver.DEFAULT_PRIORITY;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.resolveURLClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MavenArtifactResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MavenArtifactResolver
 * @since 1.0.0
 */
public class MavenArtifactResolverTest extends AbstractArtifactResolverTest<MavenArtifactResolver> {

    @Override
    protected MavenArtifactResolver createArtifactResolver() {
        return new MavenArtifactResolver();
    }

    @Override
    protected int getPriority() {
        return DEFAULT_PRIORITY;
    }

    @Test
    public void testFindMavenPomPropertiesResource() throws IOException {
        URLClassLoader urlClassLoader = resolveURLClassLoader(this.classLoader);
        URL mavenPomPropertiesResource = mavenPomPropertiesResourceOfNonnull(urlClassLoader);
        assertNotNull(mavenPomPropertiesResource);

        URL url = getClassResource(String.class);
        mavenPomPropertiesResource = artifactResolver.findMavenPomPropertiesResource(url, urlClassLoader);
        assertNull(mavenPomPropertiesResource);
    }

    @Test
    public void testResolveArtifactMetaInfoInMavenPomProperties() throws IOException {
        URLClassLoader urlClassLoader = resolveURLClassLoader(this.classLoader);
        URL mavenPomPropertiesResource = mavenPomPropertiesResourceOfNonnull(urlClassLoader);

        MavenArtifact artifact = (MavenArtifact) artifactResolver.resolveArtifactMetaInfoInMavenPomProperties(mavenPomPropertiesResource);
        assertEquals("com.google.code.findbugs", artifact.getGroupId());
        assertEquals("jsr305", artifact.getArtifactId());
    }

    private URL mavenPomPropertiesResourceOfNonnull(URLClassLoader urlClassLoader) throws IOException {
        URL url = getClassResource(Nonnull.class);
        return artifactResolver.findMavenPomPropertiesResource(url, urlClassLoader);
    }
}
