package io.microsphere.classloading;

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
    protected void assertArtifactFromFile(Artifact artifact) throws Throwable {
        assertNull(artifact);
    }
}