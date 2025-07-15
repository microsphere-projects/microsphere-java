package io.microsphere.reflect;


import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

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
public abstract class ReflectionUtils implements Utils {

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
     * Checks if the {@code sun.reflect.Reflection} class is available and supported in the current JVM.
     *
     * <p>This method determines whether the internal Sun JDK class {@code sun.reflect.Reflection}
     * can be used to retrieve caller class information. This class and its methods are specific
     * to the Sun/HotSpot JVM and may not be present or functional on other JVM implementations.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * if (ReflectionUtils.isSupportedSunReflectReflection()) {
     *     System.out.println("sun.reflect.Reflection is supported.");
     * } else {
     *     System.out.println("sun.reflect.Reflection is NOT supported.");
     * }
     * }</pre>
     *
     * @return <code>true</code> if the current JVM supports the {@code sun.reflect.Reflection} class;
     * <code>false</code> otherwise.
     */
    public static boolean isSupportedSunReflectReflection() {
        return supportedSunReflectReflection;
    }

    /**
     * Retrieves the fully qualified name of the class that called the method invoking this method.
     *
     * <p>This method attempts to use the internal Sun JDK class
     * {@code sun.reflect.Reflection} for high-performance caller class detection if
     * available. If not supported (e.g., non-Sun/HotSpot JVM), it falls back to using
     * the {@link StackTraceElement} approach.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public void exampleMethod() {
     *         String callerClassName = ReflectionUtils.getCallerClassName();
     *         System.out.println("Caller class: " + callerClassName);
     *     }
     * }
     * }</pre>
     *
     * <h3>Performance Consideration</h3>
     * <p>On Sun/HotSpot JVMs, this method is highly efficient as it leverages
     * internal JVM mechanisms. On other JVMs, a stack trace-based approach is used,
     * which may be less performant but ensures compatibility.</p>
     *
     * @return The fully qualified name of the caller class.
     * @throws IllegalStateException if an error occurs while determining the caller class.
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
     * Gets the {@link Class} of the method caller.
     *
     * <p>This method attempts to retrieve the calling class using the
     * {@code sun.reflect.Reflection} class if supported by the current JVM.
     * If not supported (e.g., non-Sun/HotSpot JVM), it falls back to using
     * the {@link StackTraceElement} approach.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public void testMethod() {
     *         Class<?> callerClass = ReflectionUtils.getCallerClass();
     *         System.out.println("Caller class: " + callerClass.getName());
     *     }
     * }
     * }</pre>
     *
     * @return The {@link Class} of the method caller.
     * @throws IllegalStateException if an error occurs while trying to determine the caller class via reflection.
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
     * Retrieves the class of the caller at the specified invocation frame.
     *
     * <p>This method attempts to use the internal Sun JDK class
     * {@code sun.reflect.Reflection} for high-performance caller class detection if
     * available and supported. If not supported (e.g., non-Sun/HotSpot JVM), it falls back to using
     * the {@link StackTraceElement} approach.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public void exampleMethod() {
     *         Class<?> callerClass = ReflectionUtils.getCallerClass(2);
     *         System.out.println("Caller class: " + callerClass.getName());
     *     }
     * }
     * }</pre>
     *
     * @param invocationFrame The depth in the call stack to retrieve the caller class from.
     *                        A value of 0 typically represents the immediate caller, but this may vary
     *                        depending on the JVM implementation and call context.
     * @return The class of the caller at the specified invocation frame.
     * @throws IllegalStateException if an error occurs while determining the caller class.
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
     * Converts an array object into a {@link List}.
     *
     * <p>This method is useful for converting any array type (including nested arrays) into a list structure.
     * If the array contains nested arrays, they will be recursively converted into lists as well.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] stringArray = {"apple", "banana", "cherry"};
     * List<String> stringList = ReflectionUtils.toList(stringArray);
     * System.out.println(stringList);  // Output: [apple, banana, cherry]
     *
     * Integer[][] nestedArray = {{1, 2}, {3, 4}};
     * List<List<Integer>> nestedList = ReflectionUtils.toList(nestedArray);
     * System.out.println(nestedList);  // Output: [[1, 2], [3, 4]]
     * }</pre>
     *
     * @param array The array object to convert. Must be a valid Java array.
     * @return A list representation of the provided array.
     * @throws IllegalArgumentException if the input is not a valid array object.
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
     * Reads all non-static fields of the given object and returns them as a map.
     * <p>
     * This method recursively processes nested objects, converting them into maps as well,
     * provided they are not primitive or simple types (e.g., String, Number).
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Person {
     *     private String name;
     *     private int age;
     *     private Address address; // Assume Address is another POJO class
     *
     *     // constructor, getters, setters...
     * }
     *
     * class Address {
     *     private String city;
     *     private String street;
     *
     *     // constructor, getters, setters...
     * }
     *
     * Person person = new Person("John", 30, new Address("New York", "5th Avenue"));
     * Map<String, Object> result = ReflectionUtils.readFieldsAsMap(person);
     *
     * // Sample output:
     * // {
     * //   "name": "John",
     * //   "age": 30,
     * //   "address": {
     * //     "city": "New York",
     * //     "street": "5th Avenue"
     * //   }
     * // }
     * }</pre>
     *
     * @param object The object whose fields are to be read.
     * @return A map containing field names as keys and their corresponding values. Nested objects are also converted to maps.
     * @throws IllegalStateException if any field cannot be accessed due to security restrictions.
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
     * Checks whether the specified {@link Throwable} is an instance of
     * {@link java.lang.reflect.InaccessibleObjectException}.
     *
     * <p>This method is useful when dealing with reflection operations that may fail due to module system
     * restrictions introduced in JDK 9+. It avoids direct dependency on the presence of the class, which
     * may not be available in earlier JDK versions.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try {
     *     Field field = MyClass.class.getDeclaredField("myField");
     *     field.setAccessible(true); // This might throw InaccessibleObjectException
     * } catch (Throwable t) {
     *     if (ReflectionUtils.isInaccessibleObjectException(t)) {
     *         System.err.println("Caught InaccessibleObjectException: " + t.getMessage());
     *     } else {
     *         throw new RuntimeException("Unexpected error", t);
     *     }
     * }
     * }</pre>
     *
     * @param failure The {@link Throwable} to check.
     * @return <code>true</code> if the specified {@link Throwable} is an instance of
     * {@link java.lang.reflect.InaccessibleObjectException}, <code>false</code> otherwise.
     */
    public static boolean isInaccessibleObjectException(Throwable failure) {
        return failure != null && INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME.equals(failure.getClass().getName());
    }

    private ReflectionUtils() {
    }

}