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
package io.microsphere.nio.charset;

import io.microsphere.util.SystemUtils;
import io.microsphere.util.Utils;

import java.nio.charset.Charset;

import static java.nio.charset.Charset.defaultCharset;

/**
 * The Utilities class for {@link Charset}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Charset
 * @since 1.0.0
 */
public abstract class CharsetUtils implements Utils {

    /**
     * The default charset looks up from the JDK system property
     * {@link SystemUtils#NATIVE_ENCODING "native.encoding"} and {@link SystemUtils#FILE_ENCODING "file.encoding"}
     * if present, or the default charset is {@code UTF-8}
     *
     * @see SystemUtils#NATIVE_ENCODING
     * @see SystemUtils#FILE_ENCODING
     */
    public static final Charset DEFAULT_CHARSET = defaultCharset();

    private CharsetUtils() {
    }
}
