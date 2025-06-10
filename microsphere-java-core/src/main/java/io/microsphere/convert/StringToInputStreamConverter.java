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

import io.microsphere.annotation.Nonnull;
import io.microsphere.io.FastByteArrayInputStream;

import java.io.InputStream;
import java.nio.charset.Charset;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static java.nio.charset.Charset.forName;

/**
 * The class to convert {@link String} to {@link InputStream}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StringToInputStreamConverter extends AbstractConverter<String, InputStream> implements StringConverter<InputStream> {

    /**
     * Singleton instance of {@link StringToInputStreamConverter}.
     */
    public static final StringToInputStreamConverter INSTANCE = new StringToInputStreamConverter();

    private final Charset charset;

    public StringToInputStreamConverter() {
        this(DEFAULT_CHARSET);
    }

    public StringToInputStreamConverter(String encoding) {
        this(forName(encoding));
    }

    public StringToInputStreamConverter(Charset charset) {
        this.charset = charset;
    }

    @Override
    protected InputStream doConvert(String source) {
        byte[] bytes = source.getBytes(charset);
        return new FastByteArrayInputStream(bytes);
    }

    /**
     * Get the {@link Charset}
     *
     * @return the {@link Charset}
     */
    @Nonnull
    public Charset getCharset() {
        return charset;
    }
}
