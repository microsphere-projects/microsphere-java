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
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Abstract Read-only {@link Iterator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ReadOnlyIterator
 * @since 1.0.0
 */
public abstract class AbstractReadOnlyIteratorTest extends AbstractTestCase {

    Iterator instance = createIterator();

    protected abstract Iterator<?> createIterator();

    @Test
    public void testHasNext() {
        assertTrue(instance.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNext() {
        assertEquals(TEST_ELEMENT, instance.next());
        // throws exception
        instance.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        instance.remove();
    }

    @Test
    public void testForEachRemaining() {
        instance.forEachRemaining(this::info);
    }

}
