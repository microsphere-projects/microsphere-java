package io.microsphere.classloading;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.net.URL;
import java.util.Objects;

import static io.microsphere.constants.SymbolConstants.QUOTE_CHAR;
import static java.util.Objects.hash;

/**
 * Represents a Maven software artifact with attributes such as group ID, artifact ID, version, and location.
 * This class extends the basic {@link Artifact} by adding Maven-specific identification through group ID.
 * It supports matching artifacts based on group ID, in addition to the properties defined in the parent class.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a Maven artifact with group ID, artifact ID, and default version (UNKNOWN)
 * MavenArtifact mavenArtifact1 = MavenArtifact.create("com.example", "my-artifact");
 *
 * // Create a Maven artifact with group ID, artifact ID, and specific version
 * MavenArtifact mavenArtifact2 = MavenArtifact.create("com.example", "my-artifact", "1.0.0");
 *
 * // Create a Maven artifact with group ID, artifact ID, version, and location
 * URL location = new URL("http://example.com/artifact.jar");
 * MavenArtifact mavenArtifact3 = MavenArtifact.create("com.example", "my-artifact", "1.0.0", location);
 *
 * // Matching Maven artifacts based on group ID, artifact ID, and version
 * boolean isMatch = mavenArtifact1.matches(mavenArtifact2); // returns false
 *  }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Artifact
 * @see ArtifactResourceResolver
 * @since 1.0.0
 */
public class MavenArtifact extends Artifact {

    @Nonnull
    private final String groupId;

    public MavenArtifact(@Nonnull String groupId, @Nonnull String artifactId, @Nullable String version, @Nullable URL location) {
        super(artifactId, version, location);
        this.groupId = groupId;
    }

    public static MavenArtifact create(@Nonnull String groupId, @Nonnull String artifactId,
                                       @Nullable String version, @Nullable URL location) {
        return new MavenArtifact(groupId, artifactId, version, location);
    }

    public static MavenArtifact create(@Nonnull String groupId, @Nonnull String artifactId,
                                       @Nullable String version) {
        return create(groupId, artifactId, version, null);
    }

    public static MavenArtifact create(@Nonnull String groupId, @Nonnull String artifactId) {
        return create(groupId, artifactId, UNKNOWN);
    }

    /**
     * Get the group id of Maven Artifact
     *
     * @return non-null
     */
    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    @Override
    public boolean matches(Artifact artifact) {
        return matchesGroupId(artifact)
                && super.matches(artifact);
    }

    private boolean matchesGroupId(Artifact artifact) {
        return matches(artifact, this::getGroupId);
    }

    private String getGroupId(Artifact artifact) {
        if (artifact instanceof MavenArtifact) {
            return ((MavenArtifact) artifact).getGroupId();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MavenArtifact)) return false;
        if (!super.equals(o)) return false;
        MavenArtifact that = (MavenArtifact) o;
        return Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return hash(super.hashCode(), groupId);
    }

    @Override
    public String toString() {
        String sb = "MavenArtifact{" + "groupId='" + groupId + QUOTE_CHAR +
                ", artifactId='" + getArtifactId() + QUOTE_CHAR +
                ", version='" + getVersion() + QUOTE_CHAR +
                ", location='" + getLocation() + QUOTE_CHAR +
                '}';
        return sb;
    }
}
