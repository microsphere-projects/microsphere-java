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

import io.microsphere.collection.MapUtils;
import io.microsphere.util.Utils;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.ListUtils.isList;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.MapUtils.isMap;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.SetUtils.isSet;
import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassUtils.isCharSequence;
import static io.microsphere.util.ClassUtils.isClass;
import static io.microsphere.util.ClassUtils.isNumber;
import static io.microsphere.util.ClassUtils.isSimpleType;
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
public abstract class BeanUtils implements Utils {

    protected static Map<String, Object> resolvePropertiesAsMap(Object bean) {
        if (bean == null) {
            return emptyMap();
        }

        Class<?> beanClass = bean.getClass();
        Map<String, Object> propertiesMap = newHashMap();
        try {
            BeanInfo beanInfo = getBeanInfo(beanClass, Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
                String propertyName = uncapitalize(propertyDescriptor.getName());
                Object propertyValue = resolveProperty(bean, propertyDescriptor);
                propertiesMap.put(propertyName, propertyValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return unmodifiableMap(propertiesMap);
    }

    static Object resolveProperty(Object instance, PropertyDescriptor propertyDescriptor) {
        Method readMethod = propertyDescriptor.getReadMethod();
        trySetAccessible(readMethod);
        Object propertyValue = invokeMethod(instance, readMethod);
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        return resolveProperty(propertyValue, propertyType);
    }

    static Object resolveProperty(Object value) {
        return resolveProperty(value, value == null ? null : value.getClass());
    }

    static Object resolveProperty(Object value, Class<?> valueType) {
        if (value == null) {
            return value;
        }

        Object resolvedValue = value;

        if (valueType.isPrimitive()
                || isSimpleType(valueType)
                || isCharSequence(valueType)
                || isNumber(valueType)
                || valueType.isEnum()
                || isClass(value)
        ) {
            // do nothing, just return the propertyValue
        } else if (valueType.isArray()) {
            int length = resolvedValue == null ? 0 : getLength(resolvedValue);
            List<Object> values = newArrayList(length);
            for (int i = 0; i < length; i++) {
                Object element = get(resolvedValue, i);
                values.add(i, resolvePropertiesAsMap(element));
            }
        } else if (isList(valueType)) {
            List<?> list = (List<?>) resolvedValue;
            int size = size(list);
            List<Object> newList = newArrayList(size);
            for (int i = 0; i < size; i++) {
                Object element = list.get(i);
                newList.add(i, resolveProperty(element));
            }
        } else if (isSet(valueType)) {
            Set<?> set = (Set<?>) resolvedValue;
            Set<Object> newSet = newFixedLinkedHashSet(size(set));
            for (Object element : set) {
                newSet.add(resolveProperty(element));
            }
        } else if (isMap(valueType)) {
            Map<?, ?> map = (Map<?, ?>) resolvedValue;
            Map<Object, Object> newMap = newFixedLinkedHashMap(MapUtils.size(map));
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                newMap.put(entry.getKey(), resolveProperty(entry.getValue()));
            }
            resolvedValue = newMap;
        } else { // as the POJO
            resolvedValue = resolvePropertiesAsMap(resolvedValue);
        }

        return resolvedValue;
    }

    private BeanUtils() {

    }
}
