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


import io.microsphere.AbstractTestCase;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.test.MultipleValueData;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.microsphere.beans.BeanUtils.findPropertyDescriptor;
import static io.microsphere.beans.BeanUtils.findWriteMethod;
import static io.microsphere.beans.BeanUtils.getBeanMetadata;
import static io.microsphere.beans.BeanUtils.resolvePropertiesAsMap;
import static io.microsphere.beans.BeanUtils.resolveProperty;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.io.event.FileChangedEvent.Kind.MODIFIED;
import static io.microsphere.lang.MutableInteger.of;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BeanUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanUtils
 * @since 1.0.0
 */
class BeanUtilsTest extends AbstractTestCase {

    @Test
    void testGetBeanMetadata() {
        BeanMetadata beanMetadata = getBeanMetadata(TestBean.class);
        assertNotNull(beanMetadata);
        assertSame(beanMetadata, getBeanMetadata(TestBean.class));
    }

    @Test
    void testFindPropertyDescriptor() {
        BeanMetadata beanMetadata = getBeanMetadata(TestBean.class);
        PropertyDescriptor propertyDescriptor = findPropertyDescriptor(beanMetadata, "booleanValue");
        assertNotNull(propertyDescriptor);
        assertSame(propertyDescriptor, beanMetadata.getPropertyDescriptor("booleanValue"));
    }

    @Test
    void testFindPropertyDescriptorOnNotFound() {
        BeanMetadata beanMetadata = getBeanMetadata(TestBean.class);
        PropertyDescriptor propertyDescriptor = findPropertyDescriptor(beanMetadata, "notFound");
        assertNull(propertyDescriptor);
    }

    @Test
    void testFindWriteMethod() {
        BeanMetadata beanMetadata = getBeanMetadata(MultipleValueData.class);
        Method writeMethod = findWriteMethod(beanMetadata, "stringList");
        assertNotNull(writeMethod);
    }

    @Test
    void testFindWriteMethodOnNotFound() {
        BeanMetadata beanMetadata = getBeanMetadata(TestBean.class);
        Method writeMethod = findWriteMethod(beanMetadata, "booleanValue");
        assertNull(writeMethod);
    }


    @Test
    void testResolvePropertiesAsMap() {
        ConfigurationProperty configurationProperty = new ConfigurationProperty("test");
        configurationProperty.setValue("value");
        configurationProperty.setDefaultValue("defaultValue");
        configurationProperty.setRequired(true);
        configurationProperty.setDescription("description");
        ConfigurationProperty.Metadata metadata = configurationProperty.getMetadata();
        metadata.getSources().add("source-1");
        metadata.getTargets().add("target-1");
        metadata.setDeclaredClass("declaredClass");
        metadata.setDeclaredField("declaredField");

        Map<String, Object> propertiesMap = resolvePropertiesAsMap(configurationProperty);

        assertEquals("test", propertiesMap.get("name"));
        assertEquals("value", propertiesMap.get("value"));
        assertEquals("defaultValue", propertiesMap.get("defaultValue"));
        assertEquals(Boolean.TRUE, propertiesMap.get("required"));
        assertEquals("description", propertiesMap.get("description"));

        Map<String, Object> metadataMap = (Map<String, Object>) propertiesMap.get("metadata");
        assertEquals(newLinkedHashSet("source-1"), metadataMap.get("sources"));
        assertEquals(newLinkedHashSet("target-1"), metadataMap.get("targets"));
        assertEquals("declaredClass", metadataMap.get("declaredClass"));
        assertEquals("declaredField", metadataMap.get("declaredField"));
    }

    @Test
    void testResolvePropertiesAsMapWithTestBean() {
        TestBean testBean = new TestBean();
        testBean.booleanValue = true;
        testBean.booleanObject = Boolean.FALSE;
        testBean.string = "string";
        testBean.stringBuilder = new StringBuilder("stringBuilder");
        testBean.atomicInteger = new AtomicInteger(1);
        testBean.timeUnit = TimeUnit.DAYS;
        testBean.clazz = String.class;

        Data data = new Data();
        data.value = 1;
        data.list = ofList(1, 2, 3);
        data.set = ofSet(1, 2, 3);
        long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        data.map = ofMap("1", date, "2", date, "3", null);

        testBean.dataArray = new Data[]{data, data, data};

        Map<String, Object> propertiesMap = resolvePropertiesAsMap(testBean);
        assertEquals(true, propertiesMap.get("booleanValue"));
        assertEquals(Boolean.FALSE, propertiesMap.get("booleanObject"));
        assertEquals("string", propertiesMap.get("string"));
        assertEquals("stringBuilder", propertiesMap.get("stringBuilder").toString());
        assertEquals(1, ((AtomicInteger) propertiesMap.get("atomicInteger")).get());
        assertEquals(TimeUnit.DAYS, propertiesMap.get("timeUnit"));
        assertEquals(String.class, propertiesMap.get("clazz"));
        Object[] dataArray = (Object[]) propertiesMap.get("dataArray");
        for (int i = 0; i < 3; i++) {
            Map<String, Object> d = (Map) dataArray[i];
            assertEquals(1, d.get("value"));
            assertEquals(ofList(1, 2, 3), d.get("list"));
            assertEquals(ofSet(1, 2, 3), d.get("set"));
            assertEquals(ofMap("1", date, "2", date, "3", null), d.get("map"));
        }
    }

    @Test
    void testResolvePropertiesAsMapWithNull() {
        Map<String, Object> propertiesMap = resolvePropertiesAsMap(null);
        assertSame(emptyMap(), propertiesMap);
    }

    @Test
    void testResolvePropertiesAsMapOnRuntimeException() {
        assertThrows(RuntimeException.class, () -> resolvePropertiesAsMap(new Object()));
    }

    @Test
    void testResolvePropertiesAsMapOnMaxResolvedLevelsExceeded() {
        Map<String, Object> propertiesMap = resolvePropertiesAsMap(new FileChangedEvent(newRandomTempFile(), MODIFIED));
        assertNotNull(propertiesMap);
        assertTrue(propertiesMap.containsKey("file"));
        assertTrue(propertiesMap.containsKey("kind"));
        assertTrue(propertiesMap.containsKey("timestamp"));
        assertTrue(propertiesMap.containsKey("source"));
    }

    @Test
    void testResolveProperty() {
        File file = newRandomTempFile();
        Object property = resolveProperty(file, of(0), 10);
        Map<String, Object> properties = (Map<String, Object>) property;
        assertFalse(properties.isEmpty());
    }

    static class TestBean {

        boolean booleanValue;

        Boolean booleanObject;

        String string;

        StringBuilder stringBuilder;

        AtomicInteger atomicInteger;

        TimeUnit timeUnit;

        Class<?> clazz;

        Data[] dataArray;

        Object object;

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public Boolean getBooleanObject() {
            return booleanObject;
        }

        public String getString() {
            return string;
        }

        public StringBuilder getStringBuilder() {
            return stringBuilder;
        }

        public AtomicInteger getAtomicInteger() {
            return atomicInteger;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public Data[] getDataArray() {
            return dataArray;
        }

        public Object getObject() {
            return object;
        }
    }

    static class Data {
        int value;

        List<Integer> list;

        Set<Object> set;

        Map<String, Date> map;

        Object object;

        public int getValue() {
            return value;
        }

        public List<Integer> getList() {
            return list;
        }

        public Set<Object> getSet() {
            return set;
        }

        public Map<String, Date> getMap() {
            return map;
        }

        public Object getObject() {
            return object;
        }
    }
}