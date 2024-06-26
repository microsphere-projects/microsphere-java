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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.ofList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyList;
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

    @Test
    public void testIsList() {
        assertTrue(isList(new ArrayList()));
        assertTrue(isList(emptyList()));

        assertFalse(isList(emptyEnumeration()));
    }

    @Test
    public void testOfList() {
        List<String> rawList = asList("A", "B", "C");
        List<String> list = ofList(rawList);
        assertSame(rawList, list);
        assertEquals(rawList, list);

        Set<String> rawSet = singleton("A");
        list = ofList(rawSet);
        assertEquals(newArrayList(rawSet), list);

        list = ofList((List) null);
        assertSame(emptyList(), list);
        assertEquals(emptyList(), list);
    }
}
