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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Test
    public void test() {
        BeanPropertyTest bean = new BeanPropertyTest();
        BeanProperty beanProperty = BeanProperty.of(bean, "value");
        assertEquals("value", beanProperty.getName());
        assertEquals(TEST_VALUE, beanProperty.getValue());
        assertEquals(BeanPropertyTest.class, beanProperty.getBeanClass());
        assertEquals(String.class, beanProperty.getDescriptor().getPropertyType());
    }
}
