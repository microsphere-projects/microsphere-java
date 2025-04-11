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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarEntry;

import static io.microsphere.classloading.MavenArtifact.create;

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
    protected boolean isArtifactMetadataEntry(JarEntry jarEntry) {
        String name = jarEntry.getName();
        int begin = name.indexOf(MAVEN_POM_PROPERTIES_RESOURCE_PREFIX);
        if (begin == 0) {
            begin += MAVEN_POM_PROPERTIES_RESOURCE_PREFIX.length();
            int end = name.lastIndexOf(MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX);
            return end > begin;
        }

        return false;
    }

    @Override
    protected Artifact resolve(URL resourceURL, InputStream artifactMetadataData, ClassLoader classLoader) throws IOException {
        Properties properties = new Properties();
        properties.load(artifactMetadataData);
        return resolveArtifactMetaInfoInMavenPomProperties(properties, resourceURL);
    }

    Artifact resolveArtifactMetaInfoInMavenPomProperties(Properties properties,
                                                         URL artifactResourceURL) {
        String groupId = properties.getProperty(GROUP_ID_PROPERTY_NAME);
        String artifactId = properties.getProperty(ARTIFACT_ID_PROPERTY_NAME);
        String version = properties.getProperty(VERSION_PROPERTY_NAME);
        return create(groupId, artifactId, version, artifactResourceURL);
    }
}
