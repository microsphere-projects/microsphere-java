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

package io.microsphere.management.builder;


import org.junit.jupiter.api.Test;

import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import java.lang.reflect.Constructor;

import static io.microsphere.management.builder.MBeanConstructorInfoBuilder.constructor;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MBeanConstructorInfoBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanConstructorInfoBuilder
 * @see AbstractMBeanFeatureInfoBuilderTest
 * @since 1.0.0
 */
class MBeanConstructorInfoBuilderTest extends AbstractMBeanFeatureInfoBuilderTest<MBeanConstructorInfoBuilder> {

    @Override
    protected MBeanConstructorInfoBuilder builder() {
        return constructor();
    }

    @Test
    void testParam() {
        assertSame(this.builder, this.builder.param(String.class, builder -> {
        }));
    }

    @Test
    void testBuild() {
        Constructor<?> constructor = findConstructor(String.class, byte[].class);
        MBeanConstructorInfo info = constructor(constructor).build();
        assertMBeanConstructorInfoBuilder(info, constructor);
    }

    void assertMBeanConstructorInfoBuilder(MBeanConstructorInfo info, Constructor<?> constructor) {
        assertEquals(constructor.getName(), info.getName());
        assertEquals(constructor.toString(), info.getDescription());

        MBeanParameterInfo[] signature = info.getSignature();
        assertEquals(1, signature.length);

        MBeanParameterInfo parameterInfo = signature[0];
        assertEquals("arg0", parameterInfo.getName());
    }
}