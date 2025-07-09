package io.microsphere.collection;

import org.junit.jupiter.api.Test;

import static io.microsphere.collection.EmptyIterable.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EmptyIterable} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EmptyIterable
 * @since 1.0.0
 */
class EmptyIterableTest {

    @Test
    void testIterator() {
        assertSame(EmptyIterator.INSTANCE, INSTANCE.iterator());
    }
}