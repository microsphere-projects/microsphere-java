package io.microsphere.classloading;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ErrorArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ErrorArtifactResourceResolver
 * @since 1.0.0
 */
public class ErrorArtifactResourceResolverTest extends AbstractArtifactResourceResolverTest<ErrorArtifactResourceResolver> {

    @Override
    protected void testResolve(ErrorArtifactResourceResolver resolver) throws Throwable {
        assertThrows(RuntimeException.class, () -> resolver.resolve(null));
    }
}