package io.microsphere.util;

import org.junit.jupiter.api.Test;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.constants.SymbolConstants.SPACE_CHAR;
import static io.microsphere.constants.SymbolConstants.VERTICAL_BAR;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.CharSequenceUtilsTest.TEST_BLANK_STRING;
import static io.microsphere.util.CharSequenceUtilsTest.TEST_CSV_STRING;
import static io.microsphere.util.CharSequenceUtilsTest.TEST_EMPTY_STRING;
import static io.microsphere.util.CharSequenceUtilsTest.TEST_STRING;
import static io.microsphere.util.StringUtils.EMPTY;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.StringUtils.contains;
import static io.microsphere.util.StringUtils.containsWhitespace;
import static io.microsphere.util.StringUtils.endsWith;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.isNotBlank;
import static io.microsphere.util.StringUtils.isNumeric;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.startsWith;
import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.StringUtils.substringAfterLast;
import static io.microsphere.util.StringUtils.substringBefore;
import static io.microsphere.util.StringUtils.substringBeforeLast;
import static io.microsphere.util.StringUtils.substringBetween;
import static io.microsphere.util.StringUtils.trimAllWhitespace;
import static io.microsphere.util.StringUtils.trimLeadingWhitespace;
import static io.microsphere.util.StringUtils.trimTrailingWhitespace;
import static io.microsphere.util.StringUtils.trimWhitespace;
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
class StringUtilsTest {

