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

import org.junit.Test;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ExceptionUtils.newThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ExceptionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ExceptionUtilsTest {

    @Test
    public void testNewThrowable() {
        assertThrowable(RuntimeException.class);
        assertThrowable(RuntimeException.class, "Hello,World");
        assertThrowable(RuntimeException.class, "Hello,{}", "World");
        assertThrowable(RuntimeException.class, new Throwable());
        assertThrowable(RuntimeException.class, new Throwable(), "Hello,{}", "World");
        assertThrowable(RuntimeException.class, new Throwable(), "Hello,World");
    }

    private <T extends Throwable> void assertThrowable(Class<T> throwableClass) {
        T t = newThrowable(throwableClass);
        assertNull(t.getMessage());
        assertNull(t.getCause());
    }

    private <T extends Throwable> void assertThrowable(Class<T> throwableClass, String message) {
        T t = newThrowable(throwableClass, message);
        assertEquals(message, t.getMessage());
        assertNull(t.getCause());
    }

    private <T extends Throwable> void assertThrowable(Class<T> throwableClass, String messagePattern, Object... args) {
        String message = format(messagePattern, args);
        assertThrowable(throwableClass, message);
    }

    private <T extends Throwable> void assertThrowable(Class<T> throwableClass, Throwable cause) {
        T t = newThrowable(throwableClass, cause);
        assertNull(t.getMessage());
        assertEquals(cause, t.getCause());
    }

    private <T extends Throwable> void assertThrowable(Class<T> throwableClass, Throwable cause, String messagePattern, Object... args) {
        String message = format(messagePattern, args);
        assertThrowable(throwableClass, cause, message);
    }

    private <T extends Throwable> void assertThrowable(Class<T> throwableClass, Throwable cause, String message) {
        T t = newThrowable(throwableClass, cause, message);
        assertEquals(message, t.getMessage());
        assertEquals(cause, t.getCause());
    }

}
