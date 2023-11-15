package io.microsphere.classloading;

import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.jar.JarUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.net.URLUtils.isJarURL;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.jar.JarUtils.toJarFile;

/**
 * Maven {@link ArtifactResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MavenArtifactResolver extends AbstractArtifactResolver {

    private static final String MAVEN_POM_PROPERTIES_RESOURCE_PREFIX = "META-INF/maven/";

    private static final String MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX = "/pom.properties";

    private static final MavenPomPropertiesFilter MAVEN_POM_PROPERTIES_FILTER = new MavenPomPropertiesFilter();

    private static final String GROUP_ID_PROPERTY_NAME = "groupId";

    private static final String ARTIFACT_ID_PROPERTY_NAME = "artifactId";

    private static final String VERSION_PROPERTY_NAME = "version";

    public static final int DEFAULT_PRIORITY = 1;

    public MavenArtifactResolver() {
        setPriority(DEFAULT_PRIORITY);
    }

    @Override
    protected void doResolve(Collection<Artifact> artifactSet, URLClassLoader urlClassLoader) {

        Set<URL> classPathURLs = findAllClassPathURLs(urlClassLoader);

        for (URL classPathURL : classPathURLs) {
            URL mavenPomPropertiesResource = null;
            try {
                mavenPomPropertiesResource = findMavenPomPropertiesResource(classPathURL, urlClassLoader);
                if (mavenPomPropertiesResource != null) {
                    Artifact artifact = resolveArtifactMetaInfoInMavenPomProperties(mavenPomPropertiesResource);
                    if (artifact != null) {
                        artifactSet.add(artifact);
                        if (logger.isDebugEnabled()) {
                            logger.debug("The artifact was resolved from the the Maven pom.properties[resource : {}] : {}", mavenPomPropertiesResource, artifact);
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("The artifact[class-path : {}] can't be open.", e);
            }
        }
    }

    private URL findMavenPomPropertiesResource(URL classPathURL, URLClassLoader urlClassLoader) throws IOException {
        if (isJarURL(classPathURL)) {
            return findMavenPomPropertiesResourceInJar(classPathURL, urlClassLoader);
        }
        return null;
    }

    private URL findMavenPomPropertiesResourceInJar(URL classPathURL, URLClassLoader urlClassLoader) throws IOException {
        JarFile jarFile = toJarFile(classPathURL);
        List<JarEntry> entries = JarUtils.filter(jarFile, MAVEN_POM_PROPERTIES_FILTER);
        if (entries.isEmpty()) {
            return null;
        }
        JarEntry jarEntry = entries.get(0);
        String relativePath = jarEntry.getName();
        return urlClassLoader.getResource(relativePath);
    }

    private Artifact resolveArtifactMetaInfoInMavenPomProperties(URL mavenPomPropertiesResourceURL) {
        Artifact artifact = null;
        try (InputStream mavenPomPropertiesStream = mavenPomPropertiesResourceURL.openStream()) {
            Properties properties = new Properties();
            properties.load(mavenPomPropertiesStream);
            URL artifactResourceURL = resolveArtifactResourceURL(mavenPomPropertiesResourceURL);
            artifact = resolveArtifactMetaInfoInMavenPomProperties(properties, artifactResourceURL);
        } catch (IOException e) {
            logger.error("The Maven artifact pom.properties[resource : {}] can't be resolved", mavenPomPropertiesResourceURL, e);
        }
        return artifact;
    }

    private Artifact resolveArtifactMetaInfoInMavenPomProperties(Properties properties,
                                                                 URL artifactResourceURL) {
        String groupId = properties.getProperty(GROUP_ID_PROPERTY_NAME);
        String artifactId = properties.getProperty(ARTIFACT_ID_PROPERTY_NAME);
        String version = properties.getProperty(VERSION_PROPERTY_NAME);
        MavenArtifact artifactMetaInfo = new MavenArtifact();
        artifactMetaInfo.setGroupId(groupId);
        artifactMetaInfo.setArtifactId(artifactId);
        artifactMetaInfo.setVersion(version);
        artifactMetaInfo.setLocation(artifactResourceURL);
        return artifactMetaInfo;
    }

    private static class MavenPomPropertiesFilter implements JarEntryFilter {

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
