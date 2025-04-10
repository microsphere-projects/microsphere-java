package io.microsphere.classloading;


import io.microsphere.annotation.ConfigurationProperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static io.microsphere.constants.FileConstants.JAR_EXTENSION;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.net.URLUtils.isArchiveURL;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.StringUtils.split;
import static java.lang.System.getProperty;

/**
 * The class {@link ArtifactResolver} based on the resource "META-INF/MANIFEST.MF"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ManifestArtifactResolver extends AbstractArtifactResolver {

    public static final String MANIFEST_RESOURCE_PATH = "META-INF/MANIFEST.MF";

    public static final String DEFAULT_ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_VALUE = "Bundle-Name,Automatic-Module-Name,Implementation-Title";

    public static final String DEFAULT_VERSION_ATTRIBUTE_NAMES_PROPERTY_VALUE = "Bundle-Version,Implementation-Version";

    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_VALUE,
            description = "The attribute names in the 'META-INF/MANIFEST' resource are retrieved as the artifact id"
    )
    public static final String ARTIFACT_ID_ATTRIBUTE_NAMES_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "artifact-id.manifest-attribute-names";

    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_VERSION_ATTRIBUTE_NAMES_PROPERTY_VALUE,
            description = "The attribute names in the 'META-INF/MANIFEST' resource are retrieved as the artifact version"
    )
    public static final String VERSION_ATTRIBUTE_NAMES_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "artifact-version.manifest-attribute-names";

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

    public static final int DEFAULT_PRIORITY = 2;

    public ManifestArtifactResolver() {
        setPriority(DEFAULT_PRIORITY);
    }

    @Override
    protected void doResolve(Collection<Artifact> artifactSet, URLClassLoader urlClassLoader) {
        try {
            Enumeration<URL> manifestResourceURLs = urlClassLoader.getResources(MANIFEST_RESOURCE_PATH);
            while (manifestResourceURLs.hasMoreElements()) {
                Artifact artifact = resolveArtifactMetaInfoInManifest(manifestResourceURLs.nextElement());
                if (artifact != null) {
                    artifactSet.add(artifact);
                }
            }
        } catch (IOException e) {
            logger.error("{} can't be resolved", MANIFEST_RESOURCE_PATH, e);
        }
    }

    private Artifact resolveArtifactMetaInfoInManifest(URL manifestResourceURL) {
        Artifact artifact = null;
        try (InputStream inputStream = manifestResourceURL.openStream()) {
            Manifest manifest = new Manifest(inputStream);
            artifact = resolveArtifactMetaInfoInManifest(manifest, manifestResourceURL);
        } catch (IOException e) {
            logger.error("{}[Path : {}] can't be resolved", MANIFEST_RESOURCE_PATH, manifestResourceURL.getPath(), e);
        }
        return artifact;
    }

    private Artifact resolveArtifactMetaInfoInManifest(Manifest manifest, URL manifestResourceURL)
            throws MalformedURLException {
        Attributes mainAttributes = manifest.getMainAttributes();
        URL artifactResourceURL = resolveArtifactResourceURL(manifestResourceURL);
        if (artifactResourceURL == null) {
            return null;
        }

        boolean isArchiveURL = isArchiveURL(artifactResourceURL);
        String artifactId = resolveArtifactId(mainAttributes, artifactResourceURL, isArchiveURL);
        if (artifactId == null) {
            return null;
        }
        String version = resolveVersion(mainAttributes, artifactId, artifactResourceURL, isArchiveURL);
        return Artifact.create(artifactId, version, artifactResourceURL);
    }

    private String resolveArtifactId(Attributes attributes, URL artifactResourceURL, boolean isArchiveURL) {
        String artifactId = null;

        for (String artifactIdAttributeName : ARTIFACT_ID_ATTRIBUTE_NAMES) {
            artifactId = attributes.getValue(artifactIdAttributeName);
            if (artifactId != null) {
                break;
            }
        }

        if (artifactId == null && isArchiveURL) {
            artifactId = resolveArtifactId(artifactResourceURL);
        }

        if (artifactId == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The artifactId can't be resolved from the {} of artifact[resource: {}] : {}",
                        artifactResourceURL.getPath(),
                        MANIFEST_RESOURCE_PATH,
                        attributes.entrySet()
                );
            }
        }

        return artifactId;
    }

    private String resolveArtifactId(URL artifactResourceURL) {
        String path = artifactResourceURL.getPath();
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex < 0) {
            return null;
        }
        int fileExtensionIndex = path.indexOf(JAR_EXTENSION);
        if (fileExtensionIndex < lastSlashIndex) {
            return null;
        }
        String jarFileName = path.substring(lastSlashIndex + 1, fileExtensionIndex);
        int lastHyphenIndex = jarFileName.lastIndexOf('-');
        if (lastHyphenIndex < 0) {
            return jarFileName;
        }
        return jarFileName.substring(0, lastHyphenIndex);
    }

    private String resolveVersion(Attributes attributes, String artifactId, URL artifactResourceURL, boolean isArchiveURL) {
        String version = null;

        for (String versionAttributeName : VERSION_ATTRIBUTE_NAMES) {
            version = attributes.getValue(versionAttributeName);
            if (version != null) {
                break;
            }
        }

        if (version == null && isArchiveURL) {
            version = resolveVersion(artifactId, artifactResourceURL);
            if (version == null) {
                version = resolveVersion(resolveArtifactId(artifactResourceURL), artifactResourceURL);
            }
        }

        if (version == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The version can't be found in the {} of artifact[Path: {}]", MANIFEST_RESOURCE_PATH,
                        artifactResourceURL.getPath()
                );
            }
        }

        return version;
    }

    private String resolveVersion(String artifactId, URL artifactResourceURL) {
        String path = artifactResourceURL.getPath();

        int lastArtifactIdIndex = path.lastIndexOf(artifactId);
        if (lastArtifactIdIndex < 0) {
            return null;
        }

        int beginIndex = lastArtifactIdIndex + artifactId.length() + 1;

        int fileExtensionIndex = path.indexOf(JAR_EXTENSION);
        if (fileExtensionIndex <= beginIndex) {
            return null;
        }

        return path.substring(beginIndex, fileExtensionIndex);
    }
}
