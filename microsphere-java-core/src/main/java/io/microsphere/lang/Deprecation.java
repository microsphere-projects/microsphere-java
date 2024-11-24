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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

import static io.microsphere.lang.Deprecation.Level.DEFAULT;

/**
 * The info class for deprecation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Serializable
 * @since 1.0.0
 */
public final class Deprecation implements Serializable {

    @Nullable
    private String since;

    @Nullable
    private String replacement;

    @Nullable
    private String reason;

    @Nullable
    private String link;

    @Nonnull
    private Level level = DEFAULT;

    @Nullable
    public String getSince() {
        return since;
    }

    public void setSince(@Nullable String since) {
        this.since = since;
    }

    @Nullable
    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(@Nullable String replacement) {
        this.replacement = replacement;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    @Nullable
    public String getLink() {
        return link;
    }

    public void setLink(@Nullable String link) {
        this.link = link;
    }

    @Nonnull
    public Level getLevel() {
        return level;
    }

    public void setLevel(@Nonnull Level level) {
        this.level = level;
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
        return Objects.hash(since, replacement, reason, link, level);
    }

    /**
     * Deprecation Level
     */
    public static enum Level {

        /**
         * Default
         */
        DEFAULT,

        /**
         * Removal
         */
        REMOVAL,
    }
}
