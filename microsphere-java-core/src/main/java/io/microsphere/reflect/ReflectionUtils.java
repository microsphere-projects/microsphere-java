package io.microsphere.reflect;


import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static io.microsphere.util.ClassUtils.isSimpleType;
import static java.lang.Class.forName;
import static java.lang.reflect.Modifier.isStatic;

/**
 * Reflection Utility class , generic methods are defined from {@link FieldUtils} , {@link MethodUtils} , {@link
 * ConstructorUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
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

    /**
     * The {@link Class class} of {@linkplain java.lang.reflect.InaccessibleObjectException} since JDK 9.
     * It may be <code>null</code> if the JDK version is less than 9.
     */
    @Nullable
    public static final Class<? extends Throwable> INACCESSIBLE_OBJECT_EXCEPTION_CLASS = (Class<? extends Throwable>) resolveClass(INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME);

    // Initialize sun.reflect.Reflection
    static {
        Method method = null;
        boolean supported = false;
        int invocationFrame = 0;
        try {
            // Use sun.reflect.Reflection to calculate frame
            Class<?> type = forName(SUN_REFLECT_REFLECTION_CLASS_NAME);
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
            targetClass = className == null ? null : forName(className);
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
     * Determine whether the specified {@link Throwable} is {@link java.lang.reflect.InaccessibleObjectException}
     *
     * @param failure {@link Throwable} instance
     * @return <code>true</code> if the specified {@link Throwable} is {@link java.lang.reflect.InaccessibleObjectException}
     */
    public static boolean isInaccessibleObjectException(Throwable failure) {
        return failure != null && INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME.equals(failure.getClass().getName());
    }

}