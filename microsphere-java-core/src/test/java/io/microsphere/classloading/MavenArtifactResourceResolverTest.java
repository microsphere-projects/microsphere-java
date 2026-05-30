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
class MavenArtifactResourceResolverTest extends StreamArtifactResourceResolverTest<MavenArtifactResourceResolver> {

    @Override
    void assertArtifact(Artifact artifact) throws Throwable {
        assertTrue(artifact instanceof MavenArtifact);
        MavenArtifact mavenArtifact = (MavenArtifact) artifact;

        assertEquals(TEST_GROUP_ID, mavenArtifact.getGroupId());
        assertEquals(TEST_ARTIFACT_ID, mavenArtifact.getArtifactId());
        assertEquals(TEST_VERSION, mavenArtifact.getVersion());
    }

}