package io.microsphere.performance;

/**
 * {@link PerformanceAction}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see PerformanceAction
 * @since 1.0.0
 */
public interface PerformanceAction<T> {

    T execute();

}
