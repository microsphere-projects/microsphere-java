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
package io.microsphere.net;

import io.microsphere.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;

import static io.microsphere.constants.ProtocolConstants.CLASSPATH_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.CONSOLE_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.JAR_PROTOCOL;
import static io.microsphere.net.ServiceLoaderURLStreamHandlerFactory.attach;
import static io.microsphere.net.URLUtils.clearURLStreamHandlerFactory;
import static io.microsphere.net.classpath.HandlerTest.TEST_PROPERTIES_CLASSPATH_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ServiceLoaderURLStreamHandlerFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ServiceLoaderURLStreamHandlerFactoryTest {

    @BeforeEach
    void init() {
        attach();
    }

    @AfterEach
    void destroy() {
        clearURLStreamHandlerFactory();
    }

    @Test
    void test() {
        ServiceLoaderURLStreamHandlerFactory factory = new ServiceLoaderURLStreamHandlerFactory();
        URLStreamHandler handler = factory.createURLStreamHandler(FILE_PROTOCOL);
        assertEquals("sun.net.www.protocol.file.Handler", handler.getClass().getName());

        handler = factory.createURLStreamHandler(JAR_PROTOCOL);
        assertEquals("sun.net.www.protocol.jar.Handler", handler.getClass().getName());

        handler = factory.createURLStreamHandler(CLASSPATH_PROTOCOL);
        assertEquals("io.microsphere.net.classpath.Handler", handler.getClass().getName());

        handler = factory.createURLStreamHandler(CONSOLE_PROTOCOL);
        assertEquals("io.microsphere.net.console.Handler", handler.getClass().getName());
    }

    @Test
    void testAttach() throws IOException {
        attach();
        URL url = new URL(TEST_PROPERTIES_CLASSPATH_URL);
        assertEquals("name = 测试名称", IOUtils.toString(url.openStream(), "UTF-8"));
    }
}
