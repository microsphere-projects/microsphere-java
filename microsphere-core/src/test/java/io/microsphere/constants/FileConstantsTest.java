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
package io.github.microsphere.constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link FileConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class FileConstantsTest {

    @Test
    public void test() {
        assertEquals("zip", FileConstants.ZIP);
        assertEquals("jar", FileConstants.JAR);
        assertEquals("war", FileConstants.WAR);
        assertEquals("ear", FileConstants.EAR);
        assertEquals("class", FileConstants.CLASS);

        assertEquals(".zip", FileConstants.ZIP_EXTENSION);
        assertEquals(".jar", FileConstants.JAR_EXTENSION);
        assertEquals(".war", FileConstants.WAR_EXTENSION);
        assertEquals(".ear", FileConstants.EAR_EXTENSION);
        assertEquals(".class", FileConstants.CLASS_EXTENSION);
    }
}
