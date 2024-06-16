package io.microsphere.classloading;

import io.microsphere.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.constants.SymbolConstants.COLON;
import static io.microsphere.util.ClassLoaderUtils.removeClassPathURL;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.split;

/**
 * The executor for the banned artifacts that are loading by {@link ClassLoader}.
 * <p>
 * The banned list should be defined at the config resource that locates on the "META-INF/banned-artifacts" was loaded by
 * {@link ClassLoader}.
 * <p>
 * The config resource format :
 * <pre>${groupId}:${artifactId}:${version}</pre>
 *
 * <ul>
 *     <li>groupId  :  Artifact Maven groupId</li>
 *     <li>artifactId : Artifact Maven artifactId</li>
 *     <li>version : Artifact Maven version</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class BannedArtifactClassLoadingExecutor {

    public static final String CONFIG_LOCATION = "META-INF/banned-artifacts";

    private static final Logger logger = LoggerFactory.getLogger(BannedArtifactClassLoadingExecutor.class);

    private static final String ENCODING = SystemUtils.FILE_ENCODING;

    private final ClassLoader classLoader;

    private final ArtifactDetector artifactDetector;

    public BannedArtifactClassLoadingExecutor() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public BannedArtifactClassLoadingExecutor(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.artifactDetector = new ArtifactDetector(classLoader);
    }

    public void execute() {
        List<MavenArtifact> bannedArtifactConfigs = loadBannedArtifactConfigs();
        List<Artifact> artifacts = artifactDetector.detect(false);
        for (Artifact artifact : artifacts) {
            URL classPathURL = artifact.getLocation();
            if (classPathURL != null) {
                for (MavenArtifact bannedArtifactConfig : bannedArtifactConfigs) {
                    if (bannedArtifactConfig.matches(artifact)) {
                        removeClassPathURL(classLoader, classPathURL);
                    }
                }
            }
        }
    }

    private List<MavenArtifact> loadBannedArtifactConfigs() {
        List<MavenArtifact> bannedArtifactConfigs = new LinkedList<>();
        try {
            Enumeration<URL> configResources = classLoader.getResources(CONFIG_LOCATION);
            while (configResources.hasMoreElements()) {
                URL configResource = configResources.nextElement();
                List<MavenArtifact> configs = loadBannedArtifactConfigs(configResource);
                bannedArtifactConfigs.addAll(configs);
            }
        } catch (IOException e) {
            logger.error("The banned artifacts config resource[{}] can't be read", CONFIG_LOCATION, e);
        }
        return bannedArtifactConfigs;
    }

    private List<MavenArtifact> loadBannedArtifactConfigs(URL configResource) throws IOException {
        List<MavenArtifact> bannedArtifactConfigs = new LinkedList<>();
        try (InputStream inputStream = configResource.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING))
        ) {
            while (true) {
                String definition = reader.readLine();
                if (isBlank(definition)) {
                    break;
                }
                MavenArtifact bannedArtifactConfig = loadBannedArtifactConfig(definition);
                bannedArtifactConfigs.add(bannedArtifactConfig);
            }
        }
        return bannedArtifactConfigs;
    }

    /**
     * @param definition
     * @return
     */
    private MavenArtifact loadBannedArtifactConfig(String definition) {
        String[] gav = split(definition.trim(), COLON);
        if (gav.length != 3) {
            throw new RuntimeException("The definition of the banned artifact must contain groupId, artifactId and version : " + definition);
        }
        String groupId = gav[0];
        String artifactId = gav[1];
        String version = gav[2];
        return MavenArtifact.create(groupId, artifactId, version);
    }

}
