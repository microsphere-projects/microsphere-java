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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.collection.MapUtils;
import io.microsphere.lang.MutableInteger;
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
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.lang.MutableInteger.of;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.util.ClassUtils.isCharSequence;
import static io.microsphere.util.ClassUtils.isClass;
import static io.microsphere.util.ClassUtils.isNumber;
import static io.microsphere.util.ClassUtils.isSimpleType;
import static io.microsphere.util.StringUtils.uncapitalize;
import static java.beans.Introspector.getBeanInfo;
import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;
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

    static final String DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_VALUE = "100";

    /**
     * The property name for the maximum depth of resolving properties of a bean.
     * <p>
     * This property is used to configure the maximum depth to which nested properties
     * of a Java Bean will be resolved. This helps in preventing stack overflow errors
     * when dealing with deeply nested objects. The default value is defined by
     * {@link #DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_VALUE}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Setting the system property to limit the resolution depth
     * System.setProperty(BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME, "50");
     * }</pre>
     */
    public static final String BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "bean.properties.max-resolved-depth";

    /**
     * The default maximum levels of resolving properties of a bean, default is 100
     */
    public static final int DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_LEVELS = parseInt(DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_VALUE);

    /**
     * The maximum levels of resolving properties of a bean, default is 100
     */
    @ConfigurationProperty(
            name = BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME,
            defaultValue = DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_VALUE,
            description = "The maximum depth of resolving properties of a bean in order to avoid stack overflow, " +
                    "default is " + DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_VALUE
    )
    public static final int BEAN_PROPERTIES_MAX_RESOLVED_DEPTH = getInteger(BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME, DEFAULT_BEAN_PROPERTIES_MAX_RESOLVED_LEVELS);

    /**
     * Resolves the properties of a given Java Bean and returns them as a {@link Map}.
     * <p>
     * This method introspects the provided bean, extracts its properties using
     * {@link PropertyDescriptor PropertyDescriptors}, and constructs a map where
     * each key is the uncapitalized property name and each value is the resolved
     * property value. The resolution process handles nested objects, arrays, lists,
     * sets, and maps recursively up to a maximum depth defined by
     * {@link #BEAN_PROPERTIES_MAX_RESOLVED_DEPTH}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Person {
     *     private String name;
     *     private Address address;
     *
     *     // Getters and setters...
     * }
     *
     * public class Address {
     *     private String city;
     *
     *     // Getters and setters...
     * }
     *
     * Person person = new Person();
     * person.setName("John Doe");
     * Address address = new Address();
     * address.setCity("New York");
     * person.setAddress(address);
     *
     * Map<String, Object> properties = BeanUtils.resolvePropertiesAsMap(person);
     * // Resulting map:
     * // {
     * //   "name": "John Doe",
     * //   "address": {
     * //     "city": "New York"
     * //   }
     * // }
     * }</pre>
     *
     * @param bean the Java Bean whose properties are to be resolved; may be {@code null}
     * @return an unmodifiable {@link Map} containing the resolved properties,
     * or an empty map if the bean is {@code null} or maximum resolution
     * depth is reached
     * @see #resolvePropertiesAsMap(Object, int)
     * @see #BEAN_PROPERTIES_MAX_RESOLVED_DEPTH
     */
    public static Map<String, Object> resolvePropertiesAsMap(Object bean) {
        return resolvePropertiesAsMap(bean, BEAN_PROPERTIES_MAX_RESOLVED_DEPTH);
    }

    /**
     * Resolves the properties of a given Java Bean up to a specified maximum depth and returns them as a {@link Map}.
     * <p>
     * This method introspects the provided bean, extracts its properties using
     * {@link PropertyDescriptor PropertyDescriptors}, and constructs a map where
     * each key is the uncapitalized property name and each value is the resolved
     * property value. The resolution process handles nested objects, arrays, lists,
     * sets, and maps recursively, but will stop resolving further once the specified
     * {@code maxResolvedDepth} is reached.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Person {
     *     private String name;
     *     private Address address;
     *
     *     // Getters and setters...
     * }
     *
     * public class Address {
     *     private String city;
     *     private Country country;
     *
     *     // Getters and setters...
     * }
     *
     * public class Country {
     *     private String name;
     *
     *     // Getters and setters...
     * }
     *
     * Person person = new Person();
     * person.setName("John Doe");
     * Address address = new Address();
     * address.setCity("New York");
     * Country country = new Country();
     * country.setName("USA");
     * address.setCountry(country);
     * person.setAddress(address);
     *
     * Map<String, Object> properties = BeanUtils.resolvePropertiesAsMap(person, 2);
     * // Resulting map (depth limited to 2):
     * // {
     * //   "name": "John Doe",
     * //   "address": {
     * //     "city": "New York",
     * //     "country": {}  // country's properties not resolved due to depth limit
     * //   }
     * // }
     * }</pre>
     *
     * @param bean             the Java Bean whose properties are to be resolved; may be {@code null}
     * @param maxResolvedDepth the maximum depth to which nested properties should be resolved;
     *                         must be non-negative
     * @return an unmodifiable {@link Map} containing the resolved properties,
     * or an empty map if the bean is {@code null} or maximum resolution
     * depth is reached
     * @throws IllegalArgumentException if {@code maxResolvedDepth} is negative
     * @see #resolvePropertiesAsMap(Object)
     * @see #resolvePropertiesAsMap(Object, MutableInteger, int)
     */
    public static Map<String, Object> resolvePropertiesAsMap(Object bean, int maxResolvedDepth) {
        return resolvePropertiesAsMap(bean, of(0), maxResolvedDepth);
    }

    /**
     * Resolves the properties of a given Java Bean recursively up to a specified maximum depth,
     * tracking the current depth with a {@link MutableInteger}, and returns them as a {@link Map}.
     * <p>
     * This method introspects the provided bean, extracts its properties using
     * {@link PropertyDescriptor PropertyDescriptors}, and constructs a map where
     * each key is the uncapitalized property name and each value is the resolved
     * property value. The resolution process handles nested objects, arrays, lists,
     * sets, and maps recursively, but will stop resolving further once the specified
     * {@code maxResolvedDepth} is reached. The {@code resolvedDepth} parameter is
     * used to track the current depth of recursion and is incremented upon each call.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Person {
     *     private String name;
     *     private Address address;
     *
     *     // Getters and setters...
     * }
     *
     * public class Address {
     *     private String city;
     *     private Country country;
     *
     *     // Getters and setters...
     * }
     *
     * public class Country {
     *     private String name;
     *
     *     // Getters and setters...
     * }
     *
     * Person person = new Person();
     * person.setName("John Doe");
     * Address address = new Address();
     * address.setCity("New York");
     * Country country = new Country();
     * country.setName("USA");
     * address.setCountry(country);
     * person.setAddress(address);
     *
     * MutableInteger depth = MutableInteger.of(0);
     * Map<String, Object> properties = BeanUtils.resolvePropertiesAsMap(person, depth, 3);
     * // Resulting map (depth limited to 3):
     * // {
     * //   "name": "John Doe",
     * //   "address": {
     * //     "city": "New York",
     * //     "country": {
     * //       "name": "USA"
     * //     }
     * //   }
     * // }
     * }</pre>
     *
     * @param bean             the Java Bean whose properties are to be resolved; may be {@code null}
     * @param resolvedDepth    the current depth of property resolution, incremented on each recursive call;
     *                         must not be {@code null}
     * @param maxResolvedDepth the maximum depth to which nested properties should be resolved;
     *                         must be non-negative
     * @return an unmodifiable {@link Map} containing the resolved properties,
     * or an empty map if the bean is {@code null} or maximum resolution
     * depth is reached
     * @throws IllegalArgumentException if {@code maxResolvedDepth} is negative
     * @see #resolvePropertiesAsMap(Object)
     * @see #resolvePropertiesAsMap(Object, int)
     */
    protected static Map<String, Object> resolvePropertiesAsMap(Object bean, MutableInteger resolvedDepth, int maxResolvedDepth) {
        if (bean == null) {
            return emptyMap();
        }
        // check the maximum depth of resolving properties
        if (resolvedDepth.incrementAndGet() >= maxResolvedDepth) {
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
                Object propertyValue = resolveProperty(bean, propertyDescriptor, resolvedDepth, maxResolvedDepth);
                propertiesMap.put(propertyName, propertyValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return unmodifiableMap(propertiesMap);
    }

    static Object resolveProperty(Object instance, PropertyDescriptor propertyDescriptor, MutableInteger resolvedDepth, int maxResolvedDepth) {
        Method readMethod = propertyDescriptor.getReadMethod();
        trySetAccessible(readMethod);
        Object propertyValue = invokeMethod(instance, readMethod);
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        return resolveProperty(propertyValue, propertyType, resolvedDepth, maxResolvedDepth);
    }

    static Object resolveProperty(Object value, MutableInteger resolvedDepth, int maxResolvedDepth) {
        return resolveProperty(value, value == null ? null : value.getClass(), resolvedDepth, maxResolvedDepth);
    }

    static Object resolveProperty(Object value, Class<?> valueType, MutableInteger resolvedDepth, int maxResolvedDepth) {
        if (value == null) {
            return null;
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
            int length = getLength(resolvedValue);
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
                newList.add(i, resolveProperty(element, resolvedDepth, maxResolvedDepth));
            }
        } else if (isSet(valueType)) {
            Set<?> set = (Set<?>) resolvedValue;
            Set<Object> newSet = newFixedLinkedHashSet(size(set));
            for (Object element : set) {
                newSet.add(resolveProperty(element, resolvedDepth, maxResolvedDepth));
            }
        } else if (isMap(valueType)) {
            Map<?, ?> map = (Map<?, ?>) resolvedValue;
            Map<Object, Object> newMap = newFixedLinkedHashMap(MapUtils.size(map));
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                newMap.put(entry.getKey(), resolveProperty(entry.getValue(), resolvedDepth, maxResolvedDepth));
            }
            resolvedValue = newMap;
        } else { // as the POJO
            resolvedValue = resolvePropertiesAsMap(resolvedValue, resolvedDepth, maxResolvedDepth);
        }

        return resolvedValue;
    }

    private BeanUtils() {
    }
}
