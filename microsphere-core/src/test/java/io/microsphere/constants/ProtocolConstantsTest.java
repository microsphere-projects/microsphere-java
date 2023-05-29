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
package io.microsphere.constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link ProtocolConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ProtocolConstantsTest {

    @Test
    public void test() {
        assertEquals("file", ProtocolConstants.FILE_PROTOCOL);
        assertEquals("http", ProtocolConstants.HTTP_PROTOCOL);
        assertEquals("https", ProtocolConstants.HTTPS_PROTOCOL);
        assertEquals("ftp", ProtocolConstants.FTP_PROTOCOL);
        assertEquals("zip", ProtocolConstants.ZIP_PROTOCOL);
        assertEquals("jar", ProtocolConstants.JAR_PROTOCOL);
        assertEquals("war", ProtocolConstants.WAR_PROTOCOL);
        assertEquals("ear", ProtocolConstants.EAR_PROTOCOL);
        assertEquals("classpath", ProtocolConstants.CLASSPATH_PROTOCOL);
    }
}
