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
import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * The definition class of Java {@link Method}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Method
 * @see Version
 * @since 1.0.0
 */
public class MethodDefinition {

    @Nonnull
    private final Version since;

    @Nullable
    private final Deprecation deprecation;

    @Nonnull
    private final Class<?> declaredClass;

    @Nonnull
    private final String methodName;

    @Nonnull
    private final Class<?>[] parameterTypes;

    @Nullable
    private final Method resolvedMethod;

    protected MethodDefinition(Version since, Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        this(since, null, declaredClass, methodName, parameterTypes);
    }

    protected MethodDefinition(Version since, Deprecation deprecation, Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        assertNotNull(since, () -> "The 'since' version of method must not be null.");
        assertNotNull(declaredClass, () -> "The declared class of method must not be null.");
        assertNotBlank(methodName, () -> "The name of method must not be blank.");
        assertNotNull(parameterTypes, () -> "The parameter types of method must not be null.");
        this.since = since;
        this.deprecation = deprecation;
        this.declaredClass = declaredClass;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.resolvedMethod = findMethod(declaredClass, methodName, parameterTypes);
    }

    /**
     * Create a new {@link MethodDefinition}
     *
     * @param since          the since version
     * @param declaredClass  The declared class of the method
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return non-null
     */
    public static MethodDefinition of(String since, Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        return of(Version.of(since), declaredClass, methodName, parameterTypes);
    }

    /**
     * Create a new {@link MethodDefinition}
     *
     * @param since          the since version
     * @param declaredClass  The declared class of the method
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return non-null
     */
    public static MethodDefinition of(Version since, Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        return new MethodDefinition(since, declaredClass, methodName, parameterTypes);
    }

    /**
     * Create a new {@link MethodDefinition}
     *
     * @param since          the since version
     * @param deprecation    the deprecation
     * @param declaredClass  The declared class of the method
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return non-null
     */
    public static MethodDefinition of(Version since, @Nullable Deprecation deprecation, Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        return new MethodDefinition(since, deprecation, declaredClass, methodName, parameterTypes);
    }


    /**
     * The since version of the method
     *
     * @return non-null
     */
    @Nonnull
    public Version getSince() {
        return since;
    }

    /**
     * The deprecation of the method
     *
     * @return <code>null</code> if not deprecated
     */
    @Nullable
    public Deprecation getDeprecation() {
        return deprecation;
    }

    /**
     * The declared class of the method
     *
     * @return non-null
     */
    @Nonnull
    public Class<?> getDeclaredClass() {
        return declaredClass;
    }

    /**
     * The method name
     *
     * @return non-null
     */
    @Nonnull
    public String getMethodName() {
        return methodName;
    }

    /**
     * The parameter types
     *
     * @return non-null
     */
    @Nonnull
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }

    /**
     * The resolved method
     *
     * @return <code>null</code> if not resovled
     */
    public Method getResolvedMethod() {
        return resolvedMethod;
    }

    /**
     * Determine whether the method definition is present
     *
     * @return {@code true} if the method definition is present
     */
    public boolean isPresent() {
        return resolvedMethod != null;
    }

    /**
     * Invokes the underlying method represented by this {@code Method}
     * object, on the specified object with the specified parameters.
     * Individual parameters are automatically unwrapped to match
     * primitive formal parameters, and both primitive and reference
     * parameters are subject to method invocation conversions as
     * necessary.
     *
     * @param instance the instance for method invocation
     * @param args     the arguments for method invocation
     * @param <R>      the type of return value
     * @return the return value
     * @throws IllegalStateException    if this {@code Method} object
     *                                  is enforcing Java language access control and the underlying
     *                                  method is inaccessible.
     * @throws IllegalArgumentException if the method is an
     *                                  instance method and the specified object argument
     *                                  is not an instance of the class or interface
     *                                  declaring the underlying method (or of a subclass
     *                                  or implementor thereof); if the number of actual
     *                                  and formal parameters differ; if an unwrapping
     *                                  conversion for primitive arguments fails; or if,
     *                                  after possible unwrapping, a parameter value
     *                                  cannot be converted to the corresponding formal
     *                                  parameter type by a method invocation conversion.
     * @throws RuntimeException         if the underlying method
     *                                  throws an exception.
     */
    public <R> R invoke(Object instance, Object... args) throws IllegalStateException, IllegalArgumentException, RuntimeException {
        return invokeMethod(instance, getResolvedMethod(), args);
    }
}
