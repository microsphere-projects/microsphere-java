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

import io.microsphere.annotation.ConfigurationProperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static io.microsphere.classloading.Artifact.create;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.jar.JarUtils.MANIFEST_RESOURCE_PATH;
import static java.lang.System.getProperty;

/**
 * {@link ArtifactResourceResolver} for Manifest
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StreamArtifactResourceResolver
 * @see ArtifactResourceResolver
 * @since 1.0.0
 */
public class ManifestArtifactResourceResolver extends StreamArtifactResourceResolver {

    /**
     * Default property value for attribute names used to extract the artifact ID from the MANIFEST file.
     * The attributes are searched in order and the first non-null value is used as the artifact ID.
     *
     * <p>
     * The default attribute names are:
     * <ul>
     *     <li>Bundle-Name - typically used in OSGi environments</li>
     *     <li>Automatic-Module-Name - used for Java 9+ module systems</li>
     *     <li>Implementation-Title - standard JAR manifest attribute</li>
     * </ul>
     */
    public static final String DEFAULT_ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_VALUE = "Bundle-Name,Automatic-Module-Name,Implementation-Title";

    /**
     * Default property value for attribute names used to extract the artifact version from the MANIFEST file.
     * The attributes are searched in order and the first non-null value is used as the artifact version.
     *
     * <p>
     * The default attribute names are:
     * <ul>
     *     <li>Bundle-Version - typically used in OSGi environments</li>
     *     <li>Implementation-Version - standard JAR manifest attribute</li>
     * </ul>
     */
    public static final String DEFAULT_VERSION_ATTRIBUTE_NAMES_PROPERTY_VALUE = "Bundle-Version,Implementation-Version";

    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_VALUE,
            description = "The attribute names in the 'META-INF/MANIFEST' resource are retrieved as the artifact id"
    )
    /**
     * The configuration property name for specifying attribute names in the MANIFEST file to extract the artifact ID.
     *
     * <p>This property allows users to customize which manifest attributes should be used to determine the artifact ID.
     * By default, it uses the values defined in {@link #DEFAULT_ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_VALUE}.
     *
     * <p>Example usage:
     * <pre>
     *   -Dmicrosphere.artifact-id.manifest-attribute-names=Custom-Name,Implementation-Title
     * </pre>
     */
    public static final String ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "artifact-id.manifest-attribute-names";

    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_VERSION_ATTRIBUTE_NAMES_PROPERTY_VALUE,
            description = "The attribute names in the 'META-INF/MANIFEST' resource are retrieved as the artifact version"
    )
    /**
     * The configuration property name for specifying attribute names in the MANIFEST file to extract the artifact version.
     *
     * <p>This property allows users to customize which manifest attributes should be used to determine the artifact version.
     * By default, it uses the values defined in {@link #DEFAULT_VERSION_ATTRIBUTE_NAMES_PROPERTY_VALUE}.
     *
     * <p>Example usage:
     * <pre>
     *   -Dmicrosphere.artifact-version.manifest-attribute-names=Custom-Version,Implementation-Version
     * </pre>
     */
    public static final String VERSION_ATTRIBUTE_NAMES_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "artifact-version.manifest-attribute-names";

    /**
     * Default priority value for the ManifestArtifactResourceResolver.
     * This priority determines the order in which this resolver is used compared to others.
     * Lower values indicate higher priority.
     */
    public static final int DEFAULT_PRIORITY = 5;

    private static final String[] ARTIFACT_ID_ATTRIBUTE_NAMES = getArtifactIdAttributeNames();

    private static final String[] VERSION_ATTRIBUTE_NAMES = getVersionAttributeNames();

    private static String[] getArtifactIdAttributeNames() {
        return getPropertyValues(ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_NAME, DEFAULT_ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_VALUE);
    }

    private static String[] getVersionAttributeNames() {
        return getPropertyValues(VERSION_ATTRIBUTE_NAMES_PROPERTY_NAME, DEFAULT_VERSION_ATTRIBUTE_NAMES_PROPERTY_VALUE);
    }

    private static String[] getPropertyValues(String propertyName, String defaultValue) {
        String propertyValue = getProperty(propertyName, defaultValue);
        return split(propertyValue, COMMA);
    }


    public ManifestArtifactResourceResolver() {
        this(DEFAULT_PRIORITY);
    }

    public ManifestArtifactResourceResolver(int priority) {
        super(priority);
    }

    public ManifestArtifactResourceResolver(ClassLoader classLoader, int priority) {
        super(classLoader, priority);
    }

    @Override
    protected boolean isArtifactMetadata(String relativePath) {
        return MANIFEST_RESOURCE_PATH.equals(relativePath);
    }

    @Override
    protected Artifact resolve(URL resourceURL, InputStream artifactMetadataData, ClassLoader classLoader) throws IOException {
        Manifest manifest = new Manifest(artifactMetadataData);
        return resolveArtifactMetaInfoInManifest(manifest, resourceURL);
    }

    private Artifact resolveArtifactMetaInfoInManifest(Manifest manifest, URL resourceURL) throws IOException {
        Attributes mainAttributes = manifest.getMainAttributes();
        String artifactId = resolveArtifactId(mainAttributes, resourceURL);
        if (artifactId == null) {
            return null;
        }
        String version = resolveVersion(mainAttributes);
        return create(artifactId, version, resourceURL);
    }

    private String resolveArtifactId(Attributes attributes, URL artifactResourceURL) {
        String artifactId = null;

        for (String artifactIdAttributeName : ARTIFACT_ID_ATTRIBUTE_NAMES) {
            artifactId = attributes.getValue(artifactIdAttributeName);
            if (artifactId != null) {
                break;
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("The artifactId was resolved from the '{}' of resource URL['{}'] of  : {} , attributes : {}",
                    MANIFEST_RESOURCE_PATH,
                    artifactResourceURL.getPath(),
                    artifactId,
                    attributes.entrySet()
            );
        }

        return artifactId;
    }

    private String resolveVersion(Attributes attributes) {
        String version = null;

        for (String versionAttributeName : VERSION_ATTRIBUTE_NAMES) {
            version = attributes.getValue(versionAttributeName);
            if (version != null) {
                break;
            }
        }

        return version;
    }
}
