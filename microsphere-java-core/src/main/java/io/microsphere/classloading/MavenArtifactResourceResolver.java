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

import io.microsphere.filter.JarEntryFilter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.classloading.MavenArtifact.create;
import static io.microsphere.net.URLUtils.isJarURL;
import static io.microsphere.util.jar.JarUtils.filter;
import static io.microsphere.util.jar.JarUtils.toJarFile;

/**
 * {@link ArtifactResourceResolver} for Maven
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactResourceResolver
 * @since 1.0.0
 */
public class MavenArtifactResourceResolver extends AbstractArtifactResourceResolver {

    public static final String MAVEN_POM_PROPERTIES_RESOURCE_PREFIX = "META-INF/maven/";

    public static final String MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX = "/pom.properties";

    public static final MavenPomPropertiesFilter MAVEN_POM_PROPERTIES_FILTER = new MavenPomPropertiesFilter();

    public static final String GROUP_ID_PROPERTY_NAME = "groupId";

    public static final String ARTIFACT_ID_PROPERTY_NAME = "artifactId";

    public static final String VERSION_PROPERTY_NAME = "version";

    public static final int DEFAULT_PRIORITY = 1;

    public MavenArtifactResourceResolver() {
        super(DEFAULT_PRIORITY);
    }

    public MavenArtifactResourceResolver(ClassLoader classLoader) {
        this(classLoader, DEFAULT_PRIORITY);
    }

    public MavenArtifactResourceResolver(ClassLoader classLoader, int priority) {
        super(classLoader, priority);
    }

    @Override
    protected Artifact doResolve(URL resourceURL, ClassLoader classLoader) throws IOException {
        URL mavenPomPropertiesResource = findMavenPomPropertiesResource(resourceURL, classLoader);
        return resolveArtifactMetaInfoInMavenPomProperties(mavenPomPropertiesResource);
    }

    URL findMavenPomPropertiesResource(URL classPathURL, ClassLoader classLoader) throws IOException {
        if (isJarURL(classPathURL)) {
            return findMavenPomPropertiesResourceInJar(classPathURL, classLoader);
        }
        return null;
    }

    URL findMavenPomPropertiesResourceInJar(URL resourceURL, ClassLoader classLoader) throws IOException {
        JarFile jarFile = toJarFile(resourceURL);
        List<JarEntry> entries = filter(jarFile, MAVEN_POM_PROPERTIES_FILTER);
        if (entries.isEmpty()) {
            return null;
        }
        JarEntry jarEntry = entries.get(0);
        String relativePath = jarEntry.getName();
        return classLoader.getResource(relativePath);
    }

    Artifact resolveArtifactMetaInfoInMavenPomProperties(URL mavenPomPropertiesResourceURL) throws IOException {
        Artifact artifact = null;
        if (mavenPomPropertiesResourceURL != null) {
            try (InputStream mavenPomPropertiesStream = mavenPomPropertiesResourceURL.openStream()) {
                Properties properties = new Properties();
                properties.load(mavenPomPropertiesStream);
                URL artifactResourceURL = resolveArtifactResourceURL(mavenPomPropertiesResourceURL);
                artifact = resolveArtifactMetaInfoInMavenPomProperties(properties, artifactResourceURL);
            }
        }
        return artifact;
    }

    Artifact resolveArtifactMetaInfoInMavenPomProperties(Properties properties,
                                                         URL artifactResourceURL) {
        String groupId = properties.getProperty(GROUP_ID_PROPERTY_NAME);
        String artifactId = properties.getProperty(ARTIFACT_ID_PROPERTY_NAME);
        String version = properties.getProperty(VERSION_PROPERTY_NAME);
        return create(groupId, artifactId, version, artifactResourceURL);
    }

    static class MavenPomPropertiesFilter implements JarEntryFilter {

        @Override
        public boolean accept(JarEntry entry) {
            String name = entry.getName();
            int begin = name.indexOf(MAVEN_POM_PROPERTIES_RESOURCE_PREFIX);
            if (begin == 0) {
                begin += MAVEN_POM_PROPERTIES_RESOURCE_PREFIX.length();
                int end = name.lastIndexOf(MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX);
                return end > begin;
            }

            return false;
        }
    }
}
