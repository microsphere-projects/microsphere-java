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

import io.microsphere.io.FastByteArrayInputStream;

import java.io.InputStream;

import static io.microsphere.convert.StringToInputStreamConverter.INSTANCE;

/**
 * {@link StringToInputStreamConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class StringToInputStreamConverterTest extends BaseConverterTest<String, InputStream> {

    @Override
    protected AbstractConverter<String, InputStream> createConverter() {
        return new StringToInputStreamConverter("UTF-8");
    }

    @Override
    protected String getSource() throws Throwable {
        return "test";
    }

    @Override
    protected InputStream getTarget() throws Throwable {
        return new FastByteArrayInputStream("test".getBytes(INSTANCE.getCharset()));
    }
}
