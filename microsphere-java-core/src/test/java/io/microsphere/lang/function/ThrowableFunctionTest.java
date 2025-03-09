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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ThrowableFunction} Test
 *
 * @since 1.0.0
 */
public class ThrowableFunctionTest {

    static final ThrowableFunction<String, String> EXCEPTION_HANDLER = m -> {
        throw new Exception(m);
    };

    @Test
    public void testExecute1() {
        assertThrows(RuntimeException.class, () -> EXCEPTION_HANDLER.execute("For testing"));
    }

    @Test
    public void testExecute2() {
        assertThrows(RuntimeException.class, () -> execute("For testing", EXCEPTION_HANDLER));
    }

    @Test
    public void testExecute3() {
        assertThrows(RuntimeException.class, () -> execute("For testing", EXCEPTION_HANDLER, (t, e) -> {
            throw new RuntimeException(t, e);
        }));
    }
}
