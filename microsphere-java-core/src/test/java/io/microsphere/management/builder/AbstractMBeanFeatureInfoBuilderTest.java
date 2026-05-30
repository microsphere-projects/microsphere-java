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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.MBeanFeatureInfo;

import static javax.management.ImmutableDescriptor.EMPTY_DESCRIPTOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Abstract test class for {@link MBeanFeatureInfoBuilder}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanFeatureInfoBuilderTest
 * @since 1.0.0
 */
@Disabled
abstract class AbstractMBeanFeatureInfoBuilderTest<B extends MBeanFeatureInfoBuilder> extends AbstractMBeanDescribableBuilderTest<B> {

    static final String TEST_NAME = "test-name";

    @Test
    void testName() {
        assertSame(this.builder, this.builder.name(TEST_NAME));
        assertEquals(TEST_NAME, this.builder.name);
    }

    @Test
    void testBuild() {
        MBeanFeatureInfo info = this.builder.build();
        assertNull(info.getName());
        assertNull(info.getDescription());
        assertSame(EMPTY_DESCRIPTOR, info.getDescriptor());

        assertSame(this.builder, this.builder.name(AbstractMBeanFeatureInfoBuilderTest.TEST_NAME));
        info = this.builder.build();
        assertSame(AbstractMBeanFeatureInfoBuilderTest.TEST_NAME, info.getName());

        assertSame(this.builder, this.builder.description(TEST_DESCRIPTION));
        info = this.builder.build();
        assertEquals(TEST_DESCRIPTION, info.getDescription());

        assertSame(this.builder, this.builder.descriptor(TEST_DESCRIPTOR));
        info = this.builder.build();
        assertEquals(TEST_DESCRIPTOR, info.getDescriptor());
    }
}
