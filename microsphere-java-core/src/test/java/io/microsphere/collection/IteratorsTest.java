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

import java.util.Iterator;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.emptyIterator;
import static io.microsphere.collection.Lists.ofList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Iterators} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Iterators
 * @since 1.0.0
 */
class IteratorsTest {

    @Test
    void testEquals() {
        List<String> values = ofList("A", "B", "C");
        Iterator<String> iterator = values.iterator();

        // the same iterator
        assertTrue(Iterators.equals(iterator, iterator));
        // equal iterators
        assertTrue(Iterators.equals(values.iterator(), values.iterator()));
        // different iterators
        assertFalse(Iterators.equals(values.iterator(), emptyIterator()));
        assertFalse(Iterators.equals(emptyIterator(), values.iterator()));
        // nulling
        assertFalse(Iterators.equals(null, values.iterator()));
        assertFalse(Iterators.equals(values.iterator(), null));

        // the same-sizz iterators
        assertFalse(Iterators.equals(values.iterator(), ofList("B", "C", "A").iterator()));
    }
}