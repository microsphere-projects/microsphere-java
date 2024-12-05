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
package io.microsphere.reflect;

import io.microsphere.lang.Deprecation;
import io.microsphere.util.Version;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Member;
import java.util.Objects;

import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * The definition class for Java Refection {@link Member}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Member
 * @since 1.0.0
 */
public abstract class MemberDefinition {

    @Nonnull
    protected final Version since;

    @Nullable
    protected final Deprecation deprecation;

    @Nonnull
    protected final Class<?> declaredClass;

    @Nonnull
    protected final String name;

    public MemberDefinition(@Nonnull Version since, @Nullable Deprecation deprecation, @Nonnull Class<?> declaredClass, @Nonnull String name) {
        assertNotNull(since, () -> "The 'since' version must not be null.");
        assertNotNull(declaredClass, () -> "The declared class must not be null.");
        assertNotBlank(name, () -> "The name must not be blank.");
        this.since = since;
        this.deprecation = deprecation;
        this.declaredClass = declaredClass;
        this.name = name;
    }

    @Nonnull
    public Version getSince() {
        return since;
    }

    @Nullable
    public Deprecation getDeprecation() {
        return deprecation;
    }

    @Nonnull
    public Class<?> getDeclaredClass() {
        return declaredClass;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemberDefinition)) return false;

        MemberDefinition that = (MemberDefinition) o;
        return getSince().equals(that.getSince())
                && Objects.equals(getDeprecation(), that.getDeprecation())
                && getDeclaredClass().equals(that.getDeclaredClass())
                && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        int result = getSince().hashCode();
        result = 31 * result + Objects.hashCode(getDeprecation());
        result = 31 * result + getDeclaredClass().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }


    @Override
    public String toString() {
        return "MemberDefinition{" +
                "since=" + since +
                ", deprecation=" + deprecation +
                ", declaredClass=" + declaredClass +
                ", name='" + name + '\'' +
                '}';
    }
}
