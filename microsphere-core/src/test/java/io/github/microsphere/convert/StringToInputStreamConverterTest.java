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
package io.github.microsphere.convert;

import io.github.microsphere.io.FastByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * {@link StringToInputStreamConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StringToInputStreamConverterTest {

    private static final String text = "test";

    @Test
    public void testOnServiceLoader() throws IOException {
        Converter<String, InputStream> converter = Converter.getConverter(String.class, InputStream.class);
        assertContent(converter, text);
    }

    @Test
    public void test() throws IOException {
        Converter<String, InputStream> converter = new StringToInputStreamConverter("UTF-8");
        assertContent(converter, text);
    }

    private void assertContent(Converter<String, InputStream> converter, String content) throws IOException {
        InputStream inputStream = converter.convert(content);
        assertEquals(FastByteArrayInputStream.class, inputStream.getClass());
        String value = IOUtils.toString(inputStream, StringToInputStreamConverter.DEFAULT_CHARSET);
        assertEquals(content, value);
    }

}
