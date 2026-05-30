/**
 *
 */
package io.microsphere.filter;

/**
 * A {@link ClassFilter} implementation that always returns {@code true}.
 * <p>
 * This class is a singleton and provides a consistent filtering behavior
 * across the application lifecycle. It is typically used when all classes
 * need to be accepted without any filtering logic.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Get the singleton instance
 * ClassFilter filter = TrueClassFilter.INSTANCE;
 *
 * // Test against any Class object
 * boolean result = filter.accept(String.class); // returns true
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassFilter
 * @since 1.0.0
 */
public class TrueClassFilter implements ClassFilter {

    /**
     * Singleton {@link TrueClassFilter} instance
     */
    public static final TrueClassFilter INSTANCE = new TrueClassFilter();

    private TrueClassFilter() {

    }

    @Override
    public boolean accept(Class<?> filteredObject) {
        return true;
    }
}
