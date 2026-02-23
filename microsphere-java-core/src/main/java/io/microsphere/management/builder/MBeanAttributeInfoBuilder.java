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

import io.microsphere.annotation.Nonnull;

import javax.management.MBeanAttributeInfo;

import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * {@link MBeanAttributeInfo} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanAttributeInfo
 * @since 1.0.0
 */
public class MBeanAttributeInfoBuilder extends MBeanFeatureInfoBuilder<MBeanAttributeInfoBuilder> {

    /**
     * @serial The actual attribute type.
     */
    @Nonnull
    private String attributeType;

    /**
     * @serial The attribute write right.
     */
    private boolean write;

    /**
     * @serial The attribute read right.
     */
    private boolean read;

    /**
     * @serial Indicates if this method is a "is"
     */
    private boolean is;

    MBeanAttributeInfoBuilder() {
        super();
    }

    public MBeanAttributeInfoBuilder write(boolean isWritable) {
        this.write = isWritable;
        return this;
    }

    public MBeanAttributeInfoBuilder read(boolean isReadable) {
        this.read = isReadable;
        return this;
    }

    public MBeanAttributeInfoBuilder is(boolean isIs) {
        this.is = isIs;
        return this;
    }

    public MBeanAttributeInfo build() {
        return new MBeanAttributeInfo(this.name, this.attributeType, this.description, this.read, this.write,
                this.is, this.descriptor);
    }

    public static MBeanAttributeInfoBuilder attribute(@Nonnull Class<?> attributeType) {
        return attribute(getTypeName(attributeType));
    }

    public static MBeanAttributeInfoBuilder attribute(@Nonnull String attributeType) {
        assertNotNull(attributeType, () -> "The 'attributeType' must not be null");
        MBeanAttributeInfoBuilder builder = new MBeanAttributeInfoBuilder();
        builder.attributeType = attributeType;
        return builder;
    }
}