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

import javax.management.MBeanAttributeInfo;
import javax.management.modelmbean.DescriptorSupport;

import static io.microsphere.management.builder.MBeanAttributeInfoBuilder.attribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MBeanAttributeInfoBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanAttributeInfoBuilder
 * @since 1.0.0
 */
class MBeanAttributeInfoBuilderTest {

    private MBeanAttributeInfoBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = attribute(Boolean.class);
    }

    @Test
    void testBuild() {
        String name = "test";
        String descrption = "NoDesc";
        DescriptorSupport descriptor = new DescriptorSupport();

        assertSame(this.builder, this.builder.name(name));
        assertSame(this.builder, this.builder.description(descrption));
        assertSame(this.builder, this.builder.descriptor(descriptor));
        assertSame(this.builder, this.builder.read(true));
        assertSame(this.builder, this.builder.write(true));
        assertSame(this.builder, this.builder.is(true));

        MBeanAttributeInfo info = this.builder.build();
        assertEquals(name, info.getName());
        assertEquals(descrption, info.getDescription());
        assertEquals(descriptor, info.getDescriptor());
        assertEquals(Boolean.class.getName(), info.getType());
        assertTrue(info.isReadable());
        assertTrue(info.isWritable());
        assertTrue(info.isIs());
    }
}