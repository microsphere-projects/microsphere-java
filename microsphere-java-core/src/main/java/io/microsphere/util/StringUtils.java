/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.util;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nullable;

import java.util.StringTokenizer;

import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.CharSequenceUtils.isEmpty;
import static io.microsphere.util.CharSequenceUtils.length;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.String.valueOf;

/**
 * The utilities class for {@link String}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class StringUtils implements Utils {

    /**
     * Represents an empty string constant : ""
     */
    public final static String EMPTY = "";

    /**
     * Represents an empty string constant: ""
     */
    public final static String EMPTY_STRING = EMPTY;

    /**
     * An empty array of String.
     */
    @Immutable
    public static final String[] EMPTY_STRING_ARRAY = ArrayUtils.EMPTY_STRING_ARRAY;

    /**
     * <p>Checks if a String is whitespace, empty, or null.</p>
     *
     * <p>Whitespace is defined by the {@link Character#isWhitespace(char)} method.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("  a  ")   = false
     * StringUtils.isBlank("abc")    = false
     * StringUtils.isBlank("\t\n\f")  = true
     * }</pre>
     *
     * @param value the String to check, may be null
     * @return {@code true} if the String is null, empty, or contains only whitespace characters
     */
    public static boolean isBlank(String value) {
        int length = length(value);
        if (length < 1) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is not blank.</p>
     *
     * <p>A string is considered not blank if it contains at least one non-whitespace character.
     * This method is the inverse of {@link #isBlank(String)}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("  a  ")   = true
     * StringUtils.isNotBlank("abc")     = true
     * StringUtils.isNotBlank("\t\n\f")  = false
     * }</pre>
     *
     * @param value the String to check, may be null
     * @return {@code true} if the String is not null, not empty, and contains at least one non-whitespace character
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * <p>Splits the provided String into an array of Strings using the specified char delimiter.</p>
     *
     * <p>A <code>null</code> or empty input String returns an empty array.
     * If the delimiter is not found, an array containing just the input String is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.split(null, ',')       = []
     * StringUtils.split("", ';')         = []
     * StringUtils.split("a,b,c", ',')    = ["a", "b", "c"]
     * StringUtils.split("a;b;c", ',')    = ["a;b;c"]
     * StringUtils.split("a,,b,c", ',')   = ["a", "", "b", "c"]
     * }</pre>
     *
     * @param value     the String to split, may be null or empty
     * @param delimiter the char used as a delimiter to split the String
     * @return an array of Strings, split by the delimiter; never null
     */
    public static String[] split(String value, char delimiter) {
        return split(value, valueOf(delimiter));
    }

    /**
     * <p>Splits the provided String into an array of Strings using the specified String delimiter.</p>
     *
     * <p>A <code>null</code> or empty input String returns an empty array.
     * If the delimiter is not found, an array containing just the input String is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.split(null, ",")       = []
     * StringUtils.split("", ";")         = []
     * StringUtils.split("a,b,c", ",")    = ["a", "b", "c"]
     * StringUtils.split("a;b;c", ",")    = ["a;b;c"]
     * StringUtils.split("a,,b,c", ",")   = ["a", "", "b", "c"]
     * }</pre>
     *
     * @param value     the String to split, may be null or empty
     * @param delimiter the String used as a delimiter to split the String, may be null or empty
     * @return an array of Strings, split by the delimiter; never null
     */
    public static String[] split(String value, String delimiter) {
        if (isEmpty(value) || isEmpty(delimiter)) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(value, delimiter);
        return (String[]) asArray(stringTokenizer, String.class);
    }

    /**
     * <p>Checks if a CharSequence contains another CharSequence.</p>
     *
     * <p>This method is case-sensitive and uses the {@link String#contains(CharSequence)} method.
     * A <code>null</code> CharSequence returns <code>false</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.contains(null, null)     = false
     * StringUtils.contains(null, "abc")    = false
     * StringUtils.contains("abc", null)    = false
     * StringUtils.contains("", "")         = true
     * StringUtils.contains("abc", "")      = true
     * StringUtils.contains("abc", "a")     = true
     * StringUtils.contains("abc", "z")     = false
     * StringUtils.contains("abc", "abc")   = true
     * }</pre>
     *
     * @param value the CharSequence to check, may be null
     * @param part  the CharSequence to search for, may be null
     * @return {@code true} if the value contains the part as a subsequence, case-sensitive
     */
    public static boolean contains(String value, CharSequence part) {
        if (value != null && part != null) {
            return value == part ? true : value.contains(part);
        }
        return false;
    }

    /**
     * <p>Checks if a String starts with another String.</p>
     *
     * <p>This method is case-sensitive and uses the {@link String#startsWith(String)} method.
     * A <code>null</code> reference for either parameter returns <code>false</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.startsWith(null, null)     = false
     * StringUtils.startsWith(null, "abc")    = false
     * StringUtils.startsWith("abc", null)    = false
     * StringUtils.startsWith("", "")         = true
     * StringUtils.startsWith("abc", "")      = true
     * StringUtils.startsWith("abc", "a")     = true
     * StringUtils.startsWith("abc", "ab")    = true
     * StringUtils.startsWith("abc", "z")     = false
     * StringUtils.startsWith("abc", "abcd")  = false
     * }</pre>
     *
     * @param value the String to check, may be null
     * @param part  the String prefix to search for, may be null
     * @return {@code true} if the value starts with the provided part, case-sensitive
     */
    public static boolean startsWith(String value, String part) {
        if (value != null && part != null) {
            return value == part ? true : value.startsWith(part);
        }
        return false;
    }

    /**
     * <p>Checks if a String ends with another String.</p>
     *
     * <p>This method is case-sensitive and uses the {@link String#endsWith(String)} method.
     * A <code>null</code> reference for either parameter returns <code>false</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.endsWith(null, null)     = false
     * StringUtils.endsWith(null, "abc")    = false
     * StringUtils.endsWith("abc", null)    = false
     * StringUtils.endsWith("", "")         = true
     * StringUtils.endsWith("abc", "")      = true
     * StringUtils.endsWith("abc", "c")     = true
     * StringUtils.endsWith("abc", "bc")    = true
     * StringUtils.endsWith("abc", "abc")   = true
     * StringUtils.endsWith("abc", "d")     = false
     * StringUtils.endsWith("abc", "abcd")  = false
     * }</pre>
     *
     * @param value the String to check, may be null
     * @param part  the String suffix to search for, may be null
     * @return {@code true} if the value ends with the provided part, case-sensitive
     */
    public static boolean endsWith(String value, String part) {
        if (value != null && part != null) {
            return value == part ? true : value.endsWith(part);
        }
        return false;
    }

    // Copy following stuffs from Apache Commons Lang StringUtils

    /**
     * Represents a failed index search.
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * <p>Replaces all occurrences of a String within another String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * }</pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     * @see #replace(String text, String searchString, String replacement, int max)
     */
    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * }</pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     */
    public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = (increase < 0 ? 0 : increase);
        increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
        StringBuffer buf = new StringBuffer(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    /**
     * <p>Gets the String that is nested in between two instances of the
     * same String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * A <code>null</code> tag returns <code>null</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.substringBetween(null, *)            = null
     * StringUtils.substringBetween("", "")             = ""
     * StringUtils.substringBetween("", "tag")          = null
     * StringUtils.substringBetween("tagabctag", null)  = null
     * StringUtils.substringBetween("tagabctag", "")    = ""
     * StringUtils.substringBetween("tagabctag", "tag") = "abc"
     * }</pre>
     *
     * @param str the String containing the substring, may be null
     * @param tag the String before and after the substring, may be null
     * @return the substring, <code>null</code> if no match
     */
    public static String substringBetween(String str, String tag) {
        return substringBetween(str, tag, tag);
    }

    /**
     * <p>Gets the String that is nested in between two Strings.
     * Only the first match is returned.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * A <code>null</code> open/close returns <code>null</code> (no match).
     * An empty ("") open and close returns an empty string.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.substringBetween("wx[b]yz", "[", "]") = "b"
     * StringUtils.substringBetween(null, *, *)          = null
     * StringUtils.substringBetween(*, null, *)          = null
     * StringUtils.substringBetween(*, *, null)          = null
     * StringUtils.substringBetween("", "", "")          = ""
     * StringUtils.substringBetween("", "", "]")         = null
     * StringUtils.substringBetween("", "[", "]")        = null
     * StringUtils.substringBetween("yabcz", "", "")     = ""
     * StringUtils.substringBetween("yabcz", "y", "z")   = "abc"
     * StringUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
     * }</pre>
     *
     * @param str   the String containing the substring, may be null
     * @param open  the String before the substring, may be null
     * @param close the String after the substring, may be null
     * @return the substring, <code>null</code> if no match
     */
    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            int end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    /**
     * <p>Gets the substring before the first occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A <code>null</code> string input will return <code>null</code>.
     * An empty ("") string input will return the empty string.
     * A <code>null</code> separator will return the input string.</p>
     *
     * <p>If nothing is found, the string input is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.substringBefore(null, *)      = null
     * StringUtils.substringBefore("", *)        = ""
     * StringUtils.substringBefore("abc", "a")   = ""
     * StringUtils.substringBefore("abcba", "b") = "a"
     * StringUtils.substringBefore("abc", "c")   = "ab"
     * StringUtils.substringBefore("abc", "d")   = "abc"
     * StringUtils.substringBefore("abc", "")    = ""
     * StringUtils.substringBefore("abc", null)  = "abc"
     * }</pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the first occurrence of the separator,
     * <code>null</code> if null String input
     */
    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.length() == 0) {
            return EMPTY;
        }
        int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * <p>Gets the substring after the first occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A <code>null</code> string input will return <code>null</code>.
     * An empty ("") string input will return the empty string.
     * A <code>null</code> separator will return the empty string if the
     * input string is not <code>null</code>.</p>
     *
     * <p>If nothing is found, the empty string is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.substringAfter(null, *)      = null
     * StringUtils.substringAfter("", *)        = ""
     * StringUtils.substringAfter(*, null)      = ""
     * StringUtils.substringAfter("abc", "a")   = "bc"
     * StringUtils.substringAfter("abcba", "b") = "cba"
     * StringUtils.substringAfter("abc", "c")   = ""
     * StringUtils.substringAfter("abc", "d")   = ""
     * StringUtils.substringAfter("abc", "")    = "abc"
     * }</pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the first occurrence of the separator,
     * <code>null</code> if null String input
     */
    public static String substringAfter(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return EMPTY;
        }
        int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * <p>Gets the substring before the last occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A <code>null</code> string input will return <code>null</code>.
     * An empty ("") string input will return the empty string.
     * An empty or <code>null</code> separator will return the input string.</p>
     *
     * <p>If nothing is found, the string input is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.substringBeforeLast(null, *)      = null
     * StringUtils.substringBeforeLast("", *)        = ""
     * StringUtils.substringBeforeLast("abcba", "b") = "abc"
     * StringUtils.substringBeforeLast("abc", "c")   = "ab"
     * StringUtils.substringBeforeLast("a", "a")     = ""
     * StringUtils.substringBeforeLast("a", "z")     = "a"
     * StringUtils.substringBeforeLast("a", null)    = "a"
     * StringUtils.substringBeforeLast("a", "")      = "a"
     * }</pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the last occurrence of the separator,
     * <code>null</code> if null String input
     */
    public static String substringBeforeLast(String str, String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * <p>Gets the substring after the last occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A <code>null</code> string input will return <code>null</code>.
     * An empty ("") string input will return the empty string.
     * An empty or <code>null</code> separator will return the empty string if
     * the input string is not <code>null</code>.</p>
     *
     * <p>If nothing is found, the empty string is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.substringAfterLast(null, *)      = null
     * StringUtils.substringAfterLast("", *)        = ""
     * StringUtils.substringAfterLast(*, "")        = ""
     * StringUtils.substringAfterLast(*, null)      = ""
     * StringUtils.substringAfterLast("abc", "a")   = "bc"
     * StringUtils.substringAfterLast("abcba", "b") = "a"
     * StringUtils.substringAfterLast("abc", "c")   = ""
     * StringUtils.substringAfterLast("a", "a")     = ""
     * StringUtils.substringAfterLast("a", "z")     = ""
     * }</pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the last occurrence of the separator,
     * <code>null</code> if null String input
     */
    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return EMPTY;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == (str.length() - separator.length())) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * <p>Checks if the String contains only unicode digits.
     * A decimal point is not a unicode digit and returns false.</p>
     *
     * <p><code>null</code> will return <code>false</code>.
     * An empty String (length()=0) will return <code>true</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = true
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * }</pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if only contains digits, and is non-null
     */
    public static boolean isNumeric(String str) {
        int sz = length(str);
        if (sz == 0) {
            return false;
        }
        for (int i = 0; i < sz; i++) {
            if (isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given {@code String} contains any whitespace characters.
     *
     * <p>
     * A whitespace character is defined as any character that returns {@code true} when passed to
     * {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code containsWhitespace(null)} returns {@code false}</li>
     *     <li>{@code containsWhitespace("")} returns {@code false}</li>
     *     <li>{@code containsWhitespace("hello world")} returns {@code true}</li>
     *     <li>{@code containsWhitespace("hello\tworld")} returns {@code true}</li>
     *     <li>{@code containsWhitespace("helloworld")} returns {@code false}</li>
     * </ul>
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the provided sequence is not empty and contains at least one whitespace character;
     * otherwise, {@code false}
     */
    public static boolean containsWhitespace(@Nullable String str) {
        return CharSequenceUtils.containsWhitespace(str);
    }

    /**
     * Trims leading and trailing whitespace from the given {@code String}.
     *
     * <p>
     * This method removes whitespace characters (as defined by
     * {@link Character#isWhitespace(char)}) from the beginning and end of the input string.
     * If the input is {@code null} or empty, it will be returned as-is.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code trimWhitespace(null)} returns {@code null}</li>
     *     <li>{@code trimWhitespace("")} returns {@code ""}</li>
     *     <li>{@code trimWhitespace("  abc  ")} returns {@code "abc"}</li>
     *     <li>{@code trimWhitespace("abc")} returns {@code "abc"}</li>
     *     <li>{@code trimWhitespace("   abc def   ")} returns {@code "abc def"}</li>
     * </ul>
     *
     * @param str the {@code String} to trim (may be {@code null})
     * @return a new {@code String} with leading and trailing whitespace removed,
     * or the original if it is {@code null} or empty
     */
    public static String trimWhitespace(String str) {
        return trimWhitespace(str, true, true);
    }

    /**
     * Trims leading whitespace from the given {@code String}.
     *
     * <p>
     * This method removes whitespace characters (as defined by
     * {@link Character#isWhitespace(char)}) from the beginning of the input string.
     * If the input is {@code null} or empty, it will be returned as-is.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code trimLeadingWhitespace(null)} returns {@code null}</li>
     *     <li>{@code trimLeadingWhitespace("")} returns {@code ""}</li>
     *     <li>{@code trimLeadingWhitespace("  abc  ")} returns {@code "abc  "}</li>
     *     <li>{@code trimLeadingWhitespace("abc")} returns {@code "abc"}</li>
     *     <li>{@code trimLeadingWhitespace("   abc def   ")} returns {@code "abc def   "}</li>
     * </ul>
     *
     * @param str the {@code String} to trim (may be {@code null})
     * @return a new {@code String} with leading whitespace removed,
     * or the original if it is {@code null} or empty
     * @see Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        return trimWhitespace(str, true, false);
    }

    /**
     * Trims trailing whitespace from the given {@code String}.
     *
     * <p>
     * This method removes whitespace characters (as defined by
     * {@link Character#isWhitespace(char)}) from the end of the input string.
     * If the input is {@code null} or empty, it will be returned as-is.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code trimTrailingWhitespace(null)} returns {@code null}</li>
     *     <li>{@code trimTrailingWhitespace("")} returns {@code ""}</li>
     *     <li>{@code trimTrailingWhitespace("  abc  ")} returns {@code "  abc"}</li>
     *     <li>{@code trimTrailingWhitespace("abc")} returns {@code "abc"}</li>
     *     <li>{@code trimTrailingWhitespace("abc def   ")} returns {@code "abc def"}</li>
     * </ul>
     *
     * @param str the {@code String} to trim (may be {@code null})
     * @return a new {@code String} with trailing whitespace removed,
     * or the original if it is {@code null} or empty
     * @see Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        return trimWhitespace(str, false, true);
    }

    /**
     * Trims all whitespace characters from the given {@link String}.
     *
     * <p>
     * This method removes all whitespace characters (as defined by {@link Character#isWhitespace(char)})
     * from the beginning, end, and middle of the input sequence. If the input is {@code null} or empty,
     * it will be returned as-is.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code trimAllWhitespace(null)} returns {@code null}</li>
     *     <li>{@code trimAllWhitespace("")} returns {@code ""}</li>
     *     <li>{@code trimAllWhitespace("  hello  world  ")} returns {@code "helloworld"}</li>
     *     <li>{@code trimAllWhitespace("  \t\n  h e l l o  \r\n\f")} returns {@code "hello"}</li>
     * </ul>
     *
     * @param str the {@link String} to trim (may be {@code null})
     * @return a new {@link String} with all whitespace characters removed, or the original if none exist
     */
    public static String trimAllWhitespace(String str) {
        return isEmpty(str) ? str : CharSequenceUtils.trimAllWhitespace(str).toString();
    }

    /**
     * Capitalizes the first character of the given String, converting it to uppercase using
     * {@link Character#toUpperCase(char)}. The rest of the characters remain unchanged.
     *
     * <p>
     * A {@code null} or empty input will be returned as-is.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code capitalize(null)} returns {@code null}</li>
     *     <li>{@code capitalize("")} returns {@code ""}</li>
     *     <li>{@code capitalize("hello")} returns {@code "Hello"}</li>
     *     <li>{@code capitalize("HELLO")} returns {@code "HELLO"}</li>
     *     <li>{@code capitalize("hELLO")} returns {@code "HELLO"}</li>
     * </ul>
     *
     * @param str the String to capitalize (may be {@code null})
     * @return a new String with the first character capitalized, or the original if it is {@code null} or empty
     */
    public static String capitalize(String str) {
        return changeFirstCharacter(str, true);
    }

    /**
     * Uncapitalizes the first character of the given String, converting it to lowercase using
     * {@link Character#toLowerCase(char)}. The rest of the characters remain unchanged.
     *
     * <p>
     * A {@code null} or empty input will be returned as-is.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <ul>
     *     <li>{@code uncapitalize(null)} returns {@code null}</li>
     *     <li>{@code uncapitalize("")} returns {@code ""}</li>
     *     <li>{@code uncapitalize("Hello")} returns {@code "hello"}</li>
     *     <li>{@code uncapitalize("HELLO")} returns {@code "hELLO"}</li>
     *     <li>{@code uncapitalize("hELLO")} returns {@code "hELLO"}</li>
     * </ul>
     *
     * @param str the String to uncapitalize (may be {@code null})
     * @return a new String with the first character uncapitalized, or the original if it is {@code null} or empty
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacter(str, false);
    }

    static String changeFirstCharacter(String str, boolean capitalize) {
        int len = length(str);
        if (len < 1) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = toUpperCase(baseChar);
        } else {
            updatedChar = toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars);
    }

    static String trimWhitespace(String str, boolean includeLeading, boolean includeTrailing) {
        int len = length(str);
        if (len < 1) {
            return str;
        }
        int beginIndex = 0;
        int endIndex = len - 1;

        if (includeLeading) {
            while (beginIndex <= endIndex && isWhitespace(str.charAt(beginIndex))) {
                beginIndex++;
            }
        }

        if (includeTrailing) {
            while (endIndex > beginIndex && isWhitespace(str.charAt(endIndex))) {
                endIndex--;
            }
        }

        return str.substring(beginIndex, endIndex + 1);
    }

    private StringUtils() {
        super();
    }
}
