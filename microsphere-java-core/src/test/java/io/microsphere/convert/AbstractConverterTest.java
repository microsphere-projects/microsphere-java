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

import org.junit.jupiter.api.Test;

import static io.microsphere.convert.Converter.getConverter;
import static io.microsphere.lang.Prioritized.NORMAL_PRIORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link AbstractConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractConverter
 * @since 1.0.0
 */
class AbstractConverterTest extends BaseConverterTest<Object, Object> {

    @Override
    protected AbstractConverter<Object, Object> createConverter() {
        return new AbstractConverter<Object, Object>() {

            @Override
            protected Object doConvert(Object source) throws Throwable {
                return source;
            }

            @Override
            protected Integer resolvePriority() {
                return null;
            }
        };
    }

    @Override
    protected Object getSource() throws Throwable {
        return "test";
    }

    @Override
    protected Object getTarget() throws Throwable {
        return "test";
    }

    @Test
    void testGetPriority() {
        assertEquals(NORMAL_PRIORITY, converter.getPriority());
    }

    @Test
    void testConvertIfPossible() throws Throwable {

    }

    @Test
    void testGetConverter() {
        assertNotEquals(this.converter, getConverter(sourceType, String.class));
        assertNotEquals(this.converter, getConverter(String.class, String.class));
        assertNotEquals(this.converter, new Object());
    }

}
