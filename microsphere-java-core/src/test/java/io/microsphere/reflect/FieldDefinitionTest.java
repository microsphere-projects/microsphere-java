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

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link FieldDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FieldDefinition
 * @since 1.0.0
 */
public class FieldDefinitionTest extends AbstractMemberDefinitionTest<FieldDefinition> {

    private String name = "test-name";

    @Override
    protected List<Object> getTailConstructorArguments() {
        return ofList("name");
    }

    @Test
    public void testFieldName() {
        for (FieldDefinition definition : definitions) {
            assertEquals("name", definition.getFieldName());
            assertEquals(definition.getName(), definition.getFieldName());
        }
    }

    @Test
    public void testGetResolvedField() {
        for (FieldDefinition definition : definitions) {
            assertSame(definition.getMember(), definition.getResolvedField());
        }
    }

    @Test
    public void testGet() {
        for (FieldDefinition definition : definitions) {
            assertSame("test-name", definition.get(this));
        }
    }

    @Test
    public void testSet() {
        for (FieldDefinition definition : definitions) {
            definition.set(this, definition.toString());
            assertEquals(definition.toString(), this.name);
        }
    }

}
