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
package io.microsphere.collection;

import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.ListUtils.of;
import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ListUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListUtils
 * @since 1.0.0
 */
public class ListUtilsTest {

    private static final Logger logger = getLogger(ListUtilsTest.class);

    @Test
    public void testIsList() {
        assertTrue(isList(new ArrayList()));
        assertTrue(isList(emptyList()));

        assertFalse(isList(emptyEnumeration()));
    }

    @Test
    public void testOf() {
        List<String> list = of();
        assertTrue(list.isEmpty());

        List<String> rawList = asList("A", "B", "C");
        list = of("A", "B", "C");
        assertEquals(rawList, list);
    }

    @Test
    public void testOfList() {
        List<String> rawList = asList("A", "B", "C");
        List<String> list = ofList(rawList);
        assertSame(rawList, list);
        assertEquals(rawList, list);

        list = ofList("A", "B", "C");
        assertEquals(rawList, list);

        Set<String> rawSet = singleton("A");
        list = ofList(rawSet);
        assertEquals(newArrayList(rawSet), list);

        list = ofList(enumeration(rawList));
        assertEquals(rawList, list);

        list = ofList((List) null);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList((Iterable) null);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList((Iterator) null);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);

        list = ofList((Enumeration) null);
        assertEquals(emptyList(), list);
    }

    @Test
    public void testNewArrayList() {
        assertEquals(newArrayList(), newArrayList(1));
        assertEquals(newArrayList(), newArrayList(emptyEnumeration()));
        assertEquals(newArrayList(newArrayList()), newArrayList(emptyIterator()));
    }

    @Test
    public void testNewLinkedList() {
        assertEquals(emptyList(), newLinkedList());
        assertEquals(newLinkedList(), newLinkedList(emptyEnumeration()));
        assertEquals(newLinkedList(newArrayList()), newLinkedList(emptyIterator()));
    }

    @Test
    public void testForEach() {
        List<String> list = asList("A", "B", "C");
        forEach(list, (index, value) -> {
            logger.trace("forEach(index = {} , value = '{}')", index, value);
        });
        forEach(list, (value) -> {
            logger.trace("forEach(value = '{}')", value);
        });
    }
}
