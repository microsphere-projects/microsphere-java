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

import java.sql.SQLException;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ExceptionUtils.create;
import static io.microsphere.util.ExceptionUtils.getStackTrace;
import static io.microsphere.util.ExceptionUtils.throwTarget;
import static io.microsphere.util.ExceptionUtils.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void testConstructor() {
        assertThrows(IllegalStateException.class, () -> new ExceptionUtils() {});
    }

    @Test
    public void testGetStackTrace() {
        String stackTrace = getStackTrace(new RuntimeException("Hello,World"));
        assertNotNull(stackTrace);
    }

    @Test
    public void testWrap() {
        // Same type : NullPointerException -> NullPointerException
        assertWrap(create(NullPointerException.class), NullPointerException.class, NullPointerException.class);

        // isAssignableFrom : NullPointerException -> RuntimeException
        assertWrap(create(NullPointerException.class), RuntimeException.class, NullPointerException.class);

        // Exception(String) -> RuntimeException
        assertWrap(create(Exception.class, "Hello,World"), RuntimeException.class, Exception.class);

        // Exception(String,IllegalStateException) -> RuntimeException
        assertWrap(create(Exception.class, "Hello,World", new IllegalArgumentException()), RuntimeException.class, IllegalArgumentException.class);

        // IllegalArgumentException -> IllegalStateException(String,Throwable) -> Exception
        assertWrap(create(IllegalStateException.class, "Hello,World", new IllegalArgumentException()), Exception.class, IllegalArgumentException.class);
    }

    @Test
    public void testCreateWithType() {
        assertCreate(RuntimeException.class);
    }

    @Test
    public void testCreateWithTypeAndMessage() {
        assertCreate(RuntimeException.class, "Hello,World");
    }


    @Test
    public void testCreateWithTypeAndCause() {
        assertCreate(RuntimeException.class, new NullPointerException());
    }

    @Test
    public void testCreateWithTypeAndMessageAndCause() {
        assertCreate(RuntimeException.class, "Hello,World", new NullPointerException());
    }

    @Test
    public void testCreateWithTypeAndCauseAndMessagePatternAndArgs() {
        assertCreate(RuntimeException.class, new NullPointerException(), "Hello,{}", "World");
    }

    @Test
    public void testCreateWithArgs() {
        SQLException e = create(SQLException.class, "Hello,World", "SQL", 101);
        assertEquals("Hello,World", e.getMessage());
        assertEquals("SQL", e.getSQLState());
        assertEquals(101, e.getErrorCode());
    }

    @Test
    public void testThrowTarget() {
        assertThrows(RuntimeException.class, () -> throwTarget(new Exception("Hello,World"), RuntimeException.class));
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

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, Throwable cause) {
        T t = create(throwableClass, cause);
        assertEquals(cause.getClass().getName(), t.getMessage());
        assertEquals(cause, t.getCause());
    }

    private <T extends Throwable> void assertCreate(Class<T> throwableClass, String message, Throwable cause) {
        T t = create(throwableClass, message, cause);
        assertEquals(message, t.getMessage());
        assertEquals(cause, t.getCause());
    }


    private <T extends Throwable> void assertCreate(Class<T> throwableClass, Throwable cause, String messagePattern, Object... args) {
        String message = format(messagePattern, args);
        T t = create(throwableClass, cause, messagePattern, args);
        assertEquals(message, t.getMessage());
        assertEquals(cause, t.getCause());
    }

}
