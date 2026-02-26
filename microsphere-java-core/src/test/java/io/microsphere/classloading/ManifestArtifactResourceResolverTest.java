package io.microsphere.classloading;

import org.junit.jupiter.api.Test;

import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ManifestArtifactResourceResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ManifestArtifactResourceResolver
 * @since 1.0.0
 */
class ManifestArtifactResourceResolverTest extends StreamArtifactResourceResolverTest<ManifestArtifactResourceResolver> {

    @Override
    void assertArtifact(Artifact artifact) {
        assertEquals("FindBugs-jsr305", artifact.getArtifactId());
        assertEquals(TEST_VERSION, artifact.getVersion());
    }

    @Test
    void testresolveArtifactMetaInfoInManifestOnNotFound() {
        ManifestArtifactResourceResolver resolver = this.resolver;
        Manifest manifest = new Manifest();
        Artifact artifact = resolver.resolveArtifactMetaInfoInManifest(manifest, null);
        assertNull(artifact);
    }
}