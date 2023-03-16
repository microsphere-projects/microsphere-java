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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The class to convert {@link String} to {@link InputStream}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StringToInputStreamConverter implements StringConverter<InputStream> {

    /**
     * The JDK system property name : "microsphere.charset.default"
     */
    public static final String DEFAULT_CHARSET_PROPERTY_NAME = "microsphere.charset.default";

    /**
     * The default charset looks up from the JDK system property {@link #DEFAULT_CHARSET_PROPERTY_NAME "microsphere.charset.default"}
     * if present, or applies {@link StandardCharsets#US_ASCII "US-ASCII"}
     */
    public static final Charset DEFAULT_CHARSET = getDefaultCharset();

    private static Charset getDefaultCharset() {
        Charset defaultCharset = null;
        String name = System.getProperty(DEFAULT_CHARSET_PROPERTY_NAME);
        if (name == null || name.isEmpty()) {
            defaultCharset = StandardCharsets.US_ASCII;
        } else {
            defaultCharset = Charset.forName(name);
        }
        return defaultCharset;
    }

    private final Charset charset;

    public StringToInputStreamConverter() {
        this(DEFAULT_CHARSET);
    }

    public StringToInputStreamConverter(String encoding) {
        this(Charset.forName(encoding));
    }

    public StringToInputStreamConverter(Charset charset) {
        this.charset = charset;
    }


    @Override
    public InputStream convert(String source) {
        byte[] bytes = source.getBytes(charset);
        return new FastByteArrayInputStream(bytes);
    }
}
