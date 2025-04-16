package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.util.CharSequenceUtils.isEmpty;
import static io.microsphere.util.CharSequenceUtils.isNotEmpty;
import static io.microsphere.util.CharSequenceUtils.length;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CharSequenceUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CharSequenceUtils
 * @since 1.0.0
 */
public class CharSequenceUtilsTest {

    static final String TEST_EMPTY_STRING = "";

    static final String TEST_BLANK_STRING = SPACE;

    static final String TEST_CSV_STRING = "a,b,c";

    static final String TEST_STRING = "testing";

    @Test
    public void testLength() {
        assertEquals(0, length(null));
        assertEquals(0, length(TEST_EMPTY_STRING));
        assertEquals(1, length(TEST_BLANK_STRING));
        assertEquals(5, length(TEST_CSV_STRING));
        assertEquals(7, length(TEST_STRING));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(isEmpty(null));
        assertTrue(isEmpty(TEST_EMPTY_STRING));
        assertFalse(isEmpty(TEST_BLANK_STRING));
        assertFalse(isEmpty(TEST_CSV_STRING));
        assertFalse(isEmpty(TEST_STRING));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(isNotEmpty(null));
        assertFalse(isNotEmpty(TEST_EMPTY_STRING));
        assertTrue(isNotEmpty(TEST_BLANK_STRING));
        assertTrue(isNotEmpty(TEST_CSV_STRING));
        assertTrue(isNotEmpty(TEST_STRING));
    }
}