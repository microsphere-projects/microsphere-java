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

import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MethodDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MethodDefinition
 * @since 1.0.0
 */
public class MethodDefinitionTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodDefinitionTest.class);

    @Test
    public void test() {
        MethodDefinition md = new MethodDefinition("1.0.0", "io.microsphere.reflect.MethodDefinitionTest", "log", "java.lang.String");

        assertEquals(Version.of("1.0.0"), md.getSince());
        assertNull(md.getDeprecation());
        assertEquals(MethodDefinitionTest.class, md.getResolvedClass());
        assertEquals("log", md.getMethodName());
        assertArrayEquals(ofArray(String.class), md.getParameterTypes());
        assertTrue(md.isPresent());
        assertNotNull(md.getMethod());

        assertNull(md.invoke(this, "test"));
    }

    private void assertObjectMethods(MethodDefinition md) {
        MethodDefinition copy = new MethodDefinition(md.since, md.deprecation, md.getDeclaredClassName(), md.name, md.parameterClassNames);
    }

    private void log(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}
