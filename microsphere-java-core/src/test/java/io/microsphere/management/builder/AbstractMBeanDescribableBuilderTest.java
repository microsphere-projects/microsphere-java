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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.modelmbean.DescriptorSupport;

import static io.microsphere.reflect.JavaType.from;
import static io.microsphere.util.ClassUtils.newInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Abstract {@link MBeanDescribableBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanDescribableBuilder
 * @since 1.0.0
 */
@Disabled
abstract class AbstractMBeanDescribableBuilderTest<B extends MBeanDescribableBuilder> {

    static final String TEST_DESCRIPTION = "test-desc";

    static final DescriptorSupport TEST_DESCRIPTOR = new DescriptorSupport();

    protected B builder;

    @BeforeEach
    void setUp() {
        this.builder = builder();
    }

    protected B builder() {
        Class<B> builderClass = from(getClass())
                .as(AbstractMBeanDescribableBuilderTest.class)
                .getGenericType(0)
                .toClass();
        return newInstance(builderClass);
    }

    @Test
    void testDescription() {
        assertSame(this.builder, this.builder.description(TEST_DESCRIPTION));
        assertEquals(TEST_DESCRIPTION, this.builder.description);
    }

    @Test
    void testDescriptor() {
        assertSame(this.builder, this.builder.descriptor(TEST_DESCRIPTOR));
        assertEquals(TEST_DESCRIPTOR, this.builder.descriptor);
    }

    @Test
    abstract void testBuild();
}
