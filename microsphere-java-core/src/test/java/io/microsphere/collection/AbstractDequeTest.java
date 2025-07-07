package io.microsphere.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractDeque} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractDeque
 * @since 1.0.0
 */
public class AbstractDequeTest {

    private static final String TEST_VALUE = "1";

    private AbstractDeque<String> deque;

    @BeforeEach
    public void init() {

        deque = new AbstractDeque<String>() {

            private String value;

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    private int cursor = 0;

                    @Override
                    public boolean hasNext() {
                        return cursor == 0;
                    }

                    @Override
                    public String next() {
                        if (cursor++ == 0) {
                            return value;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove() {
                        if (cursor <= 1) {
                            value = null;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            }

            @Override
            public Iterator<String> descendingIterator() {
                return iterator();
            }

            @Override
            public boolean offerFirst(String s) {
                if (value == null) {
                    value = s;
                    return true;
                }
                return false;
            }

            @Override
            public boolean offerLast(String s) {
                return offerFirst(s);
            }

            @Override
            public String pollFirst() {
                String s = value;
                value = null;
                return s;
            }

            @Override
            public String pollLast() {
                return pollFirst();
            }

            @Override
            public String getFirst() {
                return value;
            }

            @Override
            public String getLast() {
                return getFirst();
            }

            @Override
            public String peekFirst() {
                return value;
            }

            @Override
            public String peekLast() {
                return value;
            }

            @Override
            public boolean removeLastOccurrence(Object o) {
                if (Objects.equals(o, value)) {
                    value = null;
                    return true;
                }
                return false;
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }

    @Test
    public void testAddFirst() {
        deque.addFirst(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.addFirst(TEST_VALUE));
    }

    @Test
    public void testOfferFirst() {
        assertTrue(deque.offerFirst(TEST_VALUE));
        assertFalse(deque.offerFirst(TEST_VALUE));
    }

    @Test
    public void testAddLast() {
        deque.addLast(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.addLast(TEST_VALUE));
    }

    @Test
    public void testOfferLast() {
        assertTrue(deque.offerLast(TEST_VALUE));
        assertFalse(deque.offerLast(TEST_VALUE));
    }

    @Test
    public void testRemoveFirst() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.removeFirst());
        assertThrows(NoSuchElementException.class, () -> deque.removeFirst());
    }

    @Test
    public void testRemoveLast() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.removeLast());
        assertThrows(NoSuchElementException.class, () -> deque.removeLast());
    }

    @Test
    public void testPeekFirst() {
        assertNull(deque.peekFirst());
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.peekFirst());
        assertSame(TEST_VALUE, deque.peekFirst());
        assertSame(TEST_VALUE, deque.peekFirst());
    }

    @Test
    public void testPeekLast() {
        assertNull(deque.peekLast());
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.peekLast());
        assertSame(TEST_VALUE, deque.peekLast());
        assertSame(TEST_VALUE, deque.peekLast());
    }

    @Test
    public void testRemoveFirstOccurrence() {
        assertTrue(deque.removeFirstOccurrence(null));
        deque.add(TEST_VALUE);
        assertFalse(deque.removeFirstOccurrence(""));
        assertTrue(deque.removeFirstOccurrence(TEST_VALUE));
        assertFalse(deque.removeFirstOccurrence(TEST_VALUE));
    }

    @Test
    public void testPush() {
        deque.push(TEST_VALUE);
        assertThrows(IllegalStateException.class, () -> deque.push(TEST_VALUE));
    }

    @Test
    public void testPop() {
        deque.add(TEST_VALUE);
        assertSame(TEST_VALUE, deque.pop());
        assertThrows(NoSuchElementException.class, () -> deque.pop());
    }

    @Test
    public void testOffer() {
        assertTrue(deque.offer(TEST_VALUE));
        assertFalse(deque.offer(TEST_VALUE));
    }

    @Test
    public void testPoll() {
        assertTrue(deque.offer(TEST_VALUE));
        assertSame(TEST_VALUE, deque.poll());
        assertNull(deque.poll());
    }

    @Test
    public void testPeek() {
        assertTrue(deque.offer(TEST_VALUE));
        assertSame(TEST_VALUE, deque.peek());
        assertSame(TEST_VALUE, deque.peek());
    }
}