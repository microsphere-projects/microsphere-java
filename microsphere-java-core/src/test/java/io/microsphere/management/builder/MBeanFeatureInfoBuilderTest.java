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
import org.junit.jupiter.api.Test;

import javax.management.modelmbean.DescriptorSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MBeanFeatureInfoBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanFeatureInfoBuilder
 * @since 1.0.0
 */
class MBeanFeatureInfoBuilderTest {

    private MBeanFeatureInfoBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = new MBeanFeatureInfoBuilder();
    }

    @Test
    void testName() {
        assertSame(this.builder, this.builder.name("test"));
        assertEquals("test", this.builder.name);
    }

    @Test
    void testDescription() {
        assertSame(this.builder, this.builder.description("test"));
        assertEquals("test", this.builder.description);
    }

    @Test
    void testDescriptor() {
        DescriptorSupport descriptor = new DescriptorSupport();
        assertSame(this.builder, this.builder.descriptor(descriptor));
        assertEquals(descriptor, this.builder.descriptor);
    }
}