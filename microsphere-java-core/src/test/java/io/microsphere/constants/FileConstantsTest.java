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

import org.junit.jupiter.api.Test;

import static io.microsphere.constants.FileConstants.CLASS;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.constants.FileConstants.EAR;
import static io.microsphere.constants.FileConstants.EAR_EXTENSION;
import static io.microsphere.constants.FileConstants.FILE_EXTENSION;
import static io.microsphere.constants.FileConstants.FILE_EXTENSION_CHAR;
import static io.microsphere.constants.FileConstants.JAR;
import static io.microsphere.constants.FileConstants.JAR_EXTENSION;
import static io.microsphere.constants.FileConstants.JAVA_EXTENSION;
import static io.microsphere.constants.FileConstants.WAR;
import static io.microsphere.constants.FileConstants.WAR_EXTENSION;
import static io.microsphere.constants.FileConstants.ZIP;
import static io.microsphere.constants.FileConstants.ZIP_EXTENSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link FileConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class FileConstantsTest {

    @Test
    public void test() {
        assertEquals("zip", ZIP);
        assertEquals("jar", JAR);
        assertEquals("war", WAR);
        assertEquals("ear", EAR);
        assertEquals("class", CLASS);

        assertEquals('.', FILE_EXTENSION_CHAR);
        assertEquals(".", FILE_EXTENSION);

        assertEquals(".zip", ZIP_EXTENSION);
        assertEquals(".jar", JAR_EXTENSION);
        assertEquals(".war", WAR_EXTENSION);
        assertEquals(".ear", EAR_EXTENSION);
        assertEquals(".class", CLASS_EXTENSION);
        assertEquals(".java", JAVA_EXTENSION);
    }
}
