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

import static io.microsphere.convert.StringToCharacterConverter.INSTANCE;
import static java.lang.Character.valueOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link StringToCharacterConverter} Test
 *
 * @since 1.0.0
 */
class StringToCharacterConverterTest extends BaseConverterTest<String, Character> {

    @Override
    protected AbstractConverter<String, Character> createConverter() {
        return INSTANCE;
    }

    @Override
    protected String getSource() throws Throwable {
        return "t";
    }

    @Override
    protected Character getTarget() throws Throwable {
        return valueOf('t');
    }

    @Test
    void testConvertOnEmpty() {
        assertNull(this.converter.convert(""));
    }

    @Test
    void testConvertOnFailed() {
        assertThrows(IllegalArgumentException.class, () -> this.converter.convert("123"));
    }
}
