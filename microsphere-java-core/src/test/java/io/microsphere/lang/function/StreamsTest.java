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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.lang.function.Streams.filterSet;
import static io.microsphere.lang.function.Streams.filterStream;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Streams} Test
 *
 * @since 1.0.0
 */
public class StreamsTest {

    @Test
    public void testFilterStream() {
        Stream<Integer> stream = filterStream(ofList(1, 2, 3, 4, 5), i -> i % 2 == 0);
        assertEquals(ofList(2, 4), stream.collect(toList()));
    }

    @Test
    public void testFilterList() {
        List<Integer> list = filterList(ofList(1, 2, 3, 4, 5), i -> i % 2 == 0);
        assertEquals(ofList(2, 4), list);
    }

    @Test
    public void testFilterSet() {
        Set<Integer> set = filterSet(ofList(1, 2, 3, 4, 5), i -> i % 2 == 0);
        assertEquals(new LinkedHashSet<>(ofList(2, 4)), set);
    }
}
