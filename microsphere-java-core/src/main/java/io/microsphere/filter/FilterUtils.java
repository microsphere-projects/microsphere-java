/**
 *
 */
package io.microsphere.filter;

import io.microsphere.annotation.Nonnull;
import io.microsphere.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Utility class for working with {@link Filter} instances.
 * Provides static helper methods to apply filters to collections and iterate over filtered results.
 *
 * <p> This class also implements the marker interface {@link Utils}, indicating it is part of the utility framework.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 *     // Filtering a collection using a single filter
 *     List<String> filteredList = FilterUtils.filter(stringList, new Filter<String>() {
 *         public boolean accept(String s) {
 *             return s.startsWith("A");
 *         }
 *     });
 *
 *     // Filtering using multiple filters with AND operator
 *     List<String> filteredWithAnd = FilterUtils.filter(stringList, FilterOperator.AND,
 *         new Filter<String>() { ... },
 *         new Filter<String>() { ... }
 *     );
 *
 *     // Filtering using multiple filters with OR operator
 *     List<String> filteredWithOr = FilterUtils.filter(stringList, FilterOperator.OR,
 *         new Filter<String>() { ... },
 *         new Filter<String>() { ... }
 *     );
 * }</pre>
 *
 * @see Filter
 * @see FilterOperator
 */
public abstract class FilterUtils implements Utils {

    /**
     * Filter {@link Iterable} object to List
     *
     * @param iterable {@link Iterable} object
     * @param filter   {@link Filter} object
     * @param <E>      The filtered object type
     * @return
     */
    @Nonnull
    public static <E> List<E> filter(Iterable<E> iterable, Filter<E> filter) {
        return filter(iterable, FilterOperator.AND, filter);
    }

    /**
     * Filter {@link Iterable} object to List
     *
     * @param iterable       {@link Iterable} object
     * @param filterOperator {@link FilterOperator}
     * @param filters        {@link Filter} array objects
     * @param <E>            The filtered object type
     * @return
     */
    @Nonnull
    public static <E> List<E> filter(Iterable<E> iterable, FilterOperator filterOperator, Filter<E>... filters) {
        List<E> list = new ArrayList<E>();
        Iterator<E> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            E element = iterator.next();
            if (filterOperator.accept(element, filters)) {
                list.add(element);
            }
        }
        return unmodifiableList(list);
    }

    private FilterUtils() {
    }
}
