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


import io.microsphere.management.CacheControlMBean;
import org.junit.jupiter.api.Test;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.lang.reflect.Method;

import static io.microsphere.management.builder.MBeanOperationInfoBuilder.Impact.ACTION;
import static io.microsphere.management.builder.MBeanOperationInfoBuilder.operation;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MBeanOperationInfoBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanOperationInfoBuilder
 * @see AbstractMBeanFeatureInfoBuilderTest
 * @since 1.0.0
 */
class MBeanOperationInfoBuilderTest extends AbstractMBeanFeatureInfoBuilderTest<MBeanOperationInfoBuilder> {

    @Override
    protected MBeanOperationInfoBuilder builder() {
        Method method = findMethod(CacheControlMBean.class, "getCacheSize");
        return operation(method).description(TEST_DESCRIPTION);
    }

    @Test
    void testBuild() {
        MBeanOperationInfo info = this.builder.build();
        assertEquals("getCacheSize", info.getName());
        assertEquals("long", info.getReturnType());
        assertArrayEquals(new MBeanParameterInfo[0], info.getSignature());
        assertEquals(TEST_DESCRIPTION, info.getDescription());

        ImmutableDescriptor descriptor = (ImmutableDescriptor) info.getDescriptor();
        assertArrayEquals(ofArray("bytes"), descriptor.getFieldValues("units"));
    }

    @Test
    void testParam() {
        String methodName = "testParam";
        Method method = findMethod(MBeanOperationInfoBuilderTest.class, methodName);
        MBeanOperationInfoBuilder builder = operation(method)
                .impact(ACTION)
                .param(long.class, b -> b.name(TEST_NAME).description(TEST_DESCRIPTION));

        MBeanOperationInfo build = builder.build();
        assertEquals(methodName, build.getName());
        assertEquals(ACTION.getValue(), build.getImpact());

        MBeanParameterInfo[] signature = build.getSignature();
        assertEquals(1, signature.length);

        MBeanParameterInfo info = signature[0];
        assertEquals(TEST_NAME, info.getName());
        assertEquals(TEST_DESCRIPTION, info.getDescription());
    }
}