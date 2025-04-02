package io.microsphere.classloading;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Set;

import static io.microsphere.util.ArrayUtils.EMPTY_URL_ARRAY;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Abstract {@link ArtifactResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractArtifactResolver
 * @see ArtifactResolver
 * @since 1.0.0
 */
public abstract class AbstractArtifactResolverTest<A extends AbstractArtifactResolver> extends AbstractTestCase {

    protected A artifactResolver;

    @BeforeEach
    public void init() {
        this.artifactResolver = createArtifactResolver();
    }

    protected abstract A createArtifactResolver();

    protected abstract int getPriority();

    @Test
    public void testResolve() {
        Set<URL> urls = findAllClassPathURLs(classLoader);
        assertNotNull(artifactResolver.resolve(urls));
    }

    @Test
    public void testResolveOnEmptyCollection() {
        assertSame(emptySet(), artifactResolver.resolve(emptySet()));
    }

    @Test
    public void testResolveOnNullCollection() {
        assertSame(emptySet(), artifactResolver.resolve(TEST_NULL_COLLECTION));
    }

    @Test
    public void testResolveWithURLArray() {
        Set<URL> urls = findAllClassPathURLs(classLoader);
        URL[] urlsArray = urls.toArray(EMPTY_URL_ARRAY);
        assertNotNull(artifactResolver.resolve(urlsArray));
    }

    @Test
    public void testResolveWithURLArrayOnEmptyArray() {
        assertSame(emptySet(), artifactResolver.resolve(EMPTY_URL_ARRAY));
    }

    @Test
    public void testResolveWithURLArrayOnNullArray() {
        assertSame(emptySet(), artifactResolver.resolve((URL[]) null));
    }

    @Test
    public void testGetPriority() {
        assertEquals(getPriority(), artifactResolver.getPriority());
    }
}