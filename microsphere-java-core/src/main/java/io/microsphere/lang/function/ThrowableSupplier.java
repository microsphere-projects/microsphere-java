package io.microsphere.lang.function;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

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
        requireNonNull(exceptionHandler, "The exceptionHandler must not be null");
        T result = null;
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
        requireNonNull(supplier, "The supplier must not be null");
        return supplier.execute();
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
        requireNonNull(supplier, "The supplier must not be null");
        return supplier.execute(exceptionHandler);
    }
}

