package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.constants.SymbolConstants.SPACE_CHAR;
import static io.microsphere.constants.SymbolConstants.VERTICAL_BAR;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.StringUtils.contains;
import static io.microsphere.util.StringUtils.endsWith;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.isEmpty;
import static io.microsphere.util.StringUtils.isNotBlank;
import static io.microsphere.util.StringUtils.isNotEmpty;
import static io.microsphere.util.StringUtils.length;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StringUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StringUtils
 * @since 1.0.0
 */
public class StringUtilsTest {

    private static final String TEST_EMPTY_STRING = "";

    private static final String TEST_BLANK_STRING = SPACE;

    private static final String TEST_CSV_STRING = "a,b,c";

    private static final String TEST_STRING = "testing";


    @Test
    public void testConstants() {
        assertSame(TEST_EMPTY_STRING, EMPTY);
        assertSame(TEST_EMPTY_STRING, EMPTY_STRING);
        assertSame(ArrayUtils.EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        assertEquals(0, EMPTY_STRING_ARRAY.length);
        assertArrayEquals(new String[0], EMPTY_STRING_ARRAY);
    }

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

    @Test
    public void testIsBlank() {
        assertTrue(isBlank(null));
        assertTrue(isBlank(TEST_EMPTY_STRING));
        assertTrue(isBlank(TEST_BLANK_STRING));
        assertFalse(isBlank(TEST_CSV_STRING));
        assertFalse(isBlank(TEST_STRING));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(isNotBlank(null));
        assertFalse(isNotBlank(TEST_EMPTY_STRING));
        assertFalse(isNotBlank(TEST_BLANK_STRING));
        assertTrue(isNotBlank(TEST_CSV_STRING));
        assertTrue(isNotBlank(TEST_STRING));
    }

    @Test
    public void testSplit() {
        String[] values = split(null, SPACE_CHAR);
        assertSame(EMPTY_STRING_ARRAY, values);

        values = split(TEST_EMPTY_STRING, SPACE);
        assertSame(EMPTY_STRING_ARRAY, values);

        values = split(TEST_BLANK_STRING, null);
        assertSame(EMPTY_STRING_ARRAY, values);

        values = split(TEST_BLANK_STRING, SPACE);
        assertArrayEquals(EMPTY_STRING_ARRAY, values);

        values = split(SPACE + SPACE, SPACE);
        assertArrayEquals(EMPTY_STRING_ARRAY, values);

        values = split(SPACE + SPACE + SPACE, SPACE);
        assertArrayEquals(EMPTY_STRING_ARRAY, values);

        values = split(TEST_CSV_STRING, COMMA_CHAR);
        assertArrayEquals(ofArray("a", "b", "c"), values);
    }

    @Test
    public void testContains() {
        assertFalse(contains(null, null));
        assertFalse(contains(TEST_EMPTY_STRING, null));
        assertTrue(contains(TEST_EMPTY_STRING, TEST_EMPTY_STRING));

        assertFalse(contains(TEST_BLANK_STRING, null));
        assertTrue(contains(TEST_BLANK_STRING, TEST_BLANK_STRING));

        assertFalse(contains(TEST_CSV_STRING, DOT));
        assertTrue(contains(TEST_CSV_STRING, COMMA));
    }


    @Test
    public void testStartsWith() {
        assertFalse(startsWith(null, null));
        assertFalse(startsWith(TEST_EMPTY_STRING, null));
        assertTrue(startsWith(TEST_EMPTY_STRING, TEST_EMPTY_STRING));

        assertFalse(startsWith(TEST_BLANK_STRING, null));
        assertTrue(startsWith(TEST_BLANK_STRING, TEST_BLANK_STRING));
        assertFalse(startsWith(TEST_CSV_STRING, DOT));
        assertFalse(startsWith(TEST_CSV_STRING, COMMA));

        assertTrue(startsWith(TEST_CSV_STRING, "a"));
        assertTrue(startsWith(TEST_CSV_STRING, "a,"));
        assertTrue(startsWith(TEST_CSV_STRING, "a,b"));
        assertTrue(startsWith(TEST_CSV_STRING, "a,b,"));
        assertTrue(startsWith(TEST_CSV_STRING, new String("a,b,c")));
        assertTrue(startsWith(TEST_CSV_STRING, TEST_CSV_STRING));
    }

    @Test
    public void testEndsWith() {
        assertFalse(endsWith(null, null));
        assertFalse(endsWith(TEST_EMPTY_STRING, null));
        assertTrue(endsWith(TEST_EMPTY_STRING, TEST_EMPTY_STRING));

        assertFalse(endsWith(TEST_BLANK_STRING, null));
        assertTrue(endsWith(TEST_BLANK_STRING, TEST_BLANK_STRING));
        assertFalse(endsWith(TEST_CSV_STRING, DOT));
        assertFalse(endsWith(TEST_CSV_STRING, COMMA));

        assertFalse(endsWith(TEST_CSV_STRING, "a"));
        assertFalse(endsWith(TEST_CSV_STRING, "a,"));
        assertFalse(endsWith(TEST_CSV_STRING, "a,b"));
        assertFalse(endsWith(TEST_CSV_STRING, "a,b,"));
        assertTrue(endsWith(TEST_CSV_STRING, "c"));
        assertTrue(endsWith(TEST_CSV_STRING, ",c"));
        assertTrue(endsWith(TEST_CSV_STRING, "b,c"));
        assertTrue(endsWith(TEST_CSV_STRING, new String("a,b,c")));
        assertTrue(endsWith(TEST_CSV_STRING, TEST_CSV_STRING));
    }

    @Test
    public void testReplace() {
        assertNull(replace(null, null, null));
        assertEquals(TEST_EMPTY_STRING, replace(TEST_EMPTY_STRING, null, null));
        assertEquals(TEST_EMPTY_STRING, replace(TEST_EMPTY_STRING, TEST_EMPTY_STRING, null));
        assertEquals(TEST_EMPTY_STRING, replace(TEST_EMPTY_STRING, TEST_EMPTY_STRING, TEST_EMPTY_STRING, 0));

        assertEquals(TEST_CSV_STRING, replace(TEST_CSV_STRING, null, null));
        assertEquals(TEST_CSV_STRING, replace(TEST_CSV_STRING, TEST_EMPTY_STRING, null));
        assertEquals(TEST_CSV_STRING, replace(TEST_CSV_STRING, COMMA, null));
        assertEquals(TEST_CSV_STRING, replace(TEST_CSV_STRING, COMMA, VERTICAL_BAR, 0));

        assertEquals("a|b|c", replace(TEST_CSV_STRING, COMMA, VERTICAL_BAR));
        assertEquals("a|b,c", replace(TEST_CSV_STRING, COMMA, VERTICAL_BAR, 1));
    }


    @Test
    public void testSubstringBetween() {
    }

    @Test
    public void testTestSubstringBetween() {
    }

    @Test
    public void testSubstringBefore() {
    }

    @Test
    public void testSubstringAfter() {
    }

    @Test
    public void testSubstringBeforeLast() {
    }

    @Test
    public void testSubstringAfterLast() {
    }
}