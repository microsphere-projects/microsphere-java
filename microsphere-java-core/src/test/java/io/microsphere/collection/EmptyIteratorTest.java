package io.microsphere.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EmptyIterator} Tes
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EmptyIterator
 * @since 1.0.0
 */
class EmptyIteratorTest {

    private EmptyIterator emptyIterator;

    @BeforeEach
    void before() {
        emptyIterator = new EmptyIterator();
    }

    @Test
    void testHasNext() {
        assertFalse(emptyIterator.hasNext());
    }

    @Test
    void testNext() {
        assertThrows(NoSuchElementException.class, emptyIterator::next);
    }

    @Test
    void testRemove() {
        assertThrows(IllegalStateException.class, emptyIterator::remove);
    }
}