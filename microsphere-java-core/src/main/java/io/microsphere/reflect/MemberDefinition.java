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

/**
 * The definition class for Java Refection {@link Member}
 *
 * @param <M> the subtype of {@link Member}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Member
 * @see ConstructorDefinition
 * @see FieldDefinition
 * @see MethodDefinition
 * @since 1.0.0
 */
public abstract class MemberDefinition<M extends Member> extends ReflectiveDefinition {

    @Nonnull
    protected final String name;

    @Nullable
    private transient M member;

    private boolean resolvedMember;

    /**
     * @param since             the 'since' version
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public MemberDefinition(@Nonnull String since, @Nonnull String declaredClassName, @Nonnull String name) {
        this(since, null, declaredClassName, name);
    }

    /**
     * @param since             the 'since' version
     * @param deprecation       the deprecation
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public MemberDefinition(@Nonnull String since, @Nullable Deprecation deprecation, @Nonnull String declaredClassName, @Nonnull String name) {
        this(Version.of(since), deprecation, declaredClassName, name);
    }

    /**
     * @param since             the 'since' version
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public MemberDefinition(@Nonnull Version since, @Nonnull String declaredClassName, @Nonnull String name) {
        this(since, null, declaredClassName, name);
    }

    /**
     * @param since             the 'since' version
     * @param deprecation       the deprecation
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public MemberDefinition(@Nonnull Version since, @Nullable Deprecation deprecation, @Nonnull String declaredClassName, @Nonnull String name) {
        super(since, deprecation, declaredClassName);
        this.name = name;
    }

    /**
     * Resolve the {@link M member} instance
     *
     * @return <code>null</code> if can't be resolved
     */
    protected abstract M resolveMember();

    /**
     * Get the member name
     *
     * @return non-null
     */
    @Nonnull
    public final String getName() {
        return name;
    }

    /**
     * Get the declared class name
     *
     * @return non-null
     */
    @Nonnull
    public final String getDeclaredClassName() {
        return super.getClassName();
    }

    /**
     * Get the declared class
     *
     * @return nullable
     */
    @Nullable
    public final Class<?> getDeclaredClass() {
        return super.getResolvedClass();
    }

    /**
     * Get the member instance
     *
     * @return <code>null</code> if can't be resolved
     */
    @Nullable
    public final M getMember() {
        if (!resolvedMember && member == null) {
            member = resolveMember();
            resolvedMember = true;
        }
        return member;
    }

    @Override
    public boolean isPresent() {
        return getMember() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemberDefinition)) return false;
        if (!super.equals(o)) return false;

        MemberDefinition<?> that = (MemberDefinition<?>) o;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MemberDefinition{" +
                "since=" + since +
                ", deprecation=" + deprecation +
                ", declaredClassName='" + getDeclaredClassName() + '\'' +
                ", declaredClass=" + getDeclaredClass() +
                ", name='" + name + '\'' +
                ", member=" + getMember() +
                '}';
    }
}
