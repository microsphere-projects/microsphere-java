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

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.util.StringUtils.uncapitalize;
import static java.util.Collections.unmodifiableMap;

/**
 * The metadata class of Bean, which is used to represent a Java Bean.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanInfo
 * @see PropertyDescriptor
 * @since 1.0.0
 */
public class BeanMetadata {

    private final Class<?> beanClass;

    private final BeanInfo beanInfo;

    private final Map<String, PropertyDescriptor> propertyDescriptorsMap;

    protected BeanMetadata(@Nonnull Class<?> beanClass) {
        this(execute(() -> Introspector.getBeanInfo(beanClass, Object.class)));
    }

    protected BeanMetadata(BeanInfo beanInfo) {
        this.beanInfo = beanInfo;
        this.propertyDescriptorsMap = buildPropertyDescriptorsMap(beanInfo);
        this.beanClass = beanInfo.getBeanDescriptor().getBeanClass();
    }

    @Nonnull
    @Immutable
    static Map<String, PropertyDescriptor> buildPropertyDescriptorsMap(BeanInfo beanInfo) {
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        int length = propertyDescriptors.length;
        Map<String, PropertyDescriptor> propertyDescriptorsMap = newFixedHashMap(length);
        for (int i = 0; i < length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            String propertyName = uncapitalize(propertyDescriptor.getName());
            propertyDescriptorsMap.put(propertyName, propertyDescriptor);
        }
        return unmodifiableMap(propertyDescriptorsMap);
    }

    public BeanInfo getBeanInfo() {
        return this.beanInfo;
    }

    @Nonnull
    @Immutable
    public Collection<PropertyDescriptor> getPropertyDescriptors() {
        return this.propertyDescriptorsMap.values();
    }

    /**
     * Get the {@link PropertyDescriptor} by property name
     *
     * @param propertyName the property name, which is usually the uncapitalized name of the property
     *                     (e.g., "propertyName" for a property named "PropertyName")
     * @return the {@link PropertyDescriptor} if found, otherwise {@code null}
     */
    public PropertyDescriptor getPropertyDescriptor(String propertyName) {
        return this.propertyDescriptorsMap.get(propertyName);
    }

    @Nonnull
    @Immutable
    public Map<String, PropertyDescriptor> getPropertyDescriptorsMap() {
        return this.propertyDescriptorsMap;
    }

    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BeanMetadata)) return false;

        BeanMetadata that = (BeanMetadata) o;
        return Objects.equals(getBeanClass(), that.getBeanClass());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBeanClass());
    }

    @Override
    public String toString() {
        return "BeanMetadata{" + "beanClass='" + getBeanClass().getName() + "'}";
    }

    /**
     * Create a {@link BeanMetadata} instance from the specified bean class.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Create BeanMetadata for a simple Java Bean
     * BeanMetadata metadata = BeanMetadata.of(Person.class);
     *
     * // Example 2: Create BeanMetadata for a complex Bean
     * BeanMetadata metadata = BeanMetadata.of(Company.class);
     * }</pre>
     *
     * @param beanClass the bean class must not be {@code null}
     * @return a {@link BeanMetadata} instance
     * @throws RuntimeException if the {@link BeanInfo} cannot be obtained from the specified bean class
     * @see Introspector#getBeanInfo(Class, Class)
     */
    public static BeanMetadata of(@Nonnull Class<?> beanClass) throws RuntimeException {
        return new BeanMetadata(beanClass);
    }

}