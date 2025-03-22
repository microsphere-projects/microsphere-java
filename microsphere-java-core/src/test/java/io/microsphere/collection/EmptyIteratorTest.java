package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static io.microsphere.collection.EmptyIterator.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EmptyIterator} Tes
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EmptyIterator
 * @since 1.0.0
 */
public class EmptyIteratorTest {

    @Test
    public void testHasNext() {
        assertFalse(INSTANCE.hasNext());
    }

    @Test
    public void testNext() {
        assertThrows(NoSuchElementException.class, INSTANCE::next);
    }

    @Test
    public void testRemove() {
        assertThrows(UnsupportedOperationException.class, INSTANCE::remove);
    }
}