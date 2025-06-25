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
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract Read-only {@link Iterator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ReadOnlyIterator
 * @since 1.0.0
 */
public abstract class ReadOnlyIteratorTest extends AbstractTestCase {

    Iterator instance = createIterator();

    protected abstract Iterator<?> createIterator();

    @Test
    public void testHasNext() {
        assertTrue(instance.hasNext());
    }

    @Test
    public void testNext() {
        assertThrows(NoSuchElementException.class, () -> {
            assertEquals(TEST_ELEMENT, instance.next());
            // throws exception
            instance.next();
        });
    }

    @Test
    public void testRemove() {
        assertThrows(UnsupportedOperationException.class, instance::remove);
    }

    @Test
    public void testForEachRemaining() {
        instance.forEachRemaining(this::log);
    }

}
