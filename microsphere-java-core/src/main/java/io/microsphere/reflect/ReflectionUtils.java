package io.microsphere.reflect;


import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.reflect.TypeUtils.getTypeName;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.getType;
import static io.microsphere.util.ClassUtils.isPrimitive;
import static io.microsphere.util.ClassUtils.isSimpleType;
import static io.microsphere.util.StackTraceUtils.getCallerClassNameInStackTrace;
import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

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
     * The {@link Class} of sun.reflect.Reflection
     */
    @Nullable
    public static final Class<?> SUN_REFLECT_REFLECTION_CLASS = resolveClass(SUN_REFLECT_REFLECTION_CLASS_NAME);

    /**
     * sun.reflect.Reflection method name
     */
    private static final String getCallerClassMethodName = "getCallerClass";

    /**
     * The {@link MethodHandle} of Reflection#getCallerClass(int)
     */
    @Nullable
    private static final MethodHandle getCallerClassMethodHandle = findPublicStatic(SUN_REFLECT_REFLECTION_CLASS, getCallerClassMethodName, int.class);

    /**
     * sun.reflect.Reflection invocation frame offset
     */
    private static final int sunReflectReflectionInvocationFrameOffset;

    /**
     * Is Supported sun.reflect.Reflection ?
     */
    private static final boolean supportedSunReflectReflection = getCallerClassMethodHandle != null;

    /**
     * The class name of {@linkplain java.lang.StackWalker} that was introduced in JDK 9.
     */
    public static final String STACK_WALKER_CLASS_NAME = "java.lang.StackWalker";

    /**
     * The class name of {@linkplain java.lang.StackWalker.StackFrame} that was introduced in JDK 9.
     */
    public static final String STACK_WALKER_STACK_FRAME_CLASS_NAME = "java.lang.StackWalker$StackFrame";

    /**
     * The {@link Class} of {@linkplain java.lang.StackWalker} that was introduced in JDK 9.
     * (optional)
     */
    @Nullable
    public static final Class<?> STACK_WALKER_CLASS = resolveClass(STACK_WALKER_CLASS_NAME);

    /**
     * The {@link Class} of {@linkplain java.lang.StackWalker.StackFrame} that was introduced in JDK 9.
     * (optional)
     */
    @Nullable
    public static final Class<?> STACK_WALKER_STACK_FRAME_CLASS = resolveClass(STACK_WALKER_STACK_FRAME_CLASS_NAME);

    /**
     * The {@link Method method} name of {@linkplain java.lang.StackWalker#getInstance()}
     */
    static final String GET_INSTANCE_METHOD_NAME = "getInstance";

    /**
     * The {@link Method method} name of {{@linkplain java.lang.StackWalker#walk(java.util.function.Function)}
     */
    static final String WALK_METHOD_NAME = "walk";

    /**
     * The {@link Method method} name of {@linkplain java.lang.StackWalker.StackFrame#getClassName()}
     */
    static final String GET_CLASS_NAME_METHOD_NAME = "getClassName";

    static final Method WALK_METHOD = findMethod(STACK_WALKER_CLASS, WALK_METHOD_NAME, Function.class);

    static final Method GET_CLASS_NAME_METHOD = findMethod(STACK_WALKER_STACK_FRAME_CLASS, GET_CLASS_NAME_METHOD_NAME);

    @Nullable
    private static Object stackWalkerInstance;

    /**
     * {@linkplain java.lang.StackWalker} invocation frame offset.
     */
    private static final int stackWalkerInvocationFrameOffset;

    private static final Function<Stream<?>, Object> getClassNamesFunction = ReflectionUtils::getCallerClassNamesInStackWalker;

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

    // Initialize java.lang.StackWalker
    static {
        int invocationFrame = 0;
        if (STACK_WALKER_CLASS != null) {
            stackWalkerInstance = invokeStaticMethod(STACK_WALKER_CLASS, GET_INSTANCE_METHOD_NAME);
            List<String> stackFrameClassNames = getCallerClassNamesInStackWalker();
            for (String stackFrameClassName : stackFrameClassNames) {
                if (TYPE.getName().equals(stackFrameClassName)) {
                    break;
                }
                invocationFrame++;
            }
        }
        stackWalkerInvocationFrameOffset = invocationFrame;
    }

    // Initialize sun.reflect.Reflection
    static {
        int invocationFrame = 0;
        if (supportedSunReflectReflection) {
            // Adapt SUN JDK ,The value of invocation frame in JDK 7/8 may be different
            for (int i = 0; i < 9; i++) {
                Class<?> callerClass = getCallerClassInSunReflectReflection(i);
                if (TYPE.equals(callerClass)) {
                    invocationFrame = i;
                    break;
                }
            }
        }

        // Plus 1 , because Invocation getCallerClass()/getCallerClassName() method was considered as increment invocation frame
        // Plus 1 , because Invocation getCallerClassInSunReflectReflection(int) method was considered as increment invocation frame
        sunReflectReflectionInvocationFrameOffset = invocationFrame + 2;
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
            return getCallerClassInSunReflectReflection(sunReflectReflectionInvocationFrameOffset).getName();
        }
        return getCallerClassName(stackWalkerInstance, 1);
    }

    @Nullable
    static String getCallerClassName(Object stackWalkerInstance, int frameOffSet) {
        if (stackWalkerInstance == null) {
            // Plus 1 , because Invocation getCallerClassName() method was considered as increment invocation frame
            // Plus 1 , because Invocation getCallerClassName(Object stackWalkerInstance, int frameOffSet) method was considered as increment invocation frame
            // Plus 1 , because Invocation getCallerClassNameInStackTrace(int) method was considered as increment invocation frame
            return getCallerClassNameInStackTrace(3 + frameOffSet);
        }

        // Plus 1 , because Invocation getCallerClassName() method was considered as increment invocation frame
        // Plus 1, because Invocation getCallerClassName(Object,int) method was considered as increment invocation frame
        List<String> callerClassNames = getCallerClassNamesInStackWalker(stackWalkerInstance);
        int frame = stackWalkerInvocationFrameOffset + 2 + frameOffSet;
        if (frame < callerClassNames.size()) {
            return callerClassNames.get(frame);
        }
        return null;
    }

    @Nonnull
    static List<String> getCallerClassNamesInStackWalker(@Nonnull Object stackWalkerInstance) {
        return invokeMethod(stackWalkerInstance, WALK_METHOD, getClassNamesFunction);
    }

    static List<String> getCallerClassNamesInStackWalker() {
        return invokeMethod(stackWalkerInstance, WALK_METHOD, getClassNamesFunction);
    }

    private static List<String> getCallerClassNamesInStackWalker(Stream<?> stackFrames) {
        return stackFrames.limit(9)
                .map(ReflectionUtils::getClassName)
                .collect(Collectors.toList());
    }

    private static String getClassName(Object stackFrame) {
        return invokeMethod(stackFrame, GET_CLASS_NAME_METHOD);
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
        Class<?> callerClass = getCallerClassInSunReflectReflection(sunReflectReflectionInvocationFrameOffset);
        if (callerClass != null) {
            return callerClass;
        }
        String className = getCallerClassName(stackWalkerInstance, 1);
        return resolveClass(className);
    }

    @Nullable
    static Class<?> getCallerClassInSunReflectReflection(int realFramesToSkip) {
        try {
            return (Class<?>) getCallerClassMethodHandle.invokeExact(realFramesToSkip);
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Get caller class In SUN HotSpot JVM
     *
     * @return Caller Class
     * @see #getCallerClassInSunReflectReflection(int)
     */
    @Nullable
    static Class<?> getCallerClassInSunReflectReflection() {
        return getCallerClassInSunReflectReflection(sunReflectReflectionInvocationFrameOffset);
    }

    /**
     * Get caller class name In SUN HotSpot JVM
     *
     * @return Caller Class
     * @see #getCallerClassInSunReflectReflection(int)
     */
    @Nullable
    static String getCallerClassNameInSunReflectReflection() {
        Class<?> callerClass = getCallerClassInSunReflectReflection(sunReflectReflectionInvocationFrameOffset);
        return callerClass == null ? null : callerClass.getName();
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
        int length = getLength(array);
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Object element = get(array, i);
            list.add((T) toObject(element));
        }
        return list;
    }

    static Object toObject(Object object) {
        if (object != null) {
            Class<?> type = object.getClass();
            if (type.isArray()) {
                return toList(object);
            }
        }
        return object;
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
    @Immutable
    public static Map<String, Object> readFieldsAsMap(Object object) {
        if (object == null) {
            return emptyMap();
        }
        Class<?> type = object.getClass();
        Field[] fields = type.getDeclaredFields();
        Map<String, Object> fieldsAsMap = newLinkedHashMap(fields.length);
        for (Field field : fields) {

            if (isStatic(field)) { // To filter static fields
                continue;
            }

            String fieldName = field.getName();
            Object fieldValue = getFieldValue(object, field);
            Class<?> fieldValueType = field.getType();
            if (fieldValue != object) {
                if (!isPrimitive(fieldValueType) && !isSimpleType(fieldValueType)
                        && !object.getClass().equals(fieldValueType)) {
                    fieldValue = readFieldsAsMap(fieldValue);
                }
                fieldsAsMap.put(fieldName, fieldValue);
            }
        }
        return unmodifiableMap(fieldsAsMap);
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
        return isInaccessibleObjectException(getType(failure));
    }

    /**
     * Checks whether the specified {@link Class} represents
     * {@link java.lang.reflect.InaccessibleObjectException}.
     *
     * <p>This method is useful when dealing with reflection operations that may fail due to module system
     * restrictions introduced in JDK 9+. It avoids direct dependency on the presence of the class, which
     * may not be available in earlier JDK versions.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> exceptionClass = SomeException.class;
     * if (ReflectionUtils.isInaccessibleObjectException(exceptionClass)) {
     *     System.err.println("The class represents InaccessibleObjectException");
     * } else {
     *     System.out.println("The class does not represent InaccessibleObjectException");
     * }
     * }</pre>
     *
     * @param throwableClass The {@link Class} to check.
     * @return <code>true</code> if the specified {@link Class} represents
     * {@link java.lang.reflect.InaccessibleObjectException}, <code>false</code> otherwise.
     */
    public static boolean isInaccessibleObjectException(Class<?> throwableClass) {
        return isInaccessibleObjectException(getTypeName(throwableClass));
    }

    static boolean isInaccessibleObjectException(String className) {
        return INACCESSIBLE_OBJECT_EXCEPTION_CLASS_NAME.equals(className);
    }

    private ReflectionUtils() {
    }
}