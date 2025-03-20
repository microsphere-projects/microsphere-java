package io.microsphere.classloading;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.ClassUtils;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassPathUtils.getBootstrapClassPaths;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * {@link Artifact} Detector
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactDetector {

    private static final Logger logger = getLogger(ArtifactDetector.class);

    private static final String JAVA_HOME_PATH = normalizePath(getProperty("java.home"));

    private final ClassLoader classLoader;

    private final List<ArtifactResolver> artifactResolvers;

    public ArtifactDetector() {
        this(null);
    }

    public ArtifactDetector(@Nullable ClassLoader classLoader) {
        ClassLoader actualClassLoader = classLoader == null ? getClassLoader(getClass()) : classLoader;
        List<ArtifactResolver> artifactResolvers = loadServicesList(ArtifactResolver.class, actualClassLoader, true);
        this.classLoader = actualClassLoader;
        this.artifactResolvers = artifactResolvers;
        if (logger.isTraceEnabled()) {
            logger.trace("ClassLoader[argument : {} , actual : {}] , ArtifactResolver List : {}", classLoader, actualClassLoader, getArtifactResolverClassNames(artifactResolvers));
        }
    }

    public List<Artifact> detect() {
        return detect(true);
    }

    public List<Artifact> detect(boolean includedJdkLibraries) {
        Set<URL> classPathURLs = getClassPathURLs(includedJdkLibraries);
        return detect(classPathURLs);
    }

    protected List<Artifact> detect(Set<URL> classPathURLs) {
        if (isEmpty(classPathURLs)) {
            return emptyList();
        }

        List<Artifact> artifactList = new LinkedList<>();
        for (ArtifactResolver artifactResolver : artifactResolvers) {
            Set<Artifact> artifactSet = artifactResolver.resolve(classPathURLs);
            for (Artifact artifact : artifactSet) {
                artifactList.add(artifact);
                classPathURLs.remove(artifact.getLocation());
            }
        }
        return unmodifiableList(artifactList);
    }

    protected Set<URL> getClassPathURLs(boolean includedJdkLibraries) {
        Set<URL> urls = findAllClassPathURLs(classLoader);
        Set<URL> classPathURLs = new LinkedHashSet<>(urls);
        if (!includedJdkLibraries) {
            removeJdkClassPathURLs(classPathURLs);
        }
        if (logger.isTraceEnabled()) {
            StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
            for (URL classPathURL : classPathURLs) {
                stringJoiner.add(classPathURL.toString());
            }
            logger.trace("ClassLoader[{}] covers the URLs[expected: {}, actual: {}], class-path : {}",
                    classLoader, urls.size(), classPathURLs.size(), stringJoiner);
        }
        return classPathURLs;
    }

    private void removeJdkClassPathURLs(Set<URL> classPathURLs) {
        // Remove the URLs of Bootstrap Class-Path
        Set<String> bootstrapClassPaths = getBootstrapClassPaths();
        Iterator<URL> iterator = classPathURLs.iterator();
        while (iterator.hasNext()) {
            URL url = iterator.next();
            String path = url.getPath();
            if (bootstrapClassPaths.contains(path) || path.contains(JAVA_HOME_PATH)) {
                iterator.remove();
            }
        }
    }

    private List<String> getArtifactResolverClassNames(List<ArtifactResolver> artifactResolvers) {
        return artifactResolvers.stream()
                .map(Object::getClass)
                .map(ClassUtils::getTypeName)
                .collect(Collectors.toList());
    }

}
