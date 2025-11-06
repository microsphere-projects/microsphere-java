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

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.lang.Deprecation;
import io.microsphere.util.Version;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.util.Objects.hash;

/**
 * The definition class for Java Reflection {@link Executable}, which serves as a base class for executable members
 * like methods and constructors. It provides common functionality to store and resolve parameter types based on their class names.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MethodDefinition extends ExecutableDefinition<Method> {
 *     public MethodDefinition(Version since, String declaredClassName, String name, String... parameterClassNames) {
 *         super(since, null, declaredClassName, name, parameterClassNames);
 *     }
 *
 *     @Override
 *     protected Method resolveMember() {
 *         // Implementation here
 *         return null;
 *     }
 * }
 * }</pre>
 *
 * @param <E> the subtype of {@link Executable}, typically either {@link Method} or {@link Constructor}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConstructorDefinition
 * @see MethodDefinition
 * @see Executable
 * @see Method
 * @see Constructor
 * @since 1.0.0
 */
@Immutable
public abstract class ExecutableDefinition<E extends Executable> extends MemberDefinition<E> {

    @Nonnull
    protected final String[] parameterClassNames;

    private transient boolean resolvedParameterTypes;

    @Nonnull
    private transient Class<?>[] parameterTypes;

    /**
     * @param since               the 'since' version
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the class names of parameters
     */
    protected ExecutableDefinition(@Nonnull String since, @Nonnull String declaredClassName, @Nonnull String name,
                                   @Nonnull String... parameterClassNames) {
        this(since, null, declaredClassName, name, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param deprecation         the deprecation
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the parameter class names
     */
    protected ExecutableDefinition(@Nonnull String since, @Nonnull Deprecation deprecation, @Nonnull String declaredClassName,
                                   @Nonnull String name, @Nonnull String... parameterClassNames) {
        this(Version.of(since), deprecation, declaredClassName, name, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the class names of parameters
     */
    protected ExecutableDefinition(@Nonnull Version since, @Nonnull String declaredClassName, @Nonnull String name,
                                   @Nonnull String... parameterClassNames) {
        this(since, null, declaredClassName, name, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param deprecation         the deprecation
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the parameter class names
     */
    protected ExecutableDefinition(@Nonnull Version since, @Nonnull Deprecation deprecation, @Nonnull String declaredClassName,
                                   @Nonnull String name, @Nonnull String... parameterClassNames) {
        super(since, deprecation, declaredClassName, name);
        assertNotNull(parameterClassNames, () -> "the class names of parameters of method must not be null.");
        assertNoNullElements(parameterClassNames, () -> "The parameter class names must not contain any null element.");
        this.parameterClassNames = parameterClassNames;
    }

    /**
     * Get the parameter class names
     *
     * @return non-null
     */
    @Nonnull
    public final String[] getParameterClassNames() {
        return parameterClassNames.clone();
    }

    /**
     * the class names of parameters
     *
     * @return the element of array may contain <code>null</code> if it can't be resolved
     */
    @Nonnull
    public final Class<?>[] getParameterTypes() {
        if (!this.resolvedParameterTypes && this.parameterTypes == null) {
            this.parameterTypes = resolveParameterTypes(this.parameterClassNames);
            this.resolvedParameterTypes = true;
        }
        return this.parameterTypes.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExecutableDefinition)) return false;
        if (!super.equals(o)) return false;

        ExecutableDefinition that = (ExecutableDefinition) o;
        return arrayEquals(this.parameterClassNames, that.parameterClassNames);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + hash(this.parameterClassNames);
        return result;
    }

    protected Class<?>[] resolveParameterTypes(String[] parameterClassNames) {
        ClassLoader classLoader = getClassLoader(getClass());
        int length = parameterClassNames.length;
        Class<?>[] parameterTypes = new Class<?>[length];
        for (int i = 0; i < length; i++) {
            parameterTypes[i] = resolveClass(parameterClassNames[i], classLoader);
        }
        return parameterTypes;
    }
}
