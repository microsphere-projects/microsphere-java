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

import java.util.LinkedList;
import java.util.Queue;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DelegatingQueue} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DelegatingQueue
 * @since 1.0.0
 */
class DelegatingQueueTest extends MutableQueueTest<Queue<Object>> {
    @Override
    protected Queue<Object> newInstance() {
        return new DelegatingQueue(newLinkedList());
    }

    @Test
    void testGetDelegate() {
        DelegatingQueue queue = (DelegatingQueue) this.instance;
        assertTrue(queue.getDelegate() instanceof LinkedList);
    }

    @Test
    void testUnwrap() {
        DelegatingQueue queue = (DelegatingQueue) this.instance;
        LinkedList linkedList = queue.unwrap(LinkedList.class);
        assertNotNull(linkedList);
    }
}
