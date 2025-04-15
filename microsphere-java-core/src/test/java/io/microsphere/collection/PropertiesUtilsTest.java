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
package io.microsphere.collection;

import io.microsphere.junit.jupiter.api.extension.annotation.UtilsTestExtension;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.PropertiesUtils.flatProperties;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link PropertiesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@UtilsTestExtension
public class PropertiesUtilsTest {

    @Test
    public void testFlatProperties() {
        Map<String, Object> level3Properties = ofMap("f", "F");
        Map<String, Object> level2Properties = ofMap("c", "C", "d", level3Properties);
        Map<String, Object> properties = ofMap("a", "A", "b", level2Properties);
        Map<String, Object> flattenProperties = flatProperties(properties);
        assertEquals("A", flattenProperties.get("a"));
        assertEquals("C", flattenProperties.get("b.c"));
        assertEquals("F", flattenProperties.get("b.d.f"));
    }

    @Test
    public void testFlatPropertiesOnEmptyMap() {
        assertSame(emptyMap(), flatProperties(emptyMap()));
    }

    @Test
    public void testFlatPropertiesOnNull() {
        assertNull(flatProperties(null));
    }
}
