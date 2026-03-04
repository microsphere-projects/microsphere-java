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

package io.microsphere.io.filter;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NameFileFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NameFileFilter
 * @since 1.0.0
 */
class NameFileFilterTest {

    private static final String FILE_NAME = "Test.txt";

    private File file;

    @BeforeEach
    void setUp() {
        this.file = new File(FILE_NAME);
    }

    @Test
    void testAccept() {
        NameFileFilter filter = new NameFileFilter(FILE_NAME);
        assertTrue(filter.accept(this.file));

        filter = new NameFileFilter(FILE_NAME.toUpperCase());
        assertFalse(filter.accept(this.file));

        filter = new NameFileFilter(FILE_NAME.toLowerCase());
        assertFalse(filter.accept(this.file));
    }

    @Test
    void testAcceptOnIgnoreCaseSensitive() {
        NameFileFilter filter = new NameFileFilter(FILE_NAME, false);
        assertTrue(filter.accept(this.file));

        filter = new NameFileFilter(FILE_NAME.toUpperCase(), false);
        assertTrue(filter.accept(this.file));

        filter = new NameFileFilter(FILE_NAME.toLowerCase(), false);
        assertTrue(filter.accept(this.file));
    }
}