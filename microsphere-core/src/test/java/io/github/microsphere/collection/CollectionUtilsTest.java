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
package io.github.microsphere.collection;

import io.github.microsphere.AbstractTestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static io.github.microsphere.collection.CollectionUtils.isEmpty;
import static io.github.microsphere.collection.QueueUtils.emptyDeque;
import static io.github.microsphere.collection.QueueUtils.emptyQueue;
import static java.util.Collections.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@lin CollectionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CollectionUtilsTest extends AbstractTestCase {

    @Test
    public void testIsEmpty() {
        assertTrue(isEmpty(NULL_COLLECTION));
        assertTrue(isEmpty(NULL_LIST));
        assertTrue(isEmpty(NULL_SET));
        assertTrue(isEmpty(NULL_QUEUE));
        assertTrue(isEmpty(NULL_DEQUE));

        assertTrue(isEmpty(EMPTY_COLLECTION));
        assertTrue(isEmpty(EMPTY_LIST));
        assertTrue(isEmpty(EMPTY_SET));
        assertTrue(isEmpty(EMPTY_QUEUE));
        assertTrue(isEmpty(EMPTY_DEQUE));

        assertFalse(isEmpty(SINGLETON_LIST));
        assertFalse(isEmpty(SINGLETON_SET));
        assertFalse(isEmpty(SINGLETON_QUEUE));
        assertFalse(isEmpty(SINGLETON_DEQUE));
    }

    @Test
    public void testSize() {

    }
}
