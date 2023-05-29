package io.github.microsphere.classloading;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * The detector to find the artifacts' collision resources
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactCollisionResourceDetector {

    public static final String CONFIG_LOCATION_PATTERN = "META-INF/artifacts-collision.json";

    public static final String ARTIFACT_MAVEN_POM_PROPERTIES_RESOURCE_PATTERN = "META-INF/maven/%s/%s/pom.properties";

    private static final String GROUP_ID_PROPERTY_NAME = "groupId";

    private static final String ARTIFACT_ID_PROPERTY_NAME = "artifactId";

    private static final String VERSION_PROPERTY_NAME = "version";

    private static final Logger logger = LoggerFactory.getLogger(ArtifactCollisionResourceDetector.class);

    private final ClassLoader classLoader;

    public ArtifactCollisionResourceDetector() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ArtifactCollisionResourceDetector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Map<URL, String> detect() {
        Map<URL, String> collisionResources = new LinkedHashMap<>();
        try {
            Enumeration<URL> configResources = classLoader.getResources(CONFIG_LOCATION_PATTERN);
            while (configResources.hasMoreElements()) {
                URL configResource = configResources.nextElement();
                collisionResources.putAll(detect(configResource));
            }
        } catch (IOException e) {
            logger.error("The artifacts collision config resource[{}] can't be read", CONFIG_LOCATION_PATTERN, e);
        }
        if (!collisionResources.isEmpty()) {
            logger.debug("The artifacts collision was found：{}", collisionResources);
        }
        return collisionResources;
    }

    /**
     * JSON config resource format :
     * <pre>{@code
     *
     * {
     *   "io.github.microsphere-projects": {
     *     "microsphere-core": "*",
     *   }
     * }
     * }
     * </pre>
     *
     * <ul>
     *     <li>The first level key :  Artifact Maven groupId</li>
     *     <li>The second level key : Artifact Maven artifactId</li>
     *     <li>The second level value : Artifact Maven version</li>
     * </ul>
     *
     * @param configResource JSON config resource
     * @return a {@link Map} with artifact {@link URL} as Key and maven GAV Info as Value
     */
    protected Map<URL, String> detect(URL configResource) {
        Map<URL, String> collisionResources = new LinkedHashMap<>();
        try (InputStream inputStream = configResource.openStream()) {
            Map<String, Map<String, String>> config = JSON.parseObject(inputStream, Map.class);
            for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
                String groupId = entry.getKey();
                for (Map.Entry<String, String> artifactEntry : entry.getValue().entrySet()) {
                    String artifactId = artifactEntry.getKey();
                    String artifactPomPropertiesResource = resolveArtifactPomPropertiesResource(groupId, artifactId);
                    URL artifactPomPropertiesResourceURL = classLoader.getResource(artifactPomPropertiesResource);
                    if (artifactPomPropertiesResourceURL != null) {
                        try (InputStream artifactPomPropertiesStream = artifactPomPropertiesResourceURL.openStream()) {
                            Properties properties = new Properties();
                            properties.load(artifactPomPropertiesStream);
                            String artifactResourcePath = StringUtils.substringBefore(artifactPomPropertiesResourceURL.getPath(), "!/");
                            URL artifactResource = new URL(artifactResourcePath);
                            String extension = getExtension(artifactResourcePath);
                            String mavenGAV = buildMavenGAV(properties, extension);
                            collisionResources.put(artifactResource, mavenGAV);
                        } catch (IOException e) {
                            logger.error("Failed to load the collision artifact resources : {}", artifactPomPropertiesResourceURL, e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load the collision artifact config : {}", configResource, e);
        }

        return collisionResources;
    }

    private String getExtension(String artifactResourcePath) {
        int index = artifactResourcePath.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        String extension = artifactResourcePath.substring(index + 1);
        return extension;
    }

    private String buildMavenGAV(Properties properties, String extension) {
        StringJoiner stringJoiner = new StringJoiner(":")
                .add(properties.getProperty(GROUP_ID_PROPERTY_NAME))
                .add(properties.getProperty(ARTIFACT_ID_PROPERTY_NAME))
                .add(extension)
                .add(properties.getProperty(VERSION_PROPERTY_NAME));
        return stringJoiner.toString();
    }

    private String resolveArtifactPomPropertiesResource(String groupId, String artifactId) {
        return String.format(ARTIFACT_MAVEN_POM_PROPERTIES_RESOURCE_PATTERN, groupId, artifactId);
    }

}
