package io.microsphere.filter;

import static io.microsphere.util.ArrayUtils.length;

/**
 * Enumeration representing various logical operations for combining multiple {@link Filter} instances.
 *
 * <p>Each operator defines how the results of applying multiple filters are combined to produce a final result.</p>
 *
 * <h3>Examples:</h3>
 * <ul>
 *   <li>{@code AND}: Returns true only if all filters accept the object.</li>
 *   <li>{@code OR}: Returns true if at least one filter accepts the object.</li>
 *   <li>{@code XOR}: Returns true if an odd number of filters accept the object.</li>
 * </ul>
 *
 * <p>The following example demonstrates how to use the operators to combine filters:</p>
 *
 * <pre>{@code
 * Filter<String> filter1 = (s) -> s.startsWith("A");
 * Filter<String> filter2 = (s) -> s.length() > 5;
 *
 * // Combining filters with AND operator
 * Filter<String> andFilter = FilterOperator.AND.createFilter(filter1, filter2);
 * boolean result = andFilter.accept("ApplePie"); // returns true because both conditions are satisfied.
 *
 * // Combining filters with OR operator
 * Filter<String> orFilter = FilterOperator.OR.createFilter(filter1, filter2);
 * result = orFilter.accept("Apricot"); // returns true because at least one condition is satisfied.
 *
 * // Combining filters with XOR operator
 * Filter<String> xorFilter = FilterOperator.XOR.createFilter(filter1, filter2);
 * result = xorFilter.accept("Apricot"); // returns true because only one condition is satisfied.
 * }</pre>
 *
 * @author <a href="mercyblitz@gmail.com">Mercy</a>
 * @see Filter
 * @see FilterUtils
 * @since 1.0.0
 */
public enum FilterOperator {

    /**
     * &
     */
    AND {
        @Override
        public <T> boolean accept(T filteredObject, Filter<T>... filters) {
            int length = length(filters);
            if (length == 0)
                return true;
            boolean success = true;
            for (Filter<T> filter : filters) {
                success &= filter.accept(filteredObject);
            }
            return success;
        }

    },
    /**
     * |
     */
    OR {
        @Override
        public <T> boolean accept(T filteredObject, Filter<T>... filters) {
            int length = length(filters);
            if (length == 0)
                return true;
            boolean success = false;
            for (Filter<T> filter : filters) {
                success |= filter.accept(filteredObject);
            }
            return success;
        }
    },
    /**
     * XOR
     */
    XOR {
        @Override
        public <T> boolean accept(T filteredObject, Filter<T>... filters) {
            int length = length(filters);
            if (length == 0)
                return true;
            boolean success = true;
            for (Filter<T> filter : filters) {
                success ^= filter.accept(filteredObject);
            }
            return success;
        }
    };

    /**
     * multiple {@link Filter} accept
     *
     * @param <T>            Filtered object type
     * @param filteredObject Filtered object
     * @param filters        multiple {@link Filter}
     * @return If accepted return <code>true</code>
     */
    public abstract <T> boolean accept(T filteredObject, Filter<T>... filters);

    /**
     * Create a combined {@link Filter} from multiple filters
     *
     * @param filters multiple filters
     * @param <T>
     * @return a combined {@link Filter}
     */
    public final <T> Filter<T> createFilter(final Filter<T>... filters) {
        final FilterOperator self = this;
        return new Filter<T>() {

            @Override
            public boolean accept(T filteredObject) {
                return self.accept(filteredObject, filters);
            }
        };
    }

}
