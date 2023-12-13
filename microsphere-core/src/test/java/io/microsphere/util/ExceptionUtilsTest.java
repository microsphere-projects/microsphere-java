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

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ExceptionUtils.create;
import static io.microsphere.util.ExceptionUtils.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExceptionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ExceptionUtilsTest {

    @Test
    public void testCreate() {
        assertCreate(RuntimeException.class);
        assertCreate(RuntimeException.class, "Hello,World");
        assertCreate(RuntimeException.class, "Hello,{}", "World");
        assertCreate(RuntimeException.class, new NullPointerException());
        assertCreate(RuntimeException.class, new NullPointerException(), "Hello,{}", "World");
        assertCreate(RuntimeException.class, new NullPointerException(), "Hello,World");
    }

    @Test
    public void testWrap() {
        // Same Exception type
        assertWrap(create(NullPointerException.class), NullPointerException.class, NullPointerException.class);

        // NullPointerException -> RuntimeException
        assertWrap(create(NullPointerException.class), RuntimeException.class, NullPointerException.class);

        // NullPointerException(String) -> RuntimeException
        assertWrap(create(NullPointerException.class, "Hello,World"), RuntimeException.class, NullPointerException.class);

        // IllegalArgumentException -> IllegalStateException(String,Throwable) -> RuntimeException
        assertWrap(create(IllegalStateException.class, "Hello,World", new IllegalArgumentException()), RuntimeException.class, IllegalArgumentException.class);

        // IllegalArgumentException -> IllegalStateException(String,Throwable) -> Exception
        assertWrap(create(IllegalStateException.class, "Hello,World", new IllegalArgumentException()), Exception.class, IllegalArgumentException.class);
    }

    private <T extends Throwable, TT extends Throwable> void assertWrap(T source, Class<TT> thrownType, Class<? extends Throwable> causeType) {
        TT throwable = wrap(source, thrownType);
        assertTrue(thrownType.isAssignableFrom(throwable.getClass()));
        if (source.getMessage() != null) {
            assertEquals(source.getMessage(), throwable.getMessage());
        }
        if (throwable.getCause() == null) {
            assertEquals(throwable.getClass(), causeType);
        } else {
            assertEquals(throwable.getCause().getClass(), causeType);
        }
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass) {
        T t = create(throwableClass);
        assertNull(t.getMessage());
        assertNull(t.getCause());
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, String message) {
        T t = create(throwableClass, message);
        assertEquals(message, t.getMessage());
        assertNull(t.getCause());
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, String messagePattern, Object... args) {
        String message = format(messagePattern, args);
        assertCreate(throwableClass, message);
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, Throwable cause) {
        T t = create(throwableClass, cause);
        assertEquals(cause.getClass().getName(), t.getMessage());
        assertEquals(cause, t.getCause());
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, Throwable cause, String messagePattern, Object... args) {
        String message = format(messagePattern, args);
        assertCreate(throwableClass, cause, message);
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, Throwable cause, String message) {
        T t = create(throwableClass, message, cause);
        assertEquals(message, t.getMessage());
        assertEquals(cause, t.getCause());
    }

}
