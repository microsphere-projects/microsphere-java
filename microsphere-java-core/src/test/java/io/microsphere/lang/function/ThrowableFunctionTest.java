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
package io.microsphere.lang.function;

import org.junit.jupiter.api.Test;

import static io.microsphere.lang.function.ThrowableFunction.execute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ThrowableFunction} Test
 *
 * @since 1.0.0
 */
class ThrowableFunctionTest {

    private static final ThrowableFunction<String, String> function = t -> t;

    private static final ThrowableFunction<String, String> throwableFunction = m -> {
        throw new Exception(m);
    };

    private static final ThrowableFunction<String, Integer> stringToInteger = Integer::valueOf;

    @Test
    void testExecute1() {
        assertEquals("Hello,World", function.execute("Hello,World"));
    }

    @Test
    void testExecute1OnException() {
        assertThrows(RuntimeException.class, () -> throwableFunction.execute("For testing"));
    }

    @Test
    void testExecute2() {
        assertEquals("Hello,World", execute("Hello,World", function));
    }

    @Test
    void testExecute2OnException() {
        assertThrows(RuntimeException.class, () -> execute("For testing", throwableFunction));
    }

    @Test
    void testExecute3() {
        assertEquals("Hello,World", execute("Hello,World", function, (t, e) -> t));
    }

    @Test
    void testExecute3OnException() {
        assertThrows(RuntimeException.class, () -> execute("For testing", throwableFunction, (t, e) -> {
            throw new RuntimeException(t, e);
        }));
    }

    @Test
    void testCompose() throws Throwable {
        assertEquals(1, stringToInteger.compose(function).apply("1"));
    }

    @Test
    void testAndThen() throws Throwable {
        assertEquals(1, function.andThen(stringToInteger).apply("1"));
    }

}
