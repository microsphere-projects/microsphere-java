package io.microsphere.classloading;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassPathUtils.getBootstrapClassPaths;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static io.microsphere.util.SystemUtils.JAVA_HOME;
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

    private static final String JAVA_HOME_PATH = JAVA_HOME;

    final ClassLoader classLoader;

    private final List<ArtifactResourceResolver> artifactResourceResolvers;

    public ArtifactDetector() {
        this(null);
    }

    public ArtifactDetector(@Nullable ClassLoader classLoader) {
        ClassLoader actualClassLoader = classLoader == null ? getClassLoader(getClass()) : classLoader;
        this.artifactResourceResolvers = loadServicesList(ArtifactResourceResolver.class, actualClassLoader, true);
        this.classLoader = actualClassLoader;
        if (logger.isTraceEnabled()) {
            logger.trace("ClassLoader[argument : {} , actual : {}] , ArtifactResolver List : {}",
                    classLoader, actualClassLoader, this.artifactResourceResolvers);
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
        for (URL resourceURL : classPathURLs) {
            for (ArtifactResourceResolver artifactResourceResolver : artifactResourceResolvers) {
                Artifact artifact = artifactResourceResolver.resolve(resourceURL);
                if (artifact != null) {
                    artifactList.add(artifact);
                    break;
                }
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
            logger.trace("ClassLoader[{}] covers the URLs[expected: {}, actual: {}], class-path : {}",
                    classLoader, urls.size(), classPathURLs.size(), classPathURLs);
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

}
