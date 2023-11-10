package io.microsphere.classloading;

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
import java.util.function.Function;

import static io.microsphere.constants.SymbolConstants.COLON;
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

    private static final String ENCODING = System.getProperty("file.encoding", "UTF-8");

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
        List<BannedArtifactConfig> bannedArtifactConfigs = loadBannedArtifactConfigs();
        List<Artifact> artifacts = artifactDetector.detect(false);

    }

    private List<BannedArtifactConfig> loadBannedArtifactConfigs() {
        List<BannedArtifactConfig> bannedArtifactConfigs = new LinkedList<>();
        try {
            Enumeration<URL> configResources = classLoader.getResources(CONFIG_LOCATION);
            while (configResources.hasMoreElements()) {
                URL configResource = configResources.nextElement();
                List<BannedArtifactConfig> configs = loadBannedArtifactConfigs(configResource);
                bannedArtifactConfigs.addAll(configs);
            }
        } catch (IOException e) {
            logger.error("The banned artifacts config resource[{}] can't be read", CONFIG_LOCATION, e);
        }
        return bannedArtifactConfigs;
    }

    private List<BannedArtifactConfig> loadBannedArtifactConfigs(URL configResource) throws IOException {
        List<BannedArtifactConfig> bannedArtifactConfigs = new LinkedList<>();
        try (InputStream inputStream = configResource.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
        ) {
            while (true) {
                String definition = reader.readLine();
                if (isBlank(definition)) {
                    break;
                }
                BannedArtifactConfig bannedArtifactConfig = loadBannedArtifactConfig(definition);
                bannedArtifactConfigs.add(bannedArtifactConfig);
            }
        }
        return bannedArtifactConfigs;
    }

    /**
     * @param definition
     * @return
     */
    private BannedArtifactConfig loadBannedArtifactConfig(String definition) {
        String[] gav = split(definition.trim(), COLON);
        if (gav.length != 3) {
            throw new RuntimeException("The definition of the banned artifact must contain groupId, artifactId and version : " + definition);
        }
        String groupId = gav[0];
        String artifactId = gav[1];
        String version = gav[2];
        BannedArtifactConfig bannedArtifactConfig = new BannedArtifactConfig();
        bannedArtifactConfig.setGroupId(groupId);
        bannedArtifactConfig.setArtifactId(artifactId);
        bannedArtifactConfig.setVersion(version);
        return bannedArtifactConfig;
    }

    static class BannedArtifactConfig extends MavenArtifact {

        private static final String WILDCARD = "*";

        public boolean matches(Artifact artifact) {
            return matchesGroupId(artifact)
                    && matchesArtifactId(artifact)
                    && matchesVersion(artifact);
        }

        private boolean matchesGroupId(Artifact artifact) {
            return matches(artifact, this::getGroupId);
        }

        private boolean matchesArtifactId(Artifact artifact) {
            return matches(artifact, Artifact::getArtifactId);
        }

        private boolean matchesVersion(Artifact artifact) {
            return matches(artifact, Artifact::getVersion);
        }

        private boolean matches(Artifact artifact, Function<Artifact, String> getterFunction) {
            String configuredValue = getterFunction.apply(this);
            if (WILDCARD.equals(configuredValue)) {
                return true;
            }
            String value = getterFunction.apply(artifact);
            return configuredValue.equals(value);
        }

        private String getGroupId(Artifact artifact) {
            if (artifact instanceof MavenArtifact) {
                return ((MavenArtifact) artifact).getGroupId();
            }
            return null;
        }
    }
}
