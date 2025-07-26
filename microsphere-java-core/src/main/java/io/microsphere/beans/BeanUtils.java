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

import io.microsphere.util.Utils;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassUtils.isSimpleType;
import static io.microsphere.util.ClassUtils.isWrapperType;
import static io.microsphere.util.StringUtils.uncapitalize;
import static java.beans.Introspector.getBeanInfo;
import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The utilities class for Java Beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Utils
 * @since 1.0.0
 */
public class BeanUtils implements Utils {

    public static Map<String, Object> readPropertiesAsMap(Object bean) {
        if (bean == null) {
            return emptyMap();
        }

        Map<String, Object> propertiesMap = newHashMap();

        Class<?> beanClass = bean.getClass();
        try {
            BeanInfo beanInfo = getBeanInfo(beanClass, Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
                String propertyName = uncapitalize(propertyDescriptor.getName());
                Object propertyValue = readProperty(bean, propertyDescriptor);
                propertiesMap.put(propertyName, propertyValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return unmodifiableMap(propertiesMap);
    }

    protected static Object readProperty(Object instance, PropertyDescriptor propertyDescriptor) throws Throwable {
        Method readMethod = propertyDescriptor.getReadMethod();
        trySetAccessible(readMethod);
        Object propertyValue = invokeMethod(instance, readMethod);

        Class<?> propertyType = propertyDescriptor.getPropertyType();
        if (propertyType.isPrimitive()
                || propertyType.isEnum()
                || isWrapperType(propertyType)
                || isSimpleType(propertyType)
        ) {

        } else if (propertyType.isArray()) {
            int length = propertyValue == null ? 0 : getLength(propertyValue);
            List<Object> values = newArrayList(length);
            for (int i = 0; i < length; i++) {
                Object element = get(propertyValue, i);
                values.add(i, readPropertiesAsMap(element));
            }
        }

        return propertyValue;
    }

    private BeanUtils() {

    }
}
