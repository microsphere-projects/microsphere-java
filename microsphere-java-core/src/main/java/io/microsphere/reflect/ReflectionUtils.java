package io.microsphere.reflect;


import io.microsphere.logging.Logger;
import io.microsphere.util.ArrayUtils;
import io.microsphere.util.BaseUtils;

import javax.annotation.Nonnull;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.FieldUtils.getDeclaredField;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static io.microsphere.util.ClassUtils.isSimpleType;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * Reflection Utility class , generic methods are defined from {@link FieldUtils} , {@link MethodUtils} , {@link
 * ConstructorUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @version 1.0.0
 * @see Method
 * @see Field
 * @see Constructor
 * @see Array
 * @see MethodUtils
 * @see FieldUtils
 * @see ConstructorUtils
 * @since 1.0.0
 */
public abstract class ReflectionUtils extends BaseUtils {

    /**
     * Current Type
     */
    private static final Class<?> TYPE = ReflectionUtils.class;

    private static final Logger logger = getLogger(TYPE);

    /**
     * Sun JDK implementation class: full name of sun.reflect.Reflection
     */
    public static final String SUN_REFLECT_REFLECTION_CLASS_NAME = "sun.reflect.Reflection";

    /**
     * sun.reflect.Reflection method name
     */
    private static final String getCallerClassMethodName = "getCallerClass";

    /**
     * sun.reflect.Reflection invocation frame
     */
    private static final int sunReflectReflectionInvocationFrame;

    /**
     * {@link StackTraceElement} invocation frame
     */
    private static final int stackTraceElementInvocationFrame;

    /**
     * Is Supported sun.reflect.Reflection ?
     */
    private static final boolean supportedSunReflectReflection;

    /**
     * sun.reflect.Reflection#getCallerClass(int) method
     */
    private static final Method getCallerClassMethod;

    /**
     * The class name of {@linkplain java.lang.reflect.InaccessibleObjectException} since JDK 9
     */
    public static final String INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME = "java.lang.reflect.InaccessibleObjectException";

    // Initialize sun.reflect.Reflection
    static {
        Method method = null;
        boolean supported = false;
        int invocationFrame = 0;
        try {
            // Use sun.reflect.Reflection to calculate frame
            Class<?> type = Class.forName(SUN_REFLECT_REFLECTION_CLASS_NAME);
            method = type.getMethod(getCallerClassMethodName, int.class);
            method.setAccessible(true);
            // Adapt SUN JDK ,The value of invocation frame in JDK 6/7/8 may be different
            for (int i = 0; i < 9; i++) {
                Class<?> callerClass = (Class<?>) method.invoke(null, i);
                if (TYPE.equals(callerClass)) {
                    invocationFrame = i;
                    break;
                }
            }
            supported = true;
        } catch (Throwable e) {
            if (logger.isTraceEnabled()) {
                logger.trace("The class '{}' or its' method '{}({})' can't be initialized.", SUN_REFLECT_REFLECTION_CLASS_NAME, getCallerClassMethodName, int.class, e);
            }
        }
        // set method info
        getCallerClassMethod = method;
        supportedSunReflectReflection = supported;
        // getCallerClass() -> getCallerClass(int)
        // Plugs 1 , because Invocation getCallerClass() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClass(int) method was considered as increment invocation frame
        sunReflectReflectionInvocationFrame = invocationFrame + 2;
    }

    // Initialize StackTraceElement
    static {
        int invocationFrame = 0;
        // Use java.lang.StackTraceElement to calculate frame
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            String className = stackTraceElement.getClassName();
            if (TYPE.getName().equals(className)) {
                break;
            }
            invocationFrame++;
        }
        // getCallerClass() -> getCallerClass(int)
        // Plugs 1 , because Invocation getCallerClass() method was considered as increment invocation frame
        // Plugs 1 , because Invocation getCallerClass(int) method was considered as increment invocation frame
        stackTraceElementInvocationFrame = invocationFrame + 2;
    }

    /**
     * Is supported sun.reflect.Reflection or not
     *
     * @return <code>true</code> if supported
     */
    public static boolean isSupportedSunReflectReflection() {
        return supportedSunReflectReflection;
    }

    /**
     * Get Caller class
     *
     * @return Get the Class name that called the method
     */
    @Nonnull
    public static String getCallerClassName() {
        if (supportedSunReflectReflection) {
            Class<?> callerClass = getCallerClassInSunJVM(sunReflectReflectionInvocationFrame);
            if (callerClass != null) return callerClass.getName();
        }
        return getCallerClassNameInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * General implementation, get the calling class name
     *
     * @return call class name
     * @see #getCallerClassNameInGeneralJVM(int)
     */
    static String getCallerClassNameInGeneralJVM() {
        return getCallerClassNameInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * General implementation, get the calling class name by specifying the calling level value
     *
     * @param invocationFrame invocation frame
     * @return specified invocation frame class
     */
    static String getCallerClassNameInGeneralJVM(int invocationFrame) throws IndexOutOfBoundsException {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (invocationFrame < elements.length) {
            StackTraceElement targetStackTraceElement = elements[invocationFrame];
            return targetStackTraceElement.getClassName();
        }
        return null;
    }

    static Class<?> getCallerClassInSunJVM(int realFramesToSkip) throws UnsupportedOperationException {
        if (!supportedSunReflectReflection) {
            throw new UnsupportedOperationException("Requires SUN's JVM!");
        }
        Class<?> callerClass = null;
        if (getCallerClassMethod != null) {
            try {
                callerClass = (Class<?>) getCallerClassMethod.invoke(null, realFramesToSkip);
            } catch (Exception ignored) {
            }
        }
        return callerClass;
    }

    /**
     * Get caller class in General JVM
     *
     * @param invocationFrame invocation frame
     * @return caller class
     * @see #getCallerClassNameInGeneralJVM(int)
     */
    static Class<?> getCallerClassInGeneralJVM(int invocationFrame) {
        String className = getCallerClassNameInGeneralJVM(invocationFrame + 1);
        Class<?> targetClass = null;
        try {
            targetClass = className == null ? null : Class.forName(className);
        } catch (Throwable ignored) {
        }
        return targetClass;
    }

    /**
     * Get caller class
     * <p/>
     * For instance,
     * <pre>
     *     package com.acme;
     *     import ...;
     *     class Foo {
     *         public void bar(){
     *
     *         }
     *     }
     * </pre>
     *
     * @return Get caller class
     * @throws IllegalStateException If the caller class cannot be found
     */
    @Nonnull
    public static Class<?> getCallerClass() throws IllegalStateException {
        if (supportedSunReflectReflection) {
            Class<?> callerClass = getCallerClassInSunJVM(sunReflectReflectionInvocationFrame);
            if (callerClass != null) {
                return callerClass;
            }
        }
        return getCallerClassInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * Get caller class In SUN HotSpot JVM
     *
     * @return Caller Class
     * @throws UnsupportedOperationException If JRE is not a SUN HotSpot JVM
     * @see #getCallerClassInSunJVM(int)
     */
    static Class<?> getCallerClassInSunJVM() throws UnsupportedOperationException {
        return getCallerClassInSunJVM(sunReflectReflectionInvocationFrame);
    }

    /**
     * Get caller class name In SUN HotSpot JVM
     *
     * @return Caller Class
     * @throws UnsupportedOperationException If JRE is not a SUN HotSpot JVM
     * @see #getCallerClassInSunJVM(int)
     */
    static String getCallerClassNameInSunJVM() throws UnsupportedOperationException {
        Class<?> callerClass = getCallerClassInSunJVM(sunReflectReflectionInvocationFrame);
        return callerClass.getName();
    }

    /**
     * Get the caller class
     *
     * @param invocationFrame The frame of method invocation
     * @return <code>null</code> if not found
     */
    public static Class<?> getCallerClass(int invocationFrame) {
        if (supportedSunReflectReflection) {
            Class<?> callerClass = getCallerClassInSunJVM(invocationFrame + 1);
            if (callerClass != null) {
                return callerClass;
            }
        }
        return getCallerClassInGeneralJVM(invocationFrame + 1);
    }

    /**
     * Get caller class in General JVM
     *
     * @return Caller Class
     * @see #getCallerClassInGeneralJVM(int)
     */
    static Class<?> getCallerClassInGeneralJVM() {
        return getCallerClassInGeneralJVM(stackTraceElementInvocationFrame);
    }

    /**
     * Assert array index
     *
     * @param array Array object
     * @param index index
     * @throws IllegalArgumentException       see {@link ReflectionUtils#assertArrayType(Object)}
     * @throws ArrayIndexOutOfBoundsException If <code>index</code> is less than 0 or equals or greater than length of array
     */
    public static void assertArrayIndex(Object array, int index) throws IllegalArgumentException {
        if (index < 0) {
            String message = format("The index argument must be positive , actual is {}", index);
            throw new ArrayIndexOutOfBoundsException(message);
        }
        assertArrayType(array);
        int length = Array.getLength(array);
        if (index > length - 1) {
            String message = format("The index must be less than {} , actual is {}", length, index);
            throw new ArrayIndexOutOfBoundsException(message);
        }
    }

    /**
     * Assert the object is array or not
     *
     * @param array asserted object
     * @throws IllegalArgumentException if the object is not a array
     */
    public static void assertArrayType(Object array) throws IllegalArgumentException {
        Class<?> type = array.getClass();
        if (!type.isArray()) {
            String message = format("The argument is not an array object, its type is {}", type.getName());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert Field type match
     *
     * @param object       Object
     * @param fieldName    field name
     * @param expectedType expected type
     * @throws IllegalArgumentException if type is not matched
     */
    public static void assertFieldMatchType(Object object, String fieldName, Class<?> expectedType) throws IllegalArgumentException {
        Class<?> type = object.getClass();
        Field field = getDeclaredField(type, fieldName);
        Class<?> fieldType = field.getType();
        if (!expectedType.isAssignableFrom(fieldType)) {
            String message = format("The type['{}'] of field['{}'] in Class['{}'] can't match expected type['{}']", fieldType.getName(), fieldName, type.getName(), expectedType.getName());
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Convert {@link Array} object to {@link List}
     *
     * @param array array object
     * @return {@link List}
     * @throws IllegalArgumentException if the object argument is not an array
     */
    @Nonnull
    public static <T> List<T> toList(Object array) throws IllegalArgumentException {
        int length = Array.getLength(array);
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            list.add((T) toObject(element));
        }
        return list;
    }

    private static Object toObject(Object object) {
        if (object == null) {
            return object;
        }
        Class<?> type = object.getClass();
        if (type.isArray()) {
            return toList(object);
        } else {
            return object;
        }
    }

    /**
     * Read fields value as {@link Map}
     *
     * @param object object to be read
     * @return fields value as {@link Map}
     */
    @Nonnull
    public static Map<String, Object> readFieldsAsMap(Object object) {
        Map<String, Object> fieldsAsMap = new LinkedHashMap();
        Class<?> type = object.getClass();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {

            if (isStatic(field.getModifiers())) { // To filter static fields
                continue;
            }

            trySetAccessible(field);

            try {
                String fieldName = field.getName();
                Object fieldValue = field.get(object);
                if (fieldValue != null && fieldValue != object) {
                    Class<?> fieldValueType = fieldValue.getClass();
                    if (!isPrimitive(fieldValueType)
                            && !isSimpleType(fieldValueType)
                            && !Objects.equals(object.getClass(), fieldValueType)) {
                        fieldValue = readFieldsAsMap(fieldValue);
                    }
                    fieldsAsMap.put(fieldName, fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return fieldsAsMap;
    }


    /**
     * Find the {@link Set} of {@link ParameterizedType}
     *
     * @param sourceClass the source {@link Class class}
     * @return non-null read-only {@link Set}
     */
    public static Set<ParameterizedType> findParameterizedTypes(Class<?> sourceClass) {
        List<Type> genericTypes = new LinkedList<>();
        // Add Generic Interfaces
        addAll(genericTypes, sourceClass.getGenericInterfaces());
        // Add Generic Super Class
        genericTypes.add(sourceClass.getGenericSuperclass());

        Set<ParameterizedType> parameterizedTypes = genericTypes.stream().filter(type -> type instanceof ParameterizedType)// filter ParameterizedType
                .map(type -> (ParameterizedType) type)  // cast to ParameterizedType
                .collect(Collectors.toSet());

        if (parameterizedTypes.isEmpty()) { // If not found, try to search super types recursively
            genericTypes.stream().filter(type -> type instanceof Class).map(type -> (Class) type).forEach(superClass -> {
                parameterizedTypes.addAll(findParameterizedTypes(superClass));
            });
        }

        return unmodifiableSet(parameterizedTypes);                     // build as a Set

    }

    /**
     * Find the hierarchical types from the source {@link Class class} by specified {@link Class type}.
     *
     * @param sourceClass the source {@link Class class}
     * @param matchType   the type to match
     * @param <T>         the type to match
     * @return non-null read-only {@link Set}
     */
    public static <T> Set<Class<T>> findHierarchicalTypes(Class<?> sourceClass, Class<T> matchType) {
        if (sourceClass == null) {
            return emptySet();
        }

        Set<Class<T>> hierarchicalTypes = new LinkedHashSet<>();

        if (matchType.isAssignableFrom(sourceClass)) {
            hierarchicalTypes.add((Class<T>) sourceClass);
        }

        // Find all super classes
        hierarchicalTypes.addAll(findHierarchicalTypes(sourceClass.getSuperclass(), matchType));

        return unmodifiableSet(hierarchicalTypes);
    }

    /**
     * Get the value from the specified bean and its getter method.
     *
     * @param bean       the bean instance
     * @param methodName the name of getter
     * @param <T>        the type of property value
     * @return
     */
    public static <T> T getProperty(Object bean, String methodName) {
        Class<?> beanClass = bean.getClass();
        BeanInfo beanInfo = null;
        T propertyValue = null;

        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
            propertyValue = (T) Stream.of(beanInfo.getMethodDescriptors()).filter(methodDescriptor -> methodName.equals(methodDescriptor.getName())).findFirst().map(method -> {
                try {
                    return method.getMethod().invoke(bean);
                } catch (Exception e) {
                    //ignore
                }
                return null;
            }).get();
        } catch (Exception e) {

        }
        return propertyValue;
    }

    /**
     * Resolve the types of the specified values
     *
     * @param values the values
     * @return If can't be resolved, return {@link ArrayUtils#EMPTY_CLASS_ARRAY empty class array}
     */
    public static Class[] resolveTypes(Object... values) {

        if (isEmpty(values)) {
            return EMPTY_CLASS_ARRAY;
        }

        int size = values.length;

        Class[] types = new Class[size];

        for (int i = 0; i < size; i++) {
            Object value = values[i];
            types[i] = value == null ? null : value.getClass();
        }

        return types;
    }

    /**
     * Determine whether the specified {@link Throwable} is {@link java.lang.reflect.InaccessibleObjectException}
     *
     * @param failure {@link Throwable} instance
     * @return <code>true</code> if the specified {@link Throwable} is {@link java.lang.reflect.InaccessibleObjectException}
     */
    public static boolean isInaccessibleObjectException(Throwable failure) {
        return failure != null && INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME.equals(failure.getClass().getName());
    }

}