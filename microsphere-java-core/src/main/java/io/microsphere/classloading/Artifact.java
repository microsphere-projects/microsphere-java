package io.microsphere.classloading;

import io.microsphere.constants.SymbolConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

/**
 * Artifact entity
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Artifact {

    public static final String WILDCARD = SymbolConstants.WILDCARD;

    public static final String UNKNOWN = SymbolConstants.QUESTION_MARK;

    private final String artifactId;

    private final String version;

    private final URL location;

    public Artifact(@Nonnull String artifactId, @Nullable String version, @Nullable URL location) {
        this.artifactId = artifactId;
        this.version = version;
        this.location = location;
    }

    public static Artifact create(@Nonnull String artifactId, @Nullable String version, @Nullable URL location) {
        return new Artifact(artifactId, version, location);
    }

    public static Artifact create(@Nonnull String artifactId, @Nullable String version) {
        return create(artifactId, version, null);
    }

    public static Artifact create(@Nonnull String artifactId) {
        return create(artifactId, UNKNOWN);
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public URL getLocation() {
        return location;
    }

    public boolean matches(Artifact artifact) {
        return matchesArtifactId(artifact)
                && matchesVersion(artifact);
    }

    protected boolean matchesArtifactId(Artifact artifact) {
        return matches(artifact, Artifact::getArtifactId);
    }

    protected boolean matchesVersion(Artifact artifact) {
        return matches(artifact, Artifact::getVersion);
    }

    protected boolean matches(Artifact artifact, Function<Artifact, String> getterFunction) {
        String configuredValue = getterFunction.apply(this);
        if (WILDCARD.equals(configuredValue)) {
            return true;
        }
        String value = getterFunction.apply(artifact);
        return configuredValue.equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artifact)) return false;
        Artifact that = (Artifact) o;
        return Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, version, location);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Artifact{");
        sb.append("artifactId='").append(artifactId).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", location='").append(location).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
