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

import java.util.Comparator;

/**
 * A {@link Comparator} implementation for comparing {@link CharSequence} objects.
 * <p>
 * This class provides a consistent and null-safe comparison mechanism for CharSequence instances,
 * primarily designed to be used in sorting and ordering operations.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * List<CharSequence> sequences = Arrays.asList("apple", "banana", "apple", null);
 * Collections.sort(sequences, CharSequenceComparator.INSTANCE);
 * }</pre>
 *
 * <p>
 * The comparison is based on lexicographical ordering similar to {@link String#compareTo(String)}.
 * Null values are considered smaller than non-null values. When both sequences are null, they are considered equal.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class CharSequenceComparator implements Comparator<CharSequence> {

    public static final CharSequenceComparator INSTANCE = new CharSequenceComparator();

    private CharSequenceComparator() {
    }

    @Override
    public int compare(CharSequence c1, CharSequence c2) {
        String string1 = toString(c1);
        String string2 = toString(c2);
        return compare(string1, string2);
    }

    private String toString(CharSequence c) {
        return c == null ? null : c.toString();
    }

    private int compare(String str1, String str2) {
        if (str1 == str2) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return 1;
        }
        return str1.compareTo(str2);
    }
}
