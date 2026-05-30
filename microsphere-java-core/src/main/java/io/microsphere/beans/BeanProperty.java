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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Objects;

import static io.microsphere.constants.SymbolConstants.QUOTE_CHAR;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * The class presenting the Property of Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class BeanProperty {

    @Nonnull
    private final String name;

    @Nullable
    private Object value;

    @Nonnull
    private final Class<?> beanClass;

    @Nonnull
    private final PropertyDescriptor descriptor;

    public BeanProperty(@Nonnull String name, @Nonnull Class<?> beanClass, @Nonnull PropertyDescriptor descriptor) {
        this.name = name;
        this.beanClass = beanClass;
        this.descriptor = descriptor;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Nonnull
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Nonnull
    public PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanProperty)) return false;

        BeanProperty that = (BeanProperty) o;

        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(value, that.value)) return false;
        if (!Objects.equals(beanClass, that.beanClass))
            return false;
        return Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + beanClass.hashCode();
        result = 31 * result + descriptor.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String sb = "BeanProperty{" + "name='" + name + QUOTE_CHAR +
                ", value=" + value +
                ", declaringClass=" + beanClass +
                ", descriptor=" + descriptor +
                '}';
        return sb;
    }

    /**
     * Create a {@link BeanProperty} instance
     *
     * @param bean         bean instance
     * @param propertyName the name of bean property
     * @return a {@link BeanProperty} instance
     * @throws IllegalArgumentException if the bean or propertyName is null
     */
    public static BeanProperty of(@Nonnull Object bean, @Nonnull String propertyName) {
        assertNotNull(bean, "The 'bean' argument must not be null");
        assertNotNull(propertyName, "The 'propertyName' argument must not be null");
        Class<?> beanClass = bean.getClass();

        return execute(() -> {
            PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, beanClass);
            BeanProperty beanProperty = new BeanProperty(propertyName, beanClass, descriptor);
            Method getterMethod = descriptor.getReadMethod();
            Object propertyValue = invokeMethod(bean, getterMethod);
            beanProperty.value = (propertyValue);
            return beanProperty;
        });
    }
}
