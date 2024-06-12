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

import static io.microsphere.constants.PathConstants.BACK_SLASH;
import static io.microsphere.constants.PathConstants.BACK_SLASH_CHAR;
import static io.microsphere.constants.PathConstants.DOUBLE_SLASH;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PathConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class PathConstantsTest {

    @Test
    public void test() {
        assertEquals('/', SLASH_CHAR);
        assertEquals('\\', BACK_SLASH_CHAR);
        assertEquals("/", SLASH);
        assertEquals("//", DOUBLE_SLASH);
        assertEquals("\\", BACK_SLASH);
    }
}
