package io.microsphere.lang;

import org.junit.jupiter.api.Test;

import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.lang.Prioritized.MAX_PRIORITY;
import static io.microsphere.lang.Prioritized.MIN_PRIORITY;
import static io.microsphere.lang.Prioritized.NORMAL_PRIORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Prioritized}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Prioritized
 * @since 1.0.0
 */
class PrioritizedTest {

    private static final Prioritized prioritized = new Prioritized() {
    };

    private static final Prioritized minPriority = new Prioritized() {

        @Override
        public int getPriority() {
            return MIN_PRIORITY;
        }
    };

    private static final Prioritized maxPriority = new Prioritized() {

        @Override
        public int getPriority() {
            return MAX_PRIORITY;
        }
    };

    @Test
    void testDefaultMethods() {
        // test getPriority()
        assertEquals(NORMAL_PRIORITY, prioritized.getPriority());
        assertEquals(MIN_PRIORITY, minPriority.getPriority());
        assertEquals(MAX_PRIORITY, maxPriority.getPriority());

        // test compareTo
        assertEquals(0, prioritized.compareTo(prioritized));
        assertEquals(0, minPriority.compareTo(minPriority));
        assertEquals(0, maxPriority.compareTo(maxPriority));

        assertEquals(-1, prioritized.compareTo(minPriority));
        assertEquals(1, prioritized.compareTo(maxPriority));
        assertEquals(1, minPriority.compareTo(maxPriority));
    }

    @Test
    void testComparator() {
        assertEquals(0, COMPARATOR.compare(prioritized, prioritized));
        assertEquals(0, COMPARATOR.compare(minPriority, minPriority));
        assertEquals(0, COMPARATOR.compare(maxPriority, maxPriority));

        assertEquals(-1, COMPARATOR.compare(prioritized, minPriority));
        assertEquals(1, COMPARATOR.compare(prioritized, maxPriority));
        assertEquals(1, COMPARATOR.compare(minPriority, maxPriority));

        assertEquals(1, COMPARATOR.compare(null, prioritized));
        assertEquals(-1, COMPARATOR.compare(prioritized, null));
        assertEquals(0, COMPARATOR.compare(null, null));
    }

}