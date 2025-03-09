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

import static io.microsphere.lang.function.ThrowableConsumer.execute;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ThrowableConsumer} Test
 *
 * @since 1.0.0
 */
public class ThrowableConsumerTest {

    private static final ThrowableConsumer<String> consumer = t -> {
    };

    private static final ThrowableConsumer<String> throwableConsumer = t -> {
        throw new Exception(t);
    };

    @Test
    public void testExecute1() {
        consumer.execute("Hello,World");
    }

    @Test
    public void testExecute1OnException() {
        assertThrows(RuntimeException.class, () -> throwableConsumer.execute("For testing"));
    }

    @Test
    public void testExecute2() {
        execute("Hello,World", consumer);
    }

    @Test
    public void testExecute2OnException() {
        assertThrows(RuntimeException.class, () -> execute("For testing", throwableConsumer));
    }

    @Test
    public void testExecute3() {
        execute("Hello,World", consumer, (t, e) -> {
        });
    }

    @Test
    public void testExecute3OnException() {
        assertThrows(RuntimeException.class, () -> execute("For testing", m -> {
            throw new Exception(m);
        }, (t, e) -> {
            throw new RuntimeException(t, e);
        }));
    }
}
