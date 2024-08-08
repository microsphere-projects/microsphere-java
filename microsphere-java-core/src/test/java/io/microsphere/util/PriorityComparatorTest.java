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
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import javax.annotation.Priority;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PriorityComparator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PriorityComparator
 * @see Priority
 * @since 1.0.0
 */
public class PriorityComparatorTest {

    @Test
    public void test() {
        PriorityComparator comparator = PriorityComparator.INSTANCE;
        assertEquals(0, comparator.compare(new NoPriority(), new NoPriority()));
        assertEquals(0, comparator.compare(new PriorityOneType(), new PriorityOneType()));
        assertEquals(0, comparator.compare(new PriorityTwoType(), new PriorityTwoType()));
        assertEquals(-1, comparator.compare(new NoPriority(), new PriorityOneType()));
        assertEquals(-1, comparator.compare(new PriorityOneType(), new PriorityTwoType()));
        assertEquals(1, comparator.compare(new PriorityTwoType(), new PriorityOneType()));
    }

    static class NoPriority {

    }

    @Priority(1)
    static class PriorityOneType {

    }

    @Priority(2)
    static class PriorityTwoType {

    }
}
