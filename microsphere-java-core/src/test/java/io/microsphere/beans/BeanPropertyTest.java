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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link BeanProperty} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BeanProperty
 * @since 1.0.0
 */
public class BeanPropertyTest {

    private static final String TEST_VALUE = "test-value";

    private String value = TEST_VALUE;

    private BeanProperty beanProperty;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public void setBeanProperty(BeanProperty beanProperty) {
        this.beanProperty = beanProperty;
    }

    @BeforeEach
    public void init() {
        beanProperty = BeanProperty.of(this, "value");
    }

    @Test
    public void testValue() {
        assertEquals(TEST_VALUE, beanProperty.getValue());
        beanProperty.setValue("new-value");
        assertEquals("new-value", beanProperty.getValue());
    }

    @Test
    public void testName() {
        assertEquals("value", beanProperty.getName());
    }

    @Test
    public void testBeanClass() {
        assertEquals(BeanPropertyTest.class, beanProperty.getBeanClass());
    }

    @Test
    public void testPropertyDescriptor() {
        assertNotNull(beanProperty.getDescriptor());
    }

    @Test
    public void testEquals() {
        assertFalse(this.beanProperty.equals(null));
        assertFalse(this.beanProperty.equals("test"));
        assertNotEquals(this.beanProperty, BeanProperty.of(this, "beanProperty"));
        assertEquals(this.beanProperty, BeanProperty.of(this, "value"));
    }

    @Test
    public void testHashCode() {
        assertEquals(this.beanProperty.hashCode(), BeanProperty.of(this, "value").hashCode());
    }

    @Test
    public void testToString() {
        assertNotNull(beanProperty.toString());
    }
}
