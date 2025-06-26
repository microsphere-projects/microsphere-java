package io.microsphere.lang.function;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.microsphere.util.Assert.assertNotNull;

/**
 * A functional interface similar to {@link Supplier}, but allows the {@code get()} method to throw a
 * {@link Throwable}. This is useful for functional constructs where operations may throw checked exceptions that need
 * to be handled or rethrown.
 *
 * <p>Example usage:</p>
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
     * Executes {@link ThrowableSupplier} with {@link #handleException(Throwable) the default exception handling}
     *
     * @param supplier {@link ThrowableSupplier}
     * @param <T>      the supplied type
     * @return the result after execution
     * @throws NullPointerException if <code>supplier</code> is <code>null</code>
     */
    static <T> T execute(ThrowableSupplier<T> supplier) throws NullPointerException {
        return execute(supplier, supplier::handleException);
    }

    /**
     * Executes {@link ThrowableSupplier} with the customized {@link Throwable exception} handling
     *
     * @param supplier         {@link ThrowableSupplier}
     * @param exceptionHandler the handler to handle any {@link Throwable exception} that the {@link #get()} method throws
     * @param <T>              the supplied type
     * @return the result after execution
     * @throws NullPointerException if <code>supplier</code> or <code>exceptionHandler</code> is <code>null</code>
     */
    static <T> T execute(ThrowableSupplier<T> supplier, Function<Throwable, T> exceptionHandler) throws NullPointerException {
        assertNotNull(supplier, "The supplier must not be null");
        return supplier.execute(exceptionHandler);
    }
}

