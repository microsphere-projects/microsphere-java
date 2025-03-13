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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The definition class of Java {@link Executable}
 *
 * @param <E> the subtype of {@link Executable}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConstructorDefinition
 * @see MethodDefinition
 * @see Executable
 * @see Constructor
 * @see Method
 * @since 1.0.0
 */
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
    public ExecutableDefinition(String since, String declaredClassName, String name, String... parameterClassNames) {
        this(since, null, declaredClassName, name, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param deprecation         the deprecation
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the parameter class names
     */
    public ExecutableDefinition(String since, Deprecation deprecation, String declaredClassName, String name, String... parameterClassNames) {
        this(Version.of(since), deprecation, declaredClassName, name, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the class names of parameters
     */
    public ExecutableDefinition(Version since, String declaredClassName, String name, String... parameterClassNames) {
        this(since, null, declaredClassName, name, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param deprecation         the deprecation
     * @param declaredClassName   The declared class name of the method
     * @param name                the {@link Executable} name
     * @param parameterClassNames the parameter class names
     */
    public ExecutableDefinition(Version since, Deprecation deprecation, String declaredClassName, String name, String... parameterClassNames) {
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
        return Arrays.equals(this.parameterClassNames, that.parameterClassNames);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.parameterClassNames);
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
