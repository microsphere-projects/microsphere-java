/**
 *
 */
package io.microsphere.filter;

import io.microsphere.util.BaseUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * {@link Filter} utility class
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see FilterUtils
 * @since 1.0.0
 */
public abstract class FilterUtils extends BaseUtils {

    private FilterUtils() {
    }

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
}
