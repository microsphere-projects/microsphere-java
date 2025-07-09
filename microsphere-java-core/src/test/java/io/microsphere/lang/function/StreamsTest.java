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
package io.microsphere.lang.function;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.lang.function.Streams.filter;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.Streams.filterAllList;
import static io.microsphere.lang.function.Streams.filterAllSet;
import static io.microsphere.lang.function.Streams.filterAny;
import static io.microsphere.lang.function.Streams.filterAnyList;
import static io.microsphere.lang.function.Streams.filterAnySet;
import static io.microsphere.lang.function.Streams.filterFirst;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.lang.function.Streams.filterSet;
import static io.microsphere.lang.function.Streams.filterStream;
import static io.microsphere.lang.function.Streams.stream;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Streams} Test
 *
 * @since 1.0.0
 */
class StreamsTest {

    static final Integer[] INTEGERS = ofArray(1, 2, 3, 4, 5);

    static final List<Integer> INTEGERS_LIST = ofList(INTEGERS);

    static final Set<Integer> INTEGERS_SET = ofSet(INTEGERS);

    static final Predicate<Integer> EVEN_PREDICATE = i -> i % 2 == 0;

    static final List<Integer> EVEN_LIST = ofList(2, 4);

    static final Set<Integer> EVEN_SET = ofSet(2, 4);

    @Test
    void testStreamOnArray() {
        Stream<Integer> stream = stream(INTEGERS);
        assertEquals(INTEGERS.length, stream.count());
    }

    @Test
    void testStreamOnIterable() {
        Stream<Integer> stream = stream(INTEGERS_LIST);
        assertEquals(5, stream.count());
    }

    @Test
    void testFilterStreamOnArray() {
        Stream<Integer> stream = filterStream(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, stream.collect(toList()));
    }

    @Test
    void testFilterStreamOnIterable() {
        Stream<Integer> stream = filterStream(INTEGERS_LIST, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, stream.collect(toList()));
    }

    @Test
    void testFilterListOnArray() {
        List<Integer> list = filterList(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);
    }

    @Test
    void testFilterListOnIterable() {
        List<Integer> list = filterList(INTEGERS_LIST, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);
    }

    @Test
    public void testFilterSetOnArray() {
        Set<Integer> set = filterSet(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilterSetOnIterable() {
        Set<Integer> set = filterSet(INTEGERS_LIST, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilter() {
        List<Integer> list = filter(INTEGERS_LIST, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);

        Set<Integer> set = filter(INTEGERS_SET, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilterAll() {
        List<Integer> list = filterAll(INTEGERS_LIST, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);

        Set<Integer> set = filterAll(INTEGERS_SET, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilterAllList() {
        List<Integer> list = filterAllList(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);
    }

    @Test
    public void testFilterAllSet() {
        Set<Integer> set = filterAllSet(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilterAny() {
        List<Integer> list = filterAny(INTEGERS_LIST, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);

        Set<Integer> set = filterAny(INTEGERS_SET, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilterAnyList() {
        List<Integer> list = filterAnyList(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_LIST, list);
    }

    @Test
    public void testFilterAnySet() {
        Set<Integer> set = filterAnySet(INTEGERS, EVEN_PREDICATE);
        assertEquals(EVEN_SET, set);
    }

    @Test
    public void testFilterFirst() {
        assertEquals(2, filterFirst(INTEGERS_LIST, EVEN_PREDICATE));
        assertEquals(2, filterFirst(INTEGERS_SET, EVEN_PREDICATE));
    }

}
