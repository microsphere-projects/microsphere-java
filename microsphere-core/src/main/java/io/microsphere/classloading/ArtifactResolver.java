package io.microsphere.classloading;

import io.microsphere.lang.Prioritized;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

/**
 * {@link Artifact} Resolver
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ArtifactResolver extends Prioritized {

    URL[] EMPTY_URLS = new URL[0];

    /**
     * Resolve the {@link Artifact artifacts}
     *
     * @param classPathURLs The URLs of Class-Paths
     * @return the non-null read-only {@link Set}
     */
    @Nonnull
    default Set<Artifact> resolve(Collection<URL> classPathURLs) {
        return resolve(classPathURLs.toArray(EMPTY_URLS));
    }

    /**
     * Resolve the {@link Artifact artifacts}
     *
     * @param classPathURLs The URLs of Class-Paths
     * @return the non-null read-only {@link Set}
     */
    @Nonnull
    Set<Artifact> resolve(URL... classPathURLs);
}
