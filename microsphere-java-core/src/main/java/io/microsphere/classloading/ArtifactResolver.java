package io.microsphere.classloading;

import io.microsphere.annotation.Nonnull;
import io.microsphere.lang.Prioritized;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

import static io.microsphere.util.ArrayUtils.EMPTY_URL_ARRAY;

/**
 * {@link Artifact} Resolver
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ArtifactResolver extends Prioritized {

    /**
     * Resolve the {@link Artifact artifacts}
     *
     * @param classPathURLs The URLs of Class-Paths
     * @return the non-null read-only {@link Set}
     */
    @Nonnull
    default Set<Artifact> resolve(Collection<URL> classPathURLs) {
        return resolve(classPathURLs.toArray(EMPTY_URL_ARRAY));
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
