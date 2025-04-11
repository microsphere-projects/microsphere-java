package io.microsphere.classloading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MavenArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MavenArtifactResourceResolver
 * @since 1.0.0
 */
public class MavenArtifactResourceResolverTest extends AbstractArtifactResourceResolverTest<MavenArtifactResourceResolver> {

    @Override
    protected void assertArtifactFromFile(Artifact artifact) throws Throwable {
        assertTrue(artifact instanceof MavenArtifact);
        MavenArtifact mavenArtifact = (MavenArtifact) artifact;

        assertEquals("com.google.code.findbugs", mavenArtifact.getGroupId());
        assertEquals("jsr305", mavenArtifact.getArtifactId());
        assertEquals("3.0.2", mavenArtifact.getVersion());
    }

}