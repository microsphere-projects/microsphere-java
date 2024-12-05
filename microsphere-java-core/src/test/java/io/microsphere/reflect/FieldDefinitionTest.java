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
package io.microsphere.reflect;

import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link FieldDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FieldDefinition
 * @since 1.0.0
 */
public class FieldDefinitionTest {

    private String name;

    @Test
    public void test() {
        FieldDefinition fd = FieldDefinition.of("1.0.0", FieldDefinitionTest.class, "name");
        assertEquals(Version.of("1.0.0"), fd.getSince());
        assertEquals(this.getClass(), fd.getDeclaredClass());
        assertEquals("name", fd.getFieldName());
        assertNotNull(fd.getResolvedField());
        assertNull(fd.get(this));
        assertNull(fd.set(this, "test"));
        assertEquals("test", fd.get(this));
    }
}
