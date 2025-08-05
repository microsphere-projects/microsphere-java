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


import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static io.microsphere.util.HierarchicalClassComparator.ASCENT;
import static io.microsphere.util.HierarchicalClassComparator.DESCENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link HierarchicalClassComparator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HierarchicalClassComparator
 * @since 1.0.0
 */
class HierarchicalClassComparatorTest {

    @Test
    void testCompareOrderByAscent() {
        Comparator<Class<?>> ascent = ASCENT;
        assertEquals(-1, ascent.compare(Object.class, String.class));
        assertEquals(-1, ascent.compare(Integer.class, String.class));
        assertEquals(0, ascent.compare(String.class, String.class));
        assertEquals(1, ascent.compare(String.class, CharSequence.class));
    }

    @Test
    void testCompareOrderByDescent() {
        Comparator<Class<?>> descent = DESCENT;
        assertEquals(1, descent.compare(Object.class, String.class));
        assertEquals(-1, descent.compare(Integer.class, String.class));
        assertEquals(0, descent.compare(String.class, String.class));
        assertEquals(-1, descent.compare(String.class, CharSequence.class));
    }
}