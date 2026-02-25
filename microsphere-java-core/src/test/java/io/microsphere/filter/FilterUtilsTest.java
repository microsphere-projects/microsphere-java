/**
 *
 */
package io.microsphere.filter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.filter.FilterOperator.AND;
import static io.microsphere.filter.FilterOperator.OR;
import static io.microsphere.filter.FilterUtils.filter;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link FilterUtils} Test Case
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see FilterUtilsTest
 * @since 1.0.0
 */
class FilterUtilsTest {

    @Test
    void testFilter() {
        List<Integer> integerList = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        List<Integer> result = filter(integerList, AND, filteredObject -> filteredObject % 2 == 0);

        assertEquals(asList(0, 2, 4, 6, 8), result);

        result = filter(integerList, filteredObject -> filteredObject % 2 == 0);

        assertEquals(asList(0, 2, 4, 6, 8), result);

        result = filter(integerList, OR, filteredObject -> filteredObject % 2 == 1);

        assertEquals(asList(1, 3, 5, 7, 9), result);
    }
}