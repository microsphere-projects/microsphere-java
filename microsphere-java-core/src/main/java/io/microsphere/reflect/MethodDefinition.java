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

import java.lang.reflect.Method;

import static io.microsphere.constants.SymbolConstants.QUOTE_CHAR;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ArrayUtils.arrayToString;

/**
 * The definition class of Java {@link Method}, providing a structured way to define and resolve methods
 * by their name, parameter types, and declaring class. This class serves as a convenient wrapper for
 * method metadata and resolution logic.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Define a method without deprecation information
 * MethodDefinition methodDef = new MethodDefinition("1.0.0", "com.example.MyClass", "myMethod", "java.lang.String", "int");
 *
 * // Define a deprecated method
 * Deprecation deprecation = new Deprecation("2.0.0", "Use newMethod instead.");
 * MethodDefinition deprecatedMethodDef = new MethodDefinition("1.0.0", deprecation, "com.example.MyClass", "oldMethod", "java.util.List");
 *
 * // Resolve and invoke the method
 * Method method = methodDef.getMethod(); // May return null if not found
 * String result = methodDef.invoke(instance, "example", 42); // Invokes myMethod with String and int parameters
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Method
 * @see Version
 * @since 1.0.0
 */
public class MethodDefinition extends ExecutableDefinition<Method> {

    /**
     * @param since               the 'since' version
     * @param declaredClassName   The declared class name of the method
     * @param methodName          the method name
     * @param parameterClassNames the parameter types
     */
    public MethodDefinition(String since, String declaredClassName, String methodName, String... parameterClassNames) {
        this(since, null, declaredClassName, methodName, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param deprecation         the deprecation
     * @param declaredClassName   The declared class name of the method
     * @param methodName          the method name
     * @param parameterClassNames the parameter class names
     */
    public MethodDefinition(String since, Deprecation deprecation, String declaredClassName, String methodName, String... parameterClassNames) {
        this(Version.of(since), deprecation, declaredClassName, methodName, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param declaredClassName   The declared class name of the method
     * @param methodName          the method name
     * @param parameterClassNames the parameter types
     */
    public MethodDefinition(Version since, String declaredClassName, String methodName, String... parameterClassNames) {
        this(since, null, declaredClassName, methodName, parameterClassNames);
    }

    /**
     * @param since               the 'since' version
     * @param deprecation         the deprecation
     * @param declaredClassName   The declared class name of the method
     * @param methodName          the method name
     * @param parameterClassNames the parameter class names
     */
    public MethodDefinition(Version since, Deprecation deprecation, String declaredClassName, String methodName, String... parameterClassNames) {
        super(since, deprecation, declaredClassName, methodName, parameterClassNames);
    }

    /**
     * The method name
     *
     * @return non-null
     */
    @Nonnull
    public String getMethodName() {
        return super.getName();
    }

    /**
     * The resolved method
     *
     * @return <code>null</code> if not resolved
     */
    @Nullable
    public Method getMethod() {
        return super.getMember();
    }

    @Override
    protected Method resolveMember() {
        return findMethod(super.getDeclaredClass(), getMethodName(), super.getParameterTypes());
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
        return invokeMethod(instance, getMethod(), args);
    }

    @Override
    public String toString() {
        return "MethodDefinition{" +
                "since=" + super.since +
                ", deprecation=" + super.deprecation +
                ", declaredClassName='" + super.getDeclaredClassName() + QUOTE_CHAR +
                ", declaredClass=" + super.getDeclaredClass() +
                ", methodName='" + getMethodName() + QUOTE_CHAR +
                ", method=" + getMethod() +
                ", parameterClassName=" + arrayToString(super.parameterClassNames) +
                ", parameterTypes=" + arrayToString(super.getParameterTypes()) +
                '}';
    }
}
