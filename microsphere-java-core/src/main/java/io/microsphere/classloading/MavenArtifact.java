package io.microsphere.classloading;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import java.net.URL;
import java.util.Objects;

/**
 * Maven {@link Artifact}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MavenArtifact extends Artifact {

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
        return Objects.hash(super.hashCode(), groupId);
    }

    @Override
    public String toString() {
        String sb = "MavenArtifact{" + "groupId='" + groupId + '\'' +
                ", artifactId='" + getArtifactId() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", location='" + getLocation() + '\'' +
                '}';
        return sb;
    }
}
