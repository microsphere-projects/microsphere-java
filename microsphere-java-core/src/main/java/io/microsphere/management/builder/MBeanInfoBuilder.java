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
import java.util.List;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.reflect.MethodUtils.isIsMethod;
import static io.microsphere.util.ClassUtils.getTypeName;
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
    List<MBeanAttributeInfoBuilder> attributeBuilders = newLinkedList();

    @Nullable
    List<MBeanOperationInfoBuilder> operationBuilders = newLinkedList();

    @Nullable
    List<MBeanConstructorInfoBuilder> constructorBuilders = newLinkedList();

    @Nullable
    List<MBeanNotificationInfoBuilder> notificationBuilders = newLinkedList();

    @Nonnull
    public MBeanInfoBuilder attribute(@Nonnull String attributeName, @Nonnull Class<?> attributeType, @Nonnull Consumer<MBeanAttributeInfoBuilder> builderConsumer) {
        MBeanAttributeInfoBuilder builder = MBeanAttributeInfoBuilder.attribute(attributeType).name(attributeName);
        builderConsumer.accept(builder);
        this.attributeBuilders.add(builder);
        return this;
    }

    @Nonnull
    public MBeanInfoBuilder attribute(@Nonnull PropertyDescriptor propertyDescriptor) {
        String propertyName = propertyDescriptor.getName();
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        return attribute(propertyName, propertyType, builder -> {
            Method readMethod = propertyDescriptor.getReadMethod();
            builder.is(isIsMethod(readMethod));
            builder.read(nonNull(readMethod));
            builder.write(nonNull(propertyDescriptor.getWriteMethod()));
            builder.description(propertyDescriptor.toString());
        });
    }

    @Nonnull
    public MBeanInfoBuilder operation(@Nonnull String methodName, @Nonnull Class<?> returnType, @Nonnull Consumer<MBeanOperationInfoBuilder> builderConsumer) {
        MBeanOperationInfoBuilder builder = MBeanOperationInfoBuilder.operation(returnType).name(methodName);
        builderConsumer.accept(builder);
        this.operationBuilders.add(builder);
        return this;
    }

    @Nonnull
    public MBeanInfoBuilder operation(@Nonnull MethodDescriptor methodDescriptor) {
        Method method = methodDescriptor.getMethod();
        return operation(method);
    }

    @Nonnull
    public MBeanInfoBuilder operation(@Nonnull Method method) {
        MBeanOperationInfoBuilder builder = MBeanOperationInfoBuilder.operation(method);
        this.operationBuilders.add(builder);
        return this;
    }

    @Nonnull
    public MBeanInfoBuilder constructor(@Nonnull Constructor<?> constructor) {
        return constructor(builder -> builder.from(constructor));
    }

    @Nonnull
    public MBeanInfoBuilder constructor(@Nonnull Consumer<MBeanConstructorInfoBuilder> builderConsumer) {
        MBeanConstructorInfoBuilder builder = MBeanConstructorInfoBuilder.constructor();
        builderConsumer.accept(builder);
        this.constructorBuilders.add(builder);
        return this;
    }

    @Nonnull
    public MBeanInfoBuilder notification(Class<?>... types) {
        return notification(builder -> builder.types(types));
    }

    @Nonnull
    public MBeanInfoBuilder notification(@Nonnull Consumer<MBeanNotificationInfoBuilder> builderConsumer) {
        MBeanNotificationInfoBuilder builder = MBeanNotificationInfoBuilder.notification();
        builderConsumer.accept(builder);
        this.notificationBuilders.add(builder);
        return this;
    }

    @Nonnull
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

    @Nonnull
    public static MBeanInfoBuilder mbeanInfo(@Nonnull String className) {
        MBeanInfoBuilder builder = new MBeanInfoBuilder();
        builder.className = className;
        return builder;
    }

    @Nonnull
    public static MBeanInfoBuilder mbeanInfo(@Nonnull BeanInfo beanInfo) {
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
