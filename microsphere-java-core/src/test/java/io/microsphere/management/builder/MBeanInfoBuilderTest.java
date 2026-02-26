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


import io.microsphere.beans.BeanMetadata;
import io.microsphere.test.Data;
import org.junit.jupiter.api.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import static io.microsphere.beans.BeanUtils.getBeanMetadata;
import static io.microsphere.management.builder.AbstractMBeanFeatureInfoBuilderTest.TEST_NAME;
import static io.microsphere.management.builder.MBeanInfoBuilder.mbeanInfo;
import static io.microsphere.management.builder.MBeanOperationInfoBuilder.Impact.ACTION;
import static io.microsphere.reflect.MethodUtils.isIsMethod;
import static io.microsphere.reflect.MethodUtils.isSetterMethod;
import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.StringUtils.uncapitalize;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MBeanInfoBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanInfoBuilder
 * @since 1.0.0
 */
class MBeanInfoBuilderTest extends AbstractMBeanDescribableBuilderTest<MBeanInfoBuilder> {

    @Test
    void testAttribute() {
        assertTrue(this.builder.attributeBuilders.isEmpty());

        this.builder.attribute(TEST_NAME, String.class, b -> {
            b.is(true);
            b.read(true);
            b.write(true);
        });

        assertEquals(1, this.builder.attributeBuilders.size());

        MBeanAttributeInfoBuilder attributeInfoBuilder = this.builder.attributeBuilders.get(0);
        assertEquals(TEST_NAME, attributeInfoBuilder.name);
        assertEquals(String.class.getName(), attributeInfoBuilder.type);
        assertTrue(attributeInfoBuilder.is);
        assertTrue(attributeInfoBuilder.read);
        assertTrue(attributeInfoBuilder.write);
        assertNull(attributeInfoBuilder.description);
        assertNull(attributeInfoBuilder.descriptor);
    }

    @Test
    void testOperation() {
        assertTrue(this.builder.operationBuilders.isEmpty());

        this.builder.operation(TEST_NAME, String.class, b -> {
            b.impact(ACTION);
        });

        assertEquals(1, this.builder.operationBuilders.size());

        MBeanOperationInfoBuilder operationInfoBuilder = this.builder.operationBuilders.get(0);
        assertEquals(TEST_NAME, operationInfoBuilder.name);
        assertEquals(String.class.getName(), operationInfoBuilder.returnType);
        assertEquals(ACTION.getValue(), operationInfoBuilder.impact);
        assertNull(operationInfoBuilder.description);
        assertNull(operationInfoBuilder.descriptor);
    }

    @Test
    void testConstructor() {
        assertTrue(this.builder.constructorBuilders.isEmpty());

        this.builder.constructor(b -> {
            b.name(TEST_NAME);
            b.description(TEST_DESCRIPTION);
            b.descriptor(TEST_DESCRIPTOR);
        });

        assertEquals(1, this.builder.constructorBuilders.size());

        MBeanConstructorInfoBuilder constructorInfoBuilder = this.builder.constructorBuilders.get(0);
        assertEquals(TEST_NAME, constructorInfoBuilder.name);
        assertEquals(TEST_DESCRIPTION, constructorInfoBuilder.description);
        assertEquals(TEST_DESCRIPTOR, constructorInfoBuilder.descriptor);
        assertTrue(constructorInfoBuilder.parameters.isEmpty());
    }

    @Test
    void testNotification() {
        assertTrue(this.builder.notificationBuilders.isEmpty());

        this.builder.notification(b -> {
            b.name(TEST_NAME);
            b.description(TEST_DESCRIPTION);
            b.descriptor(TEST_DESCRIPTOR);
            b.types(String.class);
        });

        assertEquals(1, this.builder.notificationBuilders.size());

        MBeanNotificationInfoBuilder notificationInfoBuilder = this.builder.notificationBuilders.get(0);
        assertEquals(TEST_NAME, notificationInfoBuilder.name);
        assertEquals(TEST_DESCRIPTION, notificationInfoBuilder.description);
        assertEquals(TEST_DESCRIPTOR, notificationInfoBuilder.descriptor);
        assertArrayEquals(ofArray(String.class.getName()), notificationInfoBuilder.types);
    }

    @Test
    void testNotificationWithTypes() {
        assertTrue(this.builder.notificationBuilders.isEmpty());

        this.builder.notification(String.class);

        assertEquals(1, this.builder.notificationBuilders.size());

        MBeanNotificationInfoBuilder notificationInfoBuilder = this.builder.notificationBuilders.get(0);
        assertNull(notificationInfoBuilder.name);
        assertNull(notificationInfoBuilder.description);
        assertNull(notificationInfoBuilder.descriptor);
        assertArrayEquals(ofArray(String.class.getName()), notificationInfoBuilder.types);
    }

    @Test
    void testMbeanInfo() {
        MBeanInfoBuilder builder = mbeanInfo(String.class.getName());
        assertEquals(String.class.getName(), builder.className);
        assertNull(builder.description);
        assertNull(builder.descriptor);
    }

    @Test
    void testMbeanInfoWithBeanInfo() {
        BeanMetadata beanMetadata = getBeanMetadata(Data.class);
        BeanInfo beanInfo = beanMetadata.getBeanInfo();
        MBeanInfoBuilder builder = mbeanInfo(beanInfo);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();

        assertEquals(Data.class.getName(), builder.className);
        assertEquals(beanInfo.toString(), builder.description);

        forEach(propertyDescriptors, (index, propertyDescriptor) -> {
            assertAttribute(builder, index, propertyDescriptor);
        });

        forEach(methodDescriptors, (index, methodDescriptor) -> {
            assertOperation(builder, index, methodDescriptor);
        });

    }

    @Test
    void testBuild() {
        BeanMetadata beanMetadata = getBeanMetadata(Data.class);
        BeanInfo beanInfo = beanMetadata.getBeanInfo();
        MBeanInfoBuilder builder = mbeanInfo(beanInfo);
        MBeanInfo info = builder.build();

        assertEquals(getTypeName(Data.class), info.getClassName());
        assertEquals(beanInfo.toString(), info.getDescription());

        forEach(info.getAttributes(), (index, attributeInfo) -> {
            assertAttributeInfo(builder, index, attributeInfo);
        });

        forEach(info.getOperations(), (index, operationInfo) -> {
            assertOperationInfo(builder, index, operationInfo);
        });

        forEach(info.getConstructors(), (index, constructorInfo) -> {
            assertConstructorInfo(builder, index, constructorInfo);
        });
    }

    private void assertAttributeInfo(MBeanInfoBuilder builder, Integer index, MBeanAttributeInfo attributeInfo) {
        MBeanAttributeInfoBuilder attributeInfoBuilder = builder.attributeBuilders.get(index);
        assertEquals(attributeInfo.getName(), attributeInfoBuilder.name);
        assertEquals(attributeInfo.getType(), attributeInfoBuilder.type);
        assertEquals(attributeInfo.isIs(), attributeInfoBuilder.is);
        assertEquals(attributeInfo.isReadable(), attributeInfoBuilder.read);
        assertEquals(attributeInfo.isWritable(), attributeInfoBuilder.write);
        assertNull(attributeInfoBuilder.descriptor);
        assertNotNull(attributeInfo.getDescriptor());
        assertEquals(attributeInfo.getDescription(), attributeInfoBuilder.description);
    }

    private void assertOperationInfo(MBeanInfoBuilder builder, Integer index, MBeanOperationInfo operationInfo) {
        MBeanOperationInfoBuilder operationInfoBuilder = builder.operationBuilders.get(index);

        assertEquals(operationInfo.getName(), operationInfoBuilder.name);
        assertEquals(operationInfo.getImpact(), operationInfoBuilder.impact);
        assertEquals(operationInfo.getReturnType(), operationInfoBuilder.returnType);
        assertEquals(operationInfo.getDescription(), operationInfoBuilder.description);
        assertEquals(operationInfo.getDescriptor(), operationInfoBuilder.descriptor);
    }

    private void assertConstructorInfo(MBeanInfoBuilder builder, Integer index, MBeanConstructorInfo constructorInfo) {
        MBeanConstructorInfoBuilder constructorInfoBuilder = builder.constructorBuilders.get(index);

        assertEquals(constructorInfo.getName(), constructorInfoBuilder.name);
        assertEquals(constructorInfo.getDescription(), constructorInfoBuilder.description);
        assertEquals(constructorInfo.getDescriptor(), constructorInfoBuilder.descriptor);
    }

    private void assertAttribute(MBeanInfoBuilder builder, int index, PropertyDescriptor propertyDescriptor) {
        MBeanAttributeInfoBuilder attributeInfoBuilder = builder.attributeBuilders.get(index);
        String attributeName = uncapitalize(propertyDescriptor.getName());
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        Method readMethod = propertyDescriptor.getReadMethod();
        Method writeMethod = propertyDescriptor.getWriteMethod();

        assertEquals(attributeName, attributeInfoBuilder.name);
        assertEquals(getTypeName(propertyType), attributeInfoBuilder.type);
        assertEquals(isIsMethod(readMethod), attributeInfoBuilder.is);
        assertEquals(nonNull(readMethod), attributeInfoBuilder.read);
        assertEquals(isSetterMethod(writeMethod), attributeInfoBuilder.write);
        assertEquals(propertyDescriptor.toString(), attributeInfoBuilder.description);
    }

    private void assertOperation(MBeanInfoBuilder builder, Integer index, MethodDescriptor methodDescriptor) {
        MBeanOperationInfoBuilder operationInfoBuilder = builder.operationBuilders.get(index);
        Method method = methodDescriptor.getMethod();
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();

        assertEquals(methodName, operationInfoBuilder.name);
        assertEquals(getTypeName(returnType), operationInfoBuilder.returnType);
        assertEquals(method.toString(), operationInfoBuilder.description);
    }
}