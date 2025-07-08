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

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static io.microsphere.collection.CollectionUtils.singletonIterator;
import static io.microsphere.collection.CollectionUtils.toIterable;
import static io.microsphere.collection.EmptyIterator.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertSame;

/*
 * {@link IterableAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class IterableAdapterTest extends AbstractTestCase {

    @Test
    void test() {
        Iterator<String> iterator = singletonIterator(TEST_ELEMENT);
        Iterable<String> iterable = toIterable(iterator);
        assertSame(iterator, iterable.iterator());

        assertSame(INSTANCE, new IterableAdapter(null).iterator());
    }
}
