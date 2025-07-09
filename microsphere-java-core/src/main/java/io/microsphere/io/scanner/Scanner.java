/**
 *
 */
package io.microsphere.io.scanner;


import io.microsphere.annotation.Nonnull;
import io.microsphere.filter.Filter;

import java.util.Set;

/**
 * A component that scans elements of type {@code S} and produces a set of results of type {@code R}.
 *
 * <p>Implementations of this interface are expected to process a source object of type {@code S},
 * extract or compute a set of result objects of type {@code R}, and return them in a non-null collection.
 * Optionally, a {@link Filter} can be provided to further refine the results.</p>
 *
 * @param <S> the type of the scanned source
 * @param <R> the type of the scan result
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Scanner
 * @since 1.0.0
 */
public interface Scanner<S, R> {

    /**
     * Scan source to calculate result set
     *
     * @param source scanned source
     * @return result set , non-null
     * @throws IllegalArgumentException scanned source is not legal
     * @throws IllegalStateException    scanned source's state is not valid
     */
    @Nonnull
    Set<R> scan(S source) throws IllegalArgumentException, IllegalStateException;

    /**
     * Scan source to calculate result set with {@link Filter}
     *
     * @param source scanned source
     * @param filter {@link Filter<R> filter} to accept result
     * @return result set , non-null
     * @throws IllegalArgumentException scanned source is not legal
     * @throws IllegalStateException    scanned source's state is not valid
     */
    @Nonnull
    Set<R> scan(S source, Filter<R> filter) throws IllegalArgumentException, IllegalStateException;

}
