/**
 *
 */
package io.microsphere.filter;

/**
 * A {@link Filter} for {@link Class} objects.
 *
 * <p>Implementations of this interface can be used to filter classes based on specific criteria,
 * such as whether they are interfaces, annotations, or belong to a particular package.</p>
 *
 * <h3>Example Implementation</h3>
 * <pre>{@code
 * public class MyTestClassFilter implements ClassFilter {
 *     public boolean accept(Class<?> clazz) {
 *         return clazz.isAnnotation();
 *     }
 * }
 * }</pre>
 *
 * <p>This example filters only annotation types.</p>
 *
 * @author <a href="mercyblitz@gmail.com">Mercy</a>
 * @see Class
 * @see Filter
 * @since 1.0.0
 */
@FunctionalInterface
public interface ClassFilter extends Filter<Class<?>> {
}
