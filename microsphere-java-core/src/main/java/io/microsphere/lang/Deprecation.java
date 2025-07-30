/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.lang;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.util.Version;

import java.io.Serializable;
import java.util.Objects;

import static io.microsphere.constants.SymbolConstants.QUOTE_CHAR;
import static io.microsphere.lang.Deprecation.Level.DEFAULT;
import static java.util.Objects.hash;

/**
 * A serializable class that provides detailed information about deprecation.
 * <p>
 * This class is used to indicate that a specific API or component is deprecated, and optionally provides details such as:
 * <ul>
 *     <li>The version since when it became deprecated</li>
 *     <li>A suggested replacement (if available)</li>
 *     <li>The reason for deprecation</li>
 *     <li>A link to further documentation or migration guide</li>
 *     <li>The deprecation level, e.g., whether it's marked for removal</li>
 * </ul>
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a simple deprecation notice with the version string
 * Deprecation deprecation = Deprecation.of("1.2.0");
 *
 * // Create a deprecation notice with replacement and reason
 * Deprecation deprecation = Deprecation.of("1.5.0", "NewClass", "Use NewClass instead for better performance");
 *
 * // Create a full deprecation notice with all fields
 * Deprecation deprecation = Deprecation.of("2.0.0", "NewApi", "OldApi is inefficient", "https://example.com/docs/migration", Deprecation.Level.REMOVAL);
 *
 * // Using the Builder API for more flexibility
 * Deprecation deprecation = Deprecation.builder()
 *     .since("2.1.0")
 *     .replacement("ImprovedUtil")
 *     .reason("LegacyUtil has known issues and will be removed in future versions.")
 *     .link("https://docs.example.com/legacyutil-removal")
 *     .level(Deprecation.Level.REMOVAL)
 *     .build();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Serializable
 * @see Version
 * @since 1.0.0
 */
@Immutable
public final class Deprecation implements Serializable {

    private static final long serialVersionUID = 6380988834632455932L;

    @Nullable
    private final Version since;

    @Nullable
    private final String replacement;

    @Nullable
    private final String reason;

    @Nullable
    private final String link;

    @Nonnull
    private final Level level;

    Deprecation(Deprecation source) {
        this.since = source.since;
        this.replacement = source.replacement;
        this.reason = source.reason;
        this.link = source.link;
        this.level = source.level;
    }

    Deprecation(@Nullable Version since, @Nullable String replacement, @Nullable String reason,
                @Nullable String link, @Nullable Level level) {
        this.since = since;
        this.replacement = replacement;
        this.reason = reason;
        this.link = link;
        this.level = level == null ? DEFAULT : level;
    }

    @Nullable
    public Version getSince() {
        return since;
    }

    @Nullable
    public String getReplacement() {
        return replacement;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Nullable
    public String getLink() {
        return link;
    }

    @Nonnull
    public Level getLevel() {
        return level;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deprecation)) return false;
        Deprecation that = (Deprecation) o;
        return Objects.equals(since, that.since)
                && Objects.equals(replacement, that.replacement)
                && Objects.equals(reason, that.reason)
                && Objects.equals(link, that.link)
                && level == that.level;
    }

    @Override
    public int hashCode() {
        return hash(since, replacement, reason, link, level);
    }

    @Override
    public String toString() {
        return "Deprecation{" +
                "since='" + since + QUOTE_CHAR +
                ", replacement='" + replacement + QUOTE_CHAR +
                ", reason='" + reason + QUOTE_CHAR +
                ", link='" + link + QUOTE_CHAR +
                ", level=" + level +
                '}';
    }

    /**
     * Deprecation Level
     */
    public enum Level {

        /**
         * Default
         */
        DEFAULT,

        /**
         * Removal
         */
        REMOVAL,
    }

    /**
     * The Builder class for {@link Deprecation}
     */
    public static class Builder {

        @Nullable
        private Version since;

        @Nullable
        private String replacement;

        @Nullable
        private String reason;

        @Nullable
        private String link;

        @Nonnull
        private Level level;

        protected Builder() {
        }

        public Builder since(@Nullable String since) {
            return since(Version.of(since));
        }

        public Builder since(@Nullable Version since) {
            this.since = since;
            return this;
        }

        public Builder replacement(@Nullable String replacement) {
            this.replacement = replacement;
            return this;
        }

        public Builder reason(@Nullable String reason) {
            this.reason = reason;
            return this;
        }

        public Builder link(@Nullable String link) {
            this.link = link;
            return this;
        }

        public Builder level(@Nullable Level level) {
            this.level = level == null ? DEFAULT : level;
            return this;
        }


        /**
         * Build an instance of {@link Deprecation}
         *
         * @return non-null
         */
        public Deprecation build() {
            return new Deprecation(since, replacement, reason, link, level);
        }
    }

    /**
     * Create a new instance of {@link Deprecation.Builder}
     *
     * @return non-null
     */
    public static Deprecation.Builder builder() {
        return new Builder();
    }

    public static Deprecation of(String since) {
        return of(since, null);
    }

    public static Deprecation of(String since, String replacement) {
        return of(since, replacement, null);
    }

    public static Deprecation of(String since, String replacement, String reason) {
        return of(since, replacement, reason, null);
    }

    public static Deprecation of(String since, String replacement, String reason, String link) {
        return of(since, replacement, reason, link, DEFAULT);
    }

    public static Deprecation of(String since, String replacement, String reason, String link, Level level) {
        return builder()
                .since(since)
                .replacement(replacement)
                .reason(reason)
                .link(link)
                .level(level)
                .build();
    }
}
