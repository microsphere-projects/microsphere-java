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
package io.github.microsphere.commons.lang.function;

import org.junit.Test;

import static io.github.microsphere.commons.lang.function.Predicates.alwaysFalse;
import static io.github.microsphere.commons.lang.function.Predicates.alwaysTrue;
import static io.github.microsphere.commons.lang.function.Predicates.and;
import static io.github.microsphere.commons.lang.function.Predicates.or;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link Predicates} Test
 *
 * @since 1.0.0
 */
public class PredicatesTest {

    @Test
    public void testAlwaysTrue() {
        assertTrue(alwaysTrue().test(null));
    }

    @Test
    public void testAlwaysFalse() {
        assertFalse(alwaysFalse().test(null));
    }

    @Test
    public void testAnd() {
        assertTrue(and(alwaysTrue(), alwaysTrue(), alwaysTrue()).test(null));
        assertFalse(and(alwaysFalse(), alwaysFalse(), alwaysFalse()).test(null));
        assertFalse(and(alwaysTrue(), alwaysFalse(), alwaysFalse()).test(null));
        assertFalse(and(alwaysTrue(), alwaysTrue(), alwaysFalse()).test(null));
    }

    @Test
    public void testOr() {
        assertTrue(or(alwaysTrue(), alwaysTrue(), alwaysTrue()).test(null));
        assertTrue(or(alwaysTrue(), alwaysTrue(), alwaysFalse()).test(null));
        assertTrue(or(alwaysTrue(), alwaysFalse(), alwaysFalse()).test(null));
        assertFalse(or(alwaysFalse(), alwaysFalse(), alwaysFalse()).test(null));
    }
}
