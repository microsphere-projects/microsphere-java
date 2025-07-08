/**
 *
 */
package io.microsphere.filter;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link FilterUtils} Test Case
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see FilterUtilsTest
 * @since 1.0.0
 */
class FilterUtilsTest extends AbstractTestCase {

    @Test
    public void testFilter() {
        List<Integer> integerList = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        List<Integer> result = FilterUtils.filter(integerList, FilterOperator.AND, new Filter<Integer>() {
            @Override
            public boolean accept(Integer filteredObject) {
                return filteredObject % 2 == 0;
            }
        });

        assertEquals(asList(0, 2, 4, 6, 8), result);

        result = FilterUtils.filter(integerList, new Filter<Integer>() {
            @Override
            public boolean accept(Integer filteredObject) {
                return filteredObject % 2 == 0;
            }
        });

        assertEquals(asList(0, 2, 4, 6, 8), result);

        result = FilterUtils.filter(integerList, FilterOperator.OR, new Filter<Integer>() {
            @Override
            public boolean accept(Integer filteredObject) {
                return filteredObject % 2 == 1;
            }
        });

        assertEquals(asList(1, 3, 5, 7, 9), result);
    }
}
