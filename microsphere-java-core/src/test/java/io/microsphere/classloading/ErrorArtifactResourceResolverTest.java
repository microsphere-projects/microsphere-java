package io.microsphere.classloading;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ErrorArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ErrorArtifactResourceResolver
 * @since 1.0.0
 */
public class ErrorArtifactResourceResolverTest extends AbstractArtifactResourceResolverTest<ErrorArtifactResourceResolver> {

    @Override
    protected void assertArtifact(Artifact artifact) throws Throwable {
        assertNull(artifact);
    }

    @Test
    public void testIsArtifactMetadata() {
        assertFalse(this.resolver.isArtifactMetadata(null));
    }
}