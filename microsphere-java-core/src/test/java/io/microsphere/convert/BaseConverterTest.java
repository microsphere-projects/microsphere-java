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

package io.microsphere.convert;


import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.convert.Converter.convertIfPossible;
import static io.microsphere.convert.Converter.getConverter;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;
import static java.util.Objects.deepEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link Converter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractConverter
 * @since 1.0.0
 */
abstract class BaseConverterTest<S, T> extends AbstractTestCase {

    protected AbstractConverter<S, T> converter;

    protected Class<S> sourceType;

    protected Class<T> targetType;

    protected abstract AbstractConverter<S, T> createConverter();

    protected abstract S getSource() throws Throwable;

    protected abstract T getTarget() throws Throwable;

    @BeforeEach
    final void setUp() {
        this.converter = createConverter();
        this.sourceType = resolveActualTypeArgumentClass(getClass(), BaseConverterTest.class, 0);
        this.targetType = resolveActualTypeArgumentClass(getClass(), BaseConverterTest.class, 1);
    }

    @Test
    void testConvert() throws Throwable {
        Class<?> targetClass = getTarget().getClass();
        if (targetClass.isArray()) {
            assertTrue(deepEquals(getTarget(), this.converter.convert(getSource())));
        } else {
            assertEquals(getTarget(), this.converter.convert(getSource()));
        }
    }

    @Test
    void testConvertOnNull() {
        assertEquals(null, this.converter.convert(null));
    }

    @Test
    void testGetPriority() {
        // always negative
        assertTrue(this.converter.getPriority() < 0);
    }

    @Test
    void testAccept() {
        assertTrue(this.converter.accept(this.sourceType, this.targetType));
    }

    @Test
    void testGetSourceType() {
        assertEquals(this.sourceType, this.converter.getSourceType());
    }

    @Test
    void testGetTargetType() {
        assertEquals(this.targetType, this.converter.getTargetType());
    }

    @Test
    void testGetConverter() {
        assertEquals(this.converter, getConverter(this.sourceType, this.targetType));
    }

    @Test
    void testConvertIfPossible() throws Throwable {
        assertTrue(deepEquals(getTarget(), convertIfPossible(getSource(), this.targetType)));
    }
}