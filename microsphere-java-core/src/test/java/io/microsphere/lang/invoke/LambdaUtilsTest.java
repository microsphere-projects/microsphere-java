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

package io.microsphere.lang.invoke;


import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static io.microsphere.lang.invoke.LambdaUtils.consumer;
import static io.microsphere.lang.invoke.LambdaUtils.function;
import static io.microsphere.lang.invoke.LambdaUtils.lambda;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link LambdaUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LambdaUtils
 * @since 1.0.0
 */
public class LambdaUtilsTest {

    @Test
    void testFunction() throws Throwable {
        Function<String, String> toUpperCase = function(String.class, "toUpperCase");
        assertEquals("HELLO WORLD", toUpperCase.apply("hello world"));

        Function<String, Integer> lengthFunction = function(String.class, "length");
        assertEquals(11, lengthFunction.apply("hello world"));
    }

    @Test
    void testConsumer() throws Throwable {
        Consumer<String> consumer = consumer(String.class, "toString");
        assertDoesNotThrow(() -> consumer.accept("hello world"));
    }

    @Test
    void testLambda() throws Throwable {
        Function<String, String> toUpperCase = lambda(Function.class, String.class, "toUpperCase");
        assertEquals("HELLO WORLD", toUpperCase.apply("hello world"));
    }
}