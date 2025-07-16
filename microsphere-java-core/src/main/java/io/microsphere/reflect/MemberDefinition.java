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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.Deprecation;
import io.microsphere.util.Version;

import java.lang.reflect.Member;

import static io.microsphere.util.Version.ofVersion;

/**
 * The definition class for Java Reflection {@link Member}.
 *
 * <p>This abstract class provides a base implementation to define and resolve members (such as fields,
 * methods, or constructors) from a class. It extends the capabilities of the
 * {@link ReflectiveDefinition} class by adding support for member names and lazy resolution of the
 * actual reflection object.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class FieldDefinition extends MemberDefinition<Field> {
 *
 *     public FieldDefinition(Version since, String declaredClassName, String name) {
 *         super(since, null, declaredClassName, name);
 *     }
 *
 *     @Override
 *     protected Field resolveMember() {
 *         Class<?> clazz = getDeclaredClass();
 *         if (clazz == null) {
 *             return null;
 *         }
 *         try {
 *             return clazz.getDeclaredField(name);
 *         } catch (NoSuchFieldException e) {
 *             return null;
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>In this example, a custom subclass of {@link MemberDefinition}, called
 * {@code FieldDefinition}, is created to represent a field. The method {@link #resolveMember()}
 * attempts to resolve the field using reflection.
 *
 * <p>Subclasses should implement the logic to resolve specific types of members such as
 * methods, fields, or constructors by overriding the {@link #resolveMember()} method.
 *
 * @param <M> the type of the member, which must be a subclass of {@link Member}
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
        this(ofVersion(since), deprecation, declaredClassName, name);
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
}
