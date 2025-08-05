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

package io.microsphere.beans;


import io.microsphere.beans.BeanUtilsTest.TestBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;

import static io.microsphere.beans.BeanMetadata.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link BeanMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanMetadata
 * @since 1.0.0
 */
class BeanMetadataTest {

    private BeanMetadata beanMetadata;

    @BeforeEach
    void setUp() {
        beanMetadata = of(TestBean.class);
    }

    @Test
    void testGetBeanInfo() {
        BeanInfo beanInfo = beanMetadata.getBeanInfo();
        assertNotNull(beanInfo);
    }

    @Test
    void testGetPropertyDescriptors() {
        Collection<PropertyDescriptor> propertyDescriptors = this.beanMetadata.getPropertyDescriptors();
        assertEquals(9, propertyDescriptors.size());
    }

    @Test
    void testGetPropertyDescriptor() {
        assertNotNull(this.beanMetadata.getPropertyDescriptor("booleanValue"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("booleanObject"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("string"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("stringBuilder"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("atomicInteger"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("timeUnit"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("clazz"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("dataArray"));
        assertNotNull(this.beanMetadata.getPropertyDescriptor("object"));
    }

    @Test
    void testGetPropertyDescriptorsMap() {
        Map<String, PropertyDescriptor> propertyDescriptorsMap = this.beanMetadata.getPropertyDescriptorsMap();
        assertEquals(9, propertyDescriptorsMap.size());
    }

    @Test
    void testGetBeanClass() {
        assertSame(TestBean.class, this.beanMetadata.getBeanClass());
    }

    @Test
    void testEquals() {
        assertEquals(this.beanMetadata, of(TestBean.class));
        assertNotEquals(this.beanMetadata, TestBean.class);
        assertNotEquals(this.beanMetadata, null);
    }

    @Test
    void testHashCode() {
        assertEquals(this.beanMetadata.hashCode(), of(TestBean.class).hashCode());
        assertEquals(this.beanMetadata.hashCode(), TestBean.class.hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.beanMetadata.toString(), of(TestBean.class).toString());
    }
}