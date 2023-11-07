package io.microsphere.classloading;

import java.util.Objects;

/**
 * Maven {@link Artifact}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MavenArtifact extends Artifact {

    private String groupId;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
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
        final StringBuilder sb = new StringBuilder("MavenArtifact{");
        sb.append("groupId='").append(groupId).append('\'');
        sb.append(", artifactId='").append(getArtifactId()).append('\'');
        sb.append(", version='").append(getVersion()).append('\'');
        sb.append(", location='").append(getLocation()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
