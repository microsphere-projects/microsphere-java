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

import static io.microsphere.beans.BeanProperty.of;
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
class BeanPropertyTest {

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
    public void setUp() {
        beanProperty = of(this, "value");
    }

    @Test
    void testValue() {
        assertEquals(TEST_VALUE, beanProperty.getValue());
        beanProperty.setValue("new-value");
        assertEquals("new-value", beanProperty.getValue());
    }

    @Test
    void testName() {
        assertEquals("value", beanProperty.getName());
    }

    @Test
    void testBeanClass() {
        assertEquals(BeanPropertyTest.class, beanProperty.getBeanClass());
    }

    @Test
    void testPropertyDescriptor() {
        assertNotNull(beanProperty.getDescriptor());
    }

    @Test
    void testEquals() {
        assertEquals(this.beanProperty, this.beanProperty);
        assertEquals(this.beanProperty, of(this, "value"));

        // test "null"
        assertFalse(this.beanProperty.equals(null));

        // test different type
        assertFalse(this.beanProperty.equals("test"));

        // test different property name
        assertNotEquals(this.beanProperty, of(this, "beanProperty"));

        // test different property value
        BeanPropertyTest bean = new BeanPropertyTest();
        bean.value = TEST_VALUE + TEST_VALUE;
        assertNotEquals(this.beanProperty, of(bean, "value"));

        bean = new BeanPropertyTest() {
        };
        assertNotEquals(this.beanProperty, of(bean, "value"));

    }

    @Test
    void testHashCode() {
        assertEquals(this.beanProperty.hashCode(), of(this, "value").hashCode());

        BeanPropertyTest bean = new BeanPropertyTest();
        bean.value = null;
        assertNotEquals(beanProperty.hashCode(), of(bean, "value").hashCode());
    }

    @Test
    void testToString() {
        assertEquals(beanProperty.toString(), of(this, "value").toString());
    }
}
