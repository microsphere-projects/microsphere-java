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

import static io.microsphere.classloading.MavenArtifact.create;

/**
 * A resolver implementation for Maven artifact metadata, extracting information from Maven POM properties files.
 *
 * <p>This class extends the {@link StreamArtifactResourceResolver}, which provides a base for resolving artifacts by reading
 * metadata from streams (either from archives or directly from resources). This resolver specifically targets Maven-style
 * artifacts where metadata is stored in "pom.properties" files under the "META-INF/maven/" directory.</p>
 *
 * <h2>How It Works</h2>
 * <ul>
 *     <li>The resolver checks if a given resource path matches the pattern of a Maven POM properties file using the
 *     {@link #isArtifactMetadata(String)} method. The pattern is typically:
 *     <code>META-INF/maven/&lt;groupId&gt;/&lt;artifactId&gt;/pom.properties</code>.</li>
 *     <li>If a match is found, it reads the properties file via the
 *     {@link #resolve(URL, InputStream, ClassLoader)} method and extracts key metadata: groupId, artifactId, and version.</li>
 *     <li>It then constructs an {@link Artifact} object using these properties and associates it with the original URL.</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Create a resolver with default priority
 * MavenArtifactResourceResolver resolver = new MavenArtifactResourceResolver();
 *
 * // Resolve artifact metadata from a JAR that contains META-INF/maven/org.example/my-artifact/pom.properties
 * URL resourceURL = new URL("jar:file:/path/to/your-artifact.jar!/some/path");
 * Artifact artifact = resolver.resolve(resourceURL);
 *
 * if (artifact != null) {
 *     System.out.println("Resolved Artifact:");
 *     System.out.println("Group ID: " + artifact.getGroupId());
 *     System.out.println("Artifact ID: " + artifact.getArtifactId());
 *     System.out.println("Version: " + artifact.getVersion());
 * }
 * }</pre>
 *
 * <h2>Customization</h2>
 * <p>You may extend this class to customize how artifact metadata is resolved or how the resulting artifact object is
 * constructed. For example, you could override the following methods:</p>
 *
 * <ul>
 *     <li>{@link #isArtifactMetadata(String)} – to support custom metadata paths.</li>
 *     <li>{@link #resolveArtifactMetaInfoInMavenPomProperties(Properties, URL)} – to add additional logic when parsing the
 *     POM properties or to enrich the resulting artifact.</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Artifact
 * @see StreamArtifactResourceResolver
 * @since 1.0.0
 */
public class MavenArtifactResourceResolver extends StreamArtifactResourceResolver {

    public static final String MAVEN_POM_PROPERTIES_RESOURCE_PREFIX = "META-INF/maven/";

    public static final String MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX = "/pom.properties";

    public static final String GROUP_ID_PROPERTY_NAME = "groupId";

    public static final String ARTIFACT_ID_PROPERTY_NAME = "artifactId";

    public static final String VERSION_PROPERTY_NAME = "version";

    public static final int DEFAULT_PRIORITY = 1;

    public MavenArtifactResourceResolver() {
        this(DEFAULT_PRIORITY);
    }

    public MavenArtifactResourceResolver(int priority) {
        super(priority);
    }

    public MavenArtifactResourceResolver(ClassLoader classLoader, int priority) {
        super(classLoader, priority);
    }

    @Override
    protected boolean isArtifactMetadata(String relativePath) {
        int begin = relativePath.indexOf(MAVEN_POM_PROPERTIES_RESOURCE_PREFIX);
        if (begin == 0) {
            begin += MAVEN_POM_PROPERTIES_RESOURCE_PREFIX.length();
            int end = relativePath.lastIndexOf(MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX);
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
