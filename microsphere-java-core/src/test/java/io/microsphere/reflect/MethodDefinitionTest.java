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

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
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
public class MethodDefinitionTest extends AbstractExecutableDefinitionTest<MethodDefinition> {

    private static final Logger logger = LoggerFactory.getLogger(MethodDefinitionTest.class);

    @Override
    protected List<Object> getTailConstructorArguments() {
        return ofList("log", ofArray("java.lang.String"));
    }

    @Test
    public void testGetMethodName() {
        for (MethodDefinition definition : definitions) {
            assertEquals(definition.getName(), definition.getMethodName());
        }
    }

    @Test
    public void testGetMethod() {
        for (MethodDefinition definition : definitions) {
            assertEquals(definition.getMember(), definition.getMethod());
        }
    }

    @Test
    public void testInvoke() {
        for (MethodDefinition definition : definitions) {
            assertNull(definition.invoke(this, definition.toString()));
        }
    }


    private void log(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }
}
