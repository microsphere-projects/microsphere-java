package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import static io.microsphere.collection.Lists.ofList;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Lists} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Lists
 * @since 1.0.0
 */
public class ListsTest {

    @Test
    public void testOfList() {
        assertTrue(ofList().isEmpty());
        assertEquals(asList(1, 2, 3), ofList(1, 2, 3));
    }
}