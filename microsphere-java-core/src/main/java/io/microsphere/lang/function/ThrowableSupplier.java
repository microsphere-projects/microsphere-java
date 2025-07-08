package io.microsphere.lang.function;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.microsphere.util.Assert.assertNotNull;

/**
 * A functional interface similar to {@link Supplier}, but allows the {@code get()} method to throw a
 * {@link Throwable}. This is useful for functional constructs where operations may throw checked exceptions that need
 * to be handled or rethrown.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Using ThrowableSupplier to read a file content
 * ThrowableSupplier<String> fileReader = () -> {
 *     Path path = Paths.get("example.txt");
 *     return Files.readString(path);
 * };
 *
 * // Execute with default exception handling (converts to RuntimeException)
 * String content = fileReader.execute();
 *
 * // Execute with custom exception handling
 * String contentWithHandler = fileReader.execute(ex -> {
 *     System.err.println("Error reading file: " + ex.getMessage());
 *     return "default content";
 * });
 * }</pre>
 *
 * <p>This interface provides convenience methods to execute the supplier and handle exceptions using a custom handler,
 * making it easier to work with functional patterns in environments where exceptions must be managed.</p>
 *
 * @param <T> the type of result supplied by this supplier
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Supplier
 * @see Throwable
 * @since 1.0.0
 */
@FunctionalInterface
public interface ThrowableSupplier<T> {

    /**
     * Applies this function to the given argument.
     *
     * @return the supplied result
     * @throws Throwable if met with any error
     */
    T get() throws Throwable;

    /**
     * Execute {@link #get()} with {@link #handleException(Throwable) the default exception handling}
     *
     * @return the supplied result
     * @see #get()
     */
    default T execute() {
        return execute(this::handleException);
    }

    /**
     * Execute {@link #get()} with the customized {@link Throwable exception} handling
     *
     * @param exceptionHandler the handler to handle any {@link Throwable exception} that the {@link #get()} method throws
     * @return the supplied result
     * @see #execute()
     */
    default T execute(Function<Throwable, T> exceptionHandler) {
        assertNotNull(exceptionHandler, () -> "The 'exceptionHandler' must not be null");
        T result;
        try {
            result = get();
        } catch (Throwable e) {
            result = exceptionHandler.apply(e);
        }
        return result;
    }

    /**
     * Handle any exception that the {@link #get} method throws
     *
     * @param failure the instance of {@link Throwable}
     * @return the result after the exception handling
     */
    default T handleException(Throwable failure) {
        throw new RuntimeException(failure);
    }

    /**
     * Executes the given {@link ThrowableSupplier} with the default exception handling.
     * <p>
     * This method is useful when you want to invoke a supplier that may throw checked exceptions without explicitly
     * handling them. Any exception thrown during the execution of the supplier will be passed to the default exception
     * handler, which wraps it into a {@link RuntimeException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Using the static execute method with default exception handling
     * String content = ThrowableSupplier.execute(() -> {
     *     Path path = Paths.get("example.txt");
     *     return Files.readString(path);
     * });
     * }</pre>
     *
     * @param supplier The supplier to execute.
     * @param <T>      The type of result supplied by the supplier.
     * @return The result of the supplier after successful execution.
     * @throws NullPointerException if the given supplier is {@code null}.
     */
    static <T> T execute(ThrowableSupplier<T> supplier) throws NullPointerException {
        return execute(supplier, supplier::handleException);
    }

    /**
     * Executes the given {@link ThrowableSupplier} with a custom exception handler.
     * <p>
     * This method allows for flexible exception handling when executing a supplier that may throw checked exceptions.
     * If the supplier or exception handler is null, a {@link NullPointerException} will be thrown.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Using a custom exception handler to return a default value
     * String content = execute(
     *     () -> Files.readString(Paths.get("example.txt")),
     *     ex -> "Default Content"
     * );
     *
     * // Example 2: Logging and rethrowing as a custom exception
     * String content = execute(
     *     () -> Files.readString(Paths.get("example.txt")),
     *     ex -> {
     *         System.err.println("Failed to read file: " + ex.getMessage());
     *         throw new RuntimeException(ex);
     *     }
     * );
     * }</pre>
     *
     * @param supplier         The supplier to execute. Must not be null.
     * @param exceptionHandler The handler to manage any exception thrown by the supplier. Must not be null.
     * @param <T>              The type of result supplied by the supplier.
     * @return The result of the supplier after execution or after handling an exception.
     * @throws NullPointerException if either the supplier or the exception handler is null.
     */
    static <T> T execute(ThrowableSupplier<T> supplier, Function<Throwable, T> exceptionHandler) throws NullPointerException {
        assertNotNull(supplier, "The supplier must not be null");
        return supplier.execute(exceptionHandler);
    }
}

