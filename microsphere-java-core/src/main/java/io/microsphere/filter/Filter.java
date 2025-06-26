/**
 *
 */
package io.microsphere.filter;

/**
 * The {@code Filter<T>} interface represents a generic filtering mechanism that can be applied to objects of type {@code T}.
 * <p>
 * Implementations of this interface define the logic to determine whether a given object should be accepted or filtered out.
 * This interface is typically used in scenarios where conditional processing or selection of objects is required.
 * </p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * public class EvenNumberFilter implements Filter<Integer> {
 *     public boolean accept(Integer number) {
 *         return number % 2 == 0;
 *     }
 * }
 *
 * // Usage
 * Filter<Integer> filter = new EvenNumberFilter();
 * System.out.println(filter.accept(4));  // Output: true
 * System.out.println(filter.accept(5));  // Output: false
 * }</pre>
 *
 * @param <T> the type of object that this filter can evaluate
 * @author <a href="mercyblitz@gmail.com">Mercy</a>
 */
public interface Filter<T> {

    /**
     * Does accept filtered object?
     *
     * @param filteredObject filtered object
     * @return
     */
    boolean accept(T filteredObject);
}
