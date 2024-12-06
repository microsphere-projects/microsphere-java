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
import java.io.Serializable;
import java.util.Objects;

import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The abstract definition class for Java Reflection
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassDefinition
 * @see MemberDefinition
 * @since 1.0.0
 */
public abstract class ReflectiveDefinition implements Serializable {

    private static final long serialVersionUID = -5500080952852240466L;

    protected final transient ClassLoader classLoader;

    @Nonnull
    protected final Version since;

    @Nullable
    protected final Deprecation deprecation;

    @Nonnull
    protected final String declaredClassName;

    @Nullable
    protected final transient Class<?> declaredClass;

    /**
     * @param since             the 'since' version
     * @param declaredClassName the name of declared class
     */
    public ReflectiveDefinition(@Nonnull String since, @Nonnull String declaredClassName) {
        this(since, null, declaredClassName);
    }

    /**
     * @param since             the 'since' version
     * @param deprecation       the deprecation
     * @param declaredClassName the name of declared class
     */
    public ReflectiveDefinition(@Nonnull String since, @Nullable Deprecation deprecation, @Nonnull String declaredClassName) {
        this(Version.of(since), deprecation, declaredClassName);
    }

    /**
     * @param since             the 'since' version
     * @param declaredClassName the name of declared class
     */
    public ReflectiveDefinition(@Nonnull Version since, @Nonnull String declaredClassName) {
        this(since, null, declaredClassName);
    }

    /**
     * @param since             the 'since' version
     * @param deprecation       the deprecation
     * @param declaredClassName the name of declared class
     */
    public ReflectiveDefinition(@Nonnull Version since, @Nullable Deprecation deprecation, @Nonnull String declaredClassName) {
        assertNotNull(since, () -> "The 'since' version must not be null.");
        assertNotBlank(declaredClassName, () -> "The declared class name must not be null.");
        ClassLoader classLoader = getClassLoader(getClass());
        this.classLoader = classLoader;
        this.since = since;
        this.deprecation = deprecation;
        this.declaredClassName = declaredClassName;
        this.declaredClass = resolveClass(declaredClassName, classLoader, true);
    }

    /**
     * Get the 'since' version
     *
     * @return non-null
     */
    @Nonnull
    public final Version getSince() {
        return since;
    }

    /**
     * Get the deprecation
     *
     * @return nullable
     */
    @Nullable
    public final Deprecation getDeprecation() {
        return deprecation;
    }

    /**
     * Get the name of declared class
     *
     * @return non-null
     */
    @Nonnull
    public final String getDeclaredClassName() {
        return declaredClassName;
    }

    /**
     * Get the declared class
     *
     * @return <code>null</code> if can't be loaded
     */
    @Nullable
    public final Class<?> getDeclaredClass() {
        return declaredClass;
    }

    /**
     * Whether the member is deprecated
     */
    public final boolean isDeprecated() {
        return deprecation != null;
    }

    /**
     * Whether the member is present
     *
     * @return <code>true</code> if present
     */
    public abstract boolean isPresent();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectiveDefinition)) return false;

        ReflectiveDefinition that = (ReflectiveDefinition) o;
        return this.since.equals(that.since)
                && Objects.equals(this.deprecation, that.deprecation)
                && this.declaredClassName.equals(that.declaredClassName);
    }

    @Override
    public int hashCode() {
        int result = this.since.hashCode();
        result = 31 * result + Objects.hashCode(this.deprecation);
        result = 31 * result + this.declaredClassName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReflectiveDefinition{" +
                "since=" + this.since +
                ", deprecation=" + this.deprecation +
                ", declaredClassName=" + this.declaredClassName +
                ", declaredClass=" + this.declaredClass +
                '}';
    }
}
