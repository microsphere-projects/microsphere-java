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
package io.microsphere.management;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

import static java.util.Objects.requireNonNull;

/**
 * MBean {@link Attribute} with {@link MBeanAttributeInfo} and its' optional value
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Attribute
 * @since 1.0.0
 */
public class MBeanAttribute {

    private final MBeanInfo declaringMBeanInfo;

    private final MBeanAttributeInfo attributeInfo;

    private final Object value;

    public MBeanAttribute(@Nonnull MBeanInfo declaringMBeanInfo,
                          @Nonnull MBeanAttributeInfo attributeInfo,
                          @Nullable Object value) {
        requireNonNull(declaringMBeanInfo, "The declaring MBeanInfo argument must not be null!");
        requireNonNull(attributeInfo, "The MBeanAttributeInfo argument must not be null!");
        this.declaringMBeanInfo = declaringMBeanInfo;
        this.attributeInfo = attributeInfo;
        this.value = value;
    }

    @Nonnull
    public MBeanInfo getDeclaringMBeanInfo() {
        return declaringMBeanInfo;
    }

    /**
     * @return Get the attribute name of MBean
     */
    @Nonnull
    public String getName() {
        return attributeInfo.getName();
    }

    /**
     * @return Get the {@link String string} presenting attribute type of MBeans
     */
    @Nonnull
    public String getType() {
        return attributeInfo.getType();
    }

    /**
     * Whether the value of the attribute can be read.
     *
     * @return True if the attribute can be read, false otherwise.
     */
    public boolean isReadable() {
        return attributeInfo.isReadable();
    }

    /**
     * Whether new values can be written to the attribute.
     *
     * @return True if the attribute can be written to, false otherwise.
     */
    public boolean isWritable() {
        return attributeInfo.isWritable();
    }

    /**
     * Indicates if this attribute has an "is" getter.
     *
     * @return true if this attribute has an "is" getter.
     */
    public boolean isIs() {
        return attributeInfo.isIs();
    }

    /**
     * @return Get the {@link MBeanAttributeInfo}
     */
    @Nonnull
    public MBeanAttributeInfo getAttributeInfo() {
        return attributeInfo;
    }

    /**
     * @return Get the attribute value of MBean, may be <code>null</code>
     */
    public Object getValue() {
        return value;
    }
}
