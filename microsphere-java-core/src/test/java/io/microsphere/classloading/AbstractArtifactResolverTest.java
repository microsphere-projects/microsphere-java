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
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private AbstractArtifactResolver artifactResolver;

    private static final Set<URL> TEST_URLS = findAllClassPathURLs(TEST_CLASS_LOADER);

    private static final URL[] TEST_URL_ARRAY = TEST_URLS.toArray(new URL[0]);

    @BeforeEach
    public void init() {
        this.artifactResolver = createArtifactResolver();
    }

    protected abstract A createArtifactResolver();

    protected abstract int getPriority();

    @Test
    public void testResolve() {
        assertFalse(artifactResolver.resolve(TEST_URLS).isEmpty());
    }

    @Test
    public void testResolveOnEmptyCollection() {
        assertSame(emptySet(), artifactResolver.resolve(emptySet()));
    }

    @Test
    public void testResolveWithURLArray() {
        assertFalse(artifactResolver.resolve(TEST_URL_ARRAY).isEmpty());
    }

    @Test
    public void testResolveWithURLArrayOnEmptyArray() {
        assertSame(emptySet(), artifactResolver.resolve(EMPTY_URL_ARRAY));
    }

    @Test
    public void testGetPriority() {
        assertEquals(getPriority(), artifactResolver.getPriority());
    }
}