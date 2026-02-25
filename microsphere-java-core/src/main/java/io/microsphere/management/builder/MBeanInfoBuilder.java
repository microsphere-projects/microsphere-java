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
import io.microsphere.annotation.Nullable;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static io.microsphere.reflect.MethodUtils.isIsMethod;
import static io.microsphere.util.ClassUtils.getTypeName;
import static java.beans.Introspector.decapitalize;
import static java.util.Objects.nonNull;

/**
 * The {@link MBeanInfo} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanInfo
 * @since 1.0.0
 */
public class MBeanInfoBuilder extends MBeanDescribableBuilder<MBeanInfoBuilder> {

    /**
     * The MBean qualified name
     */
    @Nonnull
    String className;

    @Nullable
    List<MBeanAttributeInfoBuilder> attributeBuilders = new LinkedList<>();

    @Nullable
    List<MBeanOperationInfoBuilder> operationBuilders = new LinkedList<>();

    @Nullable
    List<MBeanConstructorInfoBuilder> constructorBuilders = new LinkedList<>();

    @Nullable
    List<MBeanNotificationInfoBuilder> notificationBuilders = new LinkedList<>();

    public MBeanInfoBuilder attribute(String attributeName, Class<?> attributeType, Consumer<MBeanAttributeInfoBuilder> builderConsumer) {
        MBeanAttributeInfoBuilder builder = MBeanAttributeInfoBuilder.attribute(attributeType).name(attributeName);
        builderConsumer.accept(builder);
        this.attributeBuilders.add(builder);
        return this;
    }

    public MBeanInfoBuilder attribute(PropertyDescriptor propertyDescriptor) {
        String propertyName = propertyDescriptor.getName();
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        String attributeName = decapitalize(propertyName);
        return attribute(attributeName, propertyType, builder -> {
            Method readMethod = propertyDescriptor.getReadMethod();
            builder.is(isIsMethod(readMethod));
            builder.read(nonNull(readMethod));
            builder.write(nonNull(propertyDescriptor.getWriteMethod()));
            builder.description(propertyDescriptor.toString());
        });
    }

    public MBeanInfoBuilder operation(String methodName, Class<?> returnType, Consumer<MBeanOperationInfoBuilder> builderConsumer) {
        MBeanOperationInfoBuilder builder = MBeanOperationInfoBuilder.operation(returnType).name(methodName);
        builderConsumer.accept(builder);
        this.operationBuilders.add(builder);
        return this;
    }

    public MBeanInfoBuilder operation(MethodDescriptor methodDescriptor) {
        Method method = methodDescriptor.getMethod();
        return operation(method);
    }

    public MBeanInfoBuilder operation(Method method) {
        MBeanOperationInfoBuilder builder = MBeanOperationInfoBuilder.operation(method);
        this.operationBuilders.add(builder);
        return this;
    }

    public MBeanInfoBuilder constructor(Constructor<?> constructor) {
        return constructor(builder -> builder.from(constructor));
    }

    public MBeanInfoBuilder constructor(Consumer<MBeanConstructorInfoBuilder> builderConsumer) {
        MBeanConstructorInfoBuilder builder = MBeanConstructorInfoBuilder.constructor();
        builderConsumer.accept(builder);
        this.constructorBuilders.add(builder);
        return this;
    }

    public MBeanInfoBuilder notification(Class<?>... types) {
        return notification(builder -> builder.types(types));
    }

    public MBeanInfoBuilder notification(Consumer<MBeanNotificationInfoBuilder> builderConsumer) {
        MBeanNotificationInfoBuilder builder = MBeanNotificationInfoBuilder.notification();
        builderConsumer.accept(builder);
        this.notificationBuilders.add(builder);
        return this;
    }

    public MBeanInfo build() {
        return new MBeanInfo(this.className, this.description, buildAttributes(), buildConstructors(),
                buildOperations(), buildNotifications(), this.descriptor);
    }

    private MBeanAttributeInfo[] buildAttributes() {
        return this.attributeBuilders.stream()
                .map(MBeanAttributeInfoBuilder::build)
                .toArray(MBeanAttributeInfo[]::new);
    }

    private MBeanConstructorInfo[] buildConstructors() {
        return this.constructorBuilders.stream()
                .map(MBeanConstructorInfoBuilder::build)
                .toArray(MBeanConstructorInfo[]::new);
    }

    private MBeanOperationInfo[] buildOperations() {
        return this.operationBuilders.stream()
                .map(MBeanOperationInfoBuilder::build)
                .toArray(MBeanOperationInfo[]::new);
    }

    private MBeanNotificationInfo[] buildNotifications() {
        return this.notificationBuilders.stream()
                .map(MBeanNotificationInfoBuilder::build)
                .toArray(MBeanNotificationInfo[]::new);
    }

    public static MBeanInfoBuilder mbeanInfo(String className) {
        MBeanInfoBuilder builder = new MBeanInfoBuilder();
        builder.className = className;
        return builder;
    }

    public static MBeanInfoBuilder mbeanInfo(BeanInfo beanInfo) {
        Class<?> beanClass = beanInfo.getBeanDescriptor().getBeanClass();
        MBeanInfoBuilder builder = new MBeanInfoBuilder();

        builder.className = getTypeName(beanClass);
        builder.description = beanInfo.toString();

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            builder.attribute(propertyDescriptor);
        }

        MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
        for (MethodDescriptor methodDescriptor : methodDescriptors) {
            builder.operation(methodDescriptor);
        }

        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            builder.constructor(constructor);
        }

        return builder;
    }
}