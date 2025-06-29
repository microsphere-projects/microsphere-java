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

import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.lang.function.ThrowableSupplier;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ExceptionUtils.wrap;

/**
 * The utility class for Java Reflection {@link Executable}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Executable
 * @since 1.0.0
 */
public abstract class ExecutableUtils implements Utils {

    private static final Logger logger = getLogger(ExecutableUtils.class);

    /**
     * Executes the given {@link Executable} object using the provided callback.
     *
     * <p>This method is typically used when no return value is expected from the execution,
     * and any exception thrown during execution will be wrapped and rethrown as a
     * {@link RuntimeException}, {@link IllegalStateException}, or
     * {@link IllegalArgumentException} depending on the cause.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * Method method = MyClass.class.getMethod("myMethod");
     * ExecutableUtils.execute(method, executable -> {
     *     // Perform some operation with the executable
     *     executable.invoke(instance);
     * });
     * }</pre>
     *
     * <p>If an exception occurs during execution:</p>
     *
     * <pre>{@code
     * try {
     *     ExecutableUtils.execute(method, executable -> {
     *         executable.invoke(instance);
     *     });
     * } catch (RuntimeException e) {
     *     System.err.println("Execution failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param object   The executable member to be executed, such as a {@link Method}, {@link Constructor}, or {@link Field}.
     * @param callback The callback that defines the operation to perform on the executable.
     * @param <E>      The type or subtype of {@link Executable}.
     * @throws NullPointerException     If <code>executableMember</code> is <code>null</code>
     * @throws IllegalStateException    if this {@code executableMember} object
     *                                  is enforcing Java language access control and the underlying
     *                                  executable member is inaccessible.
     * @throws IllegalArgumentException if the executable member is an
     *                                  instance executable member and the specified object argument
     *                                  is not an instance of the class or interface
     *                                  declaring the underlying executable member (or of a subclass
     *                                  or implementor thereof); if the number of actual
     *                                  and formal parameters differ; if an unwrapping
     *                                  conversion for primitive arguments fails; or if,
     *                                  after possible unwrapping, a parameter value
     *                                  cannot be converted to the corresponding formal
     *                                  parameter type by a executable member invocation conversion.
     * @throws RuntimeException         if the underlying executable member
     *                                  throws an exception.
     */
    public static <E extends Executable & Member> void execute(E object, ThrowableConsumer<E> callback)
            throws NullPointerException, IllegalStateException, IllegalArgumentException, RuntimeException {
        execute(object, a -> {
            callback.accept(a);
            return null;
        });
    }

    /**
     * Executes the given {@link Executable} object using a {@link ThrowableSupplier}.
     *
     * <p>This method is useful when you want to execute an operation that may throw a checked exception,
     * and you need to handle it in a clean and concise way. The supplier's execution result will be returned
     * if successful, or an appropriate runtime exception will be thrown if an error occurs.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * Method method = MyClass.class.getMethod("myMethod");
     * String result = ExecutableUtils.execute(method, () -> {
     *     return (String) method.invoke(instance);
     * });
     * }</pre>
     *
     * <p>If an exception occurs during execution:</p>
     *
     * <pre>{@code
     * try {
     *     String result = ExecutableUtils.execute(method, () -> {
     *         return (String) method.invoke(instance);
     *     });
     * } catch (RuntimeException e) {
     *     System.err.println("Execution failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param executable The executable member to be executed, such as a {@link Method}, {@link Constructor}, or {@link Field}.
     * @param supplier   The supplier defining the operation to perform on the executable.
     * @param <E>        The type or subtype of {@link Executable}.
     * @param <R>        The type of the result expected from the supplier.
     * @return The result of the supplier execution.
     * @throws NullPointerException     If <code>executableMember</code> is <code>null</code>
     * @throws IllegalStateException    if this {@code executableMember} object
     *                                  is enforcing Java language access control and the underlying
     *                                  executable member is inaccessible.
     * @throws IllegalArgumentException if the executable member is an
     *                                  instance executable member and the specified object argument
     *                                  is not an instance of the class or interface
     *                                  declaring the underlying executable member (or of a subclass
     *                                  or implementor thereof); if the number of actual
     *                                  and formal parameters differ; if an unwrapping
     *                                  conversion for primitive arguments fails; or if,
     *                                  after possible unwrapping, a parameter value
     *                                  cannot be converted to the corresponding formal
     *                                  parameter type by a executable member invocation conversion.
     * @throws RuntimeException         if the underlying executable member
     *                                  throws an exception.
     */
    public static <E extends Executable & Member, R> R execute(E executable, ThrowableSupplier<R> supplier)
            throws NullPointerException, IllegalStateException, IllegalArgumentException, RuntimeException {
        return execute(executable, (ThrowableFunction<E, R>) a -> supplier.execute());
    }

    /**
     * Executes the provided {@link Executable} using the given callback function.
     *
     * <p>This method handles common reflection-related exceptions and wraps them into appropriate runtime exceptions.
     * It is suitable for executing operations that return a result, allowing custom handling of the executable member.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * Method method = MyClass.class.getMethod("myMethod", String.class);
     * String result = ExecutableUtils.execute(method, m -> {
     *     return (String) m.invoke(instance, "Hello");
     * });
     * }</pre>
     *
     * <p>If an exception occurs during execution:</p>
     *
     * <pre>{@code
     * try {
     *     Method method = MyClass.class.getMethod("myMethod", String.class);
     *     String result = ExecutableUtils.execute(method, m -> {
     *         return (String) m.invoke(instance, "Hello");
     *     });
     * } catch (IllegalStateException e) {
     *     System.err.println("Member is inaccessible: " + e.getMessage());
     * } catch (IllegalArgumentException e) {
     *     System.err.println("Argument mismatch: " + e.getMessage());
     * } catch (RuntimeException e) {
     *     System.err.println("Execution failed: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param executableMember The executable member to be executed, such as a {@link Method}, {@link Constructor}, or {@link Field}.
     * @param callback         The function defining the operation to perform on the executable member, returning a result.
     * @param <E>              The type or subtype of {@link Executable}.
     * @param <R>              The type of the result of the callback function.
     * @return The result of the callback execution.
     * @throws NullPointerException     If <code>executableMember</code> is <code>null</code>
     * @throws IllegalStateException    if this {@code executableMember} object
     *                                  is enforcing Java language access control and the underlying
     *                                  executable member is inaccessible.
     * @throws IllegalArgumentException if the executable member is an
     *                                  instance executable member and the specified object argument
     *                                  is not an instance of the class or interface
     *                                  declaring the underlying executable member (or of a subclass
     *                                  or implementor thereof); if the number of actual
     *                                  and formal parameters differ; if an unwrapping
     *                                  conversion for primitive arguments fails; or if,
     *                                  after possible unwrapping, a parameter value
     *                                  cannot be converted to the corresponding formal
     *                                  parameter type by a executable member invocation conversion.
     * @throws RuntimeException         if the underlying executable member
     *                                  throws an exception.
     */
    public static <E extends Executable & Member, R> R execute(E executableMember, ThrowableFunction<E, R> callback)
            throws NullPointerException, IllegalStateException, IllegalArgumentException, RuntimeException {
        R result = null;
        RuntimeException failure = null;
        try {
            result = callback.apply(executableMember);
        } catch (IllegalAccessException e) {
            String errorMessage = format("The executable member['{}'] can't be accessed", executableMember);
            failure = new IllegalStateException(errorMessage, e);
        } catch (IllegalArgumentException e) {
            String errorMessage = format("The arguments can't match the executable member['{}'] : {}", executableMember, e.getMessage());
            failure = new IllegalArgumentException(errorMessage, e);
        } catch (InvocationTargetException e) {
            String errorMessage = format("It's failed to invoke the executable member['{}']", executableMember);
            failure = new RuntimeException(errorMessage, e.getTargetException());
        } catch (Throwable e) {
            failure = wrap(e, RuntimeException.class);
        }

        if (failure != null) {
            logger.error(failure.getMessage(), failure.getCause());
            throw failure;
        }

        return result;
    }

    private ExecutableUtils() {
    }
}
