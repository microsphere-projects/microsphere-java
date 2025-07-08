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

import static io.microsphere.lang.function.ThrowableBiFunction.execute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ThrowableBiFunction} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ThrowableBiFunction
 * @since 1.0.0
 */
class ThrowableBiFunctionTest {

    private static final ThrowableBiFunction<String, String, String> stringConcat = (t, u) -> t + u;

    private static final ThrowableBiFunction<String, String, Integer> stringHashCode = (t, u) -> t.hashCode() + u.hashCode();

    @Test
    public void testApply() throws Throwable {
        assertEquals("mercyblitz", stringConcat.apply("mercy", "blitz"));
        assertEquals("1".hashCode() + "2".hashCode(), stringHashCode.apply("1", "2"));
    }

    @Test
    public void testExecute3() {
        assertEquals("1".hashCode() + "2".hashCode(), execute("1", "2", stringHashCode));
    }

    @Test
    public void testExecute3OnException() {
        assertThrows(RuntimeException.class, () -> execute(null, null, stringHashCode));
    }

    @Test
    public void testExecute4() {
        assertEquals(1, execute(null, null, stringHashCode, (t, u, e) -> 1));
    }

    @Test
    public void testExecute4OnException() {
        assertThrows(IllegalStateException.class, () -> execute(null, null, stringHashCode, (t, u, e) -> {
            throw new IllegalStateException(e);
        }));
    }
}
