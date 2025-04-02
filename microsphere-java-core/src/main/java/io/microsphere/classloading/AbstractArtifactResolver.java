package io.microsphere.classloading;

import io.microsphere.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ClassLoaderUtils.newURLClassLoader;
import static java.util.Collections.emptySet;

/**
 * Abstract {@link ArtifactResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractArtifactResolver implements ArtifactResolver {

    protected final Logger logger = getLogger(getClass());

    private int priority;

    @Override
    public final Set<Artifact> resolve(URL... classPathURLs) {
        if (isEmpty(classPathURLs)) {
            return emptySet();
        }
        URLClassLoader urlClassLoader = newURLClassLoader(classPathURLs);
        Set<Artifact> artifactSet = new LinkedHashSet<>(classPathURLs.length);
        doResolve(artifactSet, urlClassLoader);
        return artifactSet;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    protected abstract void doResolve(Collection<Artifact> artifactSet, URLClassLoader urlClassLoader);

    protected URL resolveArtifactResourceURL(URL resourceURL) {
        URL url = null;
        try {
            File archiveFile = resolveArchiveFile(resourceURL);
            if (archiveFile != null) {
                url = archiveFile.toURI().toURL();
            }
        } catch (IOException e) {
            logger.error("The resource [url : {}] can't resolve the target artifact", resourceURL, e);
        }
        return url;
    }
}