    @Test
    public void testConstants() {
        assertSame(TEST_EMPTY_STRING, EMPTY);
        assertSame(TEST_EMPTY_STRING, EMPTY_STRING);
        assertSame(ArrayUtils.EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        assertEquals(0, EMPTY_STRING_ARRAY.length);
        assertArrayEquals(new String[0], EMPTY_STRING_ARRAY);
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
        assertEquals("a|b|c", replace(TEST_CSV_STRING, COMMA, VERTICAL_BAR, 100));
        assertEquals("a|b,c", replace(TEST_CSV_STRING, COMMA, VERTICAL_BAR, 1));
    }

    @Test
    public void testSubstringBetween() {
        assertNull(substringBetween(null, null));
        assertNull(substringBetween(TEST_EMPTY_STRING, null));
        assertNull(substringBetween(TEST_EMPTY_STRING, TEST_EMPTY_STRING, null));
        assertNull(substringBetween(TEST_CSV_STRING, DOT));
        assertNull(substringBetween(TEST_CSV_STRING, COMMA, DOT));

        assertNull(substringBetween(TEST_CSV_STRING, "a"));
        assertEquals(TEST_EMPTY_STRING, substringBetween(TEST_CSV_STRING, "a", COMMA));
        assertEquals(COMMA, substringBetween(TEST_CSV_STRING, "a", "b"));
        assertEquals(",b,", substringBetween(TEST_CSV_STRING, "a", "c"));
    }

    @Test
    public void testSubstringBefore() {
        assertNull(substringBefore(null, null));
        assertSame(TEST_EMPTY_STRING, substringBefore(TEST_EMPTY_STRING, null));
        assertSame(TEST_CSV_STRING, substringBefore(TEST_CSV_STRING, null));
        assertSame(TEST_EMPTY_STRING, substringBefore(TEST_CSV_STRING, TEST_EMPTY_STRING));

        assertEquals("a", substringBefore(TEST_CSV_STRING, COMMA));
        assertEquals("a,", substringBefore(TEST_CSV_STRING, "b"));
        assertEquals("a,b", substringBefore(TEST_CSV_STRING, ",c"));
        assertEquals("a,b,", substringBefore(TEST_CSV_STRING, "c"));
        assertEquals("a,b,c", substringBefore(TEST_CSV_STRING, "1"));
    }

    @Test
    public void testSubstringAfter() {
        assertNull(substringAfter(null, null));
        assertSame(TEST_EMPTY_STRING, substringAfter(TEST_EMPTY_STRING, null));
        assertSame(TEST_EMPTY_STRING, substringAfter(TEST_CSV_STRING, null));
        assertSame(TEST_CSV_STRING, substringAfter(TEST_CSV_STRING, TEST_EMPTY_STRING));

        assertEquals(",b,c", substringAfter(TEST_CSV_STRING, "a"));
        assertEquals("b,c", substringAfter(TEST_CSV_STRING, COMMA));
        assertEquals("b,c", substringAfter(TEST_CSV_STRING, "a,"));
        assertEquals(",c", substringAfter(TEST_CSV_STRING, "a,b"));
        assertEquals("c", substringAfter(TEST_CSV_STRING, "a,b,"));
        assertEquals(TEST_EMPTY_STRING, substringAfter(TEST_CSV_STRING, "a,b,c"));
        assertEquals(TEST_EMPTY_STRING, substringAfter(TEST_CSV_STRING, ",c"));
        assertEquals(TEST_EMPTY_STRING, substringAfter(TEST_CSV_STRING, "c"));
        assertEquals(TEST_EMPTY_STRING, substringAfter(TEST_CSV_STRING, "1"));
    }

    @Test
    public void testSubstringBeforeLast() {
        assertNull(substringBeforeLast(null, null));
        assertSame(TEST_EMPTY_STRING, substringBeforeLast(TEST_EMPTY_STRING, null));
        assertSame(TEST_CSV_STRING, substringBeforeLast(TEST_CSV_STRING, null));
        assertSame(TEST_CSV_STRING, substringBeforeLast(TEST_CSV_STRING, TEST_EMPTY_STRING));

        assertEquals("a,b", substringBeforeLast(TEST_CSV_STRING, COMMA));
        assertEquals("a,", substringBeforeLast(TEST_CSV_STRING, "b"));
        assertEquals("a,b", substringBeforeLast(TEST_CSV_STRING, ",c"));
        assertEquals("a,b,", substringBeforeLast(TEST_CSV_STRING, "c"));
        assertEquals(TEST_EMPTY_STRING, substringBeforeLast(TEST_CSV_STRING, "a"));
    }

    @Test
    public void testSubstringAfterLast() {
        assertNull(substringAfterLast(null, null));
        assertSame(TEST_EMPTY_STRING, substringAfterLast(TEST_EMPTY_STRING, null));
        assertSame(TEST_EMPTY_STRING, substringAfterLast(TEST_CSV_STRING, null));
        assertSame(TEST_EMPTY_STRING, substringAfterLast(TEST_CSV_STRING, TEST_EMPTY_STRING));

        assertEquals(",b,c", substringAfterLast(TEST_CSV_STRING, "a"));
        assertEquals(",c", substringAfterLast(TEST_CSV_STRING, "b"));
        assertEquals("c", substringAfterLast(TEST_CSV_STRING, COMMA));
        assertEquals(TEST_EMPTY_STRING, substringAfterLast(TEST_CSV_STRING, "c"));
    }

    @Test
    public void testIsNumeric() {
        assertFalse(isNumeric(null));
        assertFalse(isNumeric(TEST_EMPTY_STRING));
        assertFalse(isNumeric(TEST_CSV_STRING));
        assertFalse(isNumeric(TEST_EMPTY_STRING));
        assertTrue(isNumeric("1"));
        assertTrue(isNumeric("12"));
        assertTrue(isNumeric("123"));
        assertFalse(isNumeric("12a"));
    }

    @Test
    public void testContainsWhitespace() {
        assertFalse(containsWhitespace(null));
        assertFalse(containsWhitespace(TEST_EMPTY_STRING));
        assertTrue(containsWhitespace(TEST_BLANK_STRING));
        assertTrue(containsWhitespace("hello world"));
        assertTrue(containsWhitespace("hello\tworld"));
        assertFalse(containsWhitespace("helloworld"));
    }

    @Test
    public void testTrimWhitespace() {
        assertNull(trimWhitespace(null));
        assertEquals(TEST_EMPTY_STRING, trimWhitespace(TEST_EMPTY_STRING));
        assertEquals(TEST_EMPTY_STRING, trimWhitespace(TEST_BLANK_STRING));
        assertEquals("a", trimWhitespace(" a "));
        assertEquals("a", trimWhitespace(" a"));
        assertEquals("a", trimWhitespace("a "));
    }

    @Test
    public void testTrimLeadingWhitespace() {
        assertNull(trimLeadingWhitespace(null));
        assertEquals(TEST_EMPTY_STRING, trimLeadingWhitespace(TEST_EMPTY_STRING));
        assertEquals(TEST_EMPTY_STRING, trimLeadingWhitespace(TEST_BLANK_STRING));
        assertEquals("a ", trimLeadingWhitespace(" a "));
        assertEquals("a", trimLeadingWhitespace(" a"));
        assertEquals("a ", trimLeadingWhitespace("a "));
    }

    @Test
    public void testTrimTrailingWhitespace() {
        assertNull(trimTrailingWhitespace(null));
        assertEquals(TEST_EMPTY_STRING, trimTrailingWhitespace(TEST_EMPTY_STRING));
        assertEquals(" a", trimTrailingWhitespace(" a "));
        assertEquals(" a", trimTrailingWhitespace(" a"));
        assertEquals("a", trimTrailingWhitespace("a "));
    }

    @Test
    public void testTrimAllWhitespace() {
        assertNull(trimAllWhitespace(null));
        assertEquals(TEST_EMPTY_STRING, trimAllWhitespace(TEST_EMPTY_STRING));
        assertEquals(TEST_EMPTY_STRING, trimAllWhitespace(TEST_BLANK_STRING));
        assertEquals("helloworld", trimAllWhitespace("  hello  world  "));
        assertEquals("hello", trimAllWhitespace("  \t\n  h e l l o  \r\n\f"));
    }

}