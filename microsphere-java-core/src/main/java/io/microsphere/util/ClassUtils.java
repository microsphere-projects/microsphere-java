/**
 *
 */
package io.microsphere.util;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.filter.ClassFileJarEntryFilter;
import io.microsphere.io.filter.FileExtensionFilter;
import io.microsphere.io.filter.IOFileFilter;
import io.microsphere.io.scanner.SimpleFileScanner;
import io.microsphere.logging.Logger;
import io.microsphere.reflect.ConstructorUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.constants.FileConstants.CLASS;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.constants.FileConstants.JAR_EXTENSION;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.microsphere.constants.SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.DOLLAR_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.io.FileUtils.resolveRelativePath;
import static io.microsphere.io.filter.FileExtensionFilter.of;
import static io.microsphere.io.scanner.SimpleJarEntryScanner.INSTANCE;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.resolveProtocol;
import static io.microsphere.reflect.ConstructorUtils.findDeclaredConstructors;
import static io.microsphere.reflect.Modifier.isAnnotation;
import static io.microsphere.reflect.Modifier.isSynthetic;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.isNotBlank;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.startsWith;
import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.StringUtils.substringBefore;
import static io.microsphere.util.StringUtils.substringBeforeLast;
import static io.microsphere.util.StringUtils.substringBetween;
import static io.microsphere.util.TypeFinder.classFinder;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * {@link Class} utility class
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see Class
 * @see ClassLoaderUtils
 * @see ConstructorUtils
 * @since 1.0.0
 */
public abstract class ClassUtils implements Utils {

    private final static Logger logger = getLogger(ClassUtils.class);

    /**
     * Suffix for array class names: "[]"
     */
    @Nonnull
    @Immutable
    public static final String ARRAY_SUFFIX = "[]";

    @Nonnull
    @Immutable
    private static final Class<?>[] PRIMITIVE_TYPES_ARRAY = ofArray(
            Void.TYPE,
            Boolean.TYPE,
            Byte.TYPE,
            Character.TYPE,
            Short.TYPE,
            Integer.TYPE,
            Long.TYPE,
            Float.TYPE,
            Double.TYPE
    );

    @Nonnull
    @Immutable
    private static final Class<?>[] WRAPPER_TYPES_ARRAY = ofArray(
            Void.class,
            Boolean.class,
            Byte.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class
    );

    @Nonnull
    @Immutable
    public static final Set<Class<?>> PRIMITIVE_TYPES = ofSet(PRIMITIVE_TYPES_ARRAY);

    @Nonnull
    @Immutable
    public static final Set<Class<?>> WRAPPER_TYPES = ofSet(WRAPPER_TYPES_ARRAY);

    @Nonnull
    @Immutable
    public static final Set<Class<?>> PRIMITIVE_ARRAY_TYPES = ofSet(
            boolean[].class,
            char[].class,
            byte[].class,
            short[].class,
            int[].class,
            long[].class,
            float[].class,
            double[].class
    );

    /**
     * Simple Types including:
     * <ul>
     *     <li>{@link Void}</li>
     *     <li>{@link Boolean}</li>
     *     <li>{@link Character}</li>
     *     <li>{@link Byte}</li>
     *     <li>{@link Integer}</li>
     *     <li>{@link Float}</li>
     *     <li>{@link Double}</li>
     *     <li>{@link String}</li>
     *     <li>{@link BigDecimal}</li>
     *     <li>{@link BigInteger}</li>
     *     <li>{@link Date}</li>
     *     <li>{@link Object}</li>
     * </ul>
     *
     * @see javax.management.openmbean.SimpleType
     */
    @Immutable
    public static final Set<Class<?>> SIMPLE_TYPES;

    static {
        Class<?>[] otherSimpleTypes = ofArray(
                String.class,
                BigDecimal.class,
                BigInteger.class,
                Date.class,
                Object.class);

        Set<Class<?>> simpleTypes = newFixedLinkedHashSet(WRAPPER_TYPES_ARRAY.length + otherSimpleTypes.length);
        addAll(simpleTypes, WRAPPER_TYPES_ARRAY);
        addAll(simpleTypes, otherSimpleTypes);

        SIMPLE_TYPES = unmodifiableSet(simpleTypes);
    }

    /**
     * A map with primitive wrapper type as key and corresponding primitive type
     * as value, for example: Integer.class -> int.class.
     */
    @Immutable
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPES_MAP;

    static {
        int size = WRAPPER_TYPES_ARRAY.length;
        Map<Class<?>, Class<?>> wrapperToPrimitiveTypesMap = newFixedLinkedHashMap(size);

        for (int i = 0; i < size; i++) {
            Class<?> wrapperType = WRAPPER_TYPES_ARRAY[i];
            Class<?> primitiveType = PRIMITIVE_TYPES_ARRAY[i];
            wrapperToPrimitiveTypesMap.put(wrapperType, primitiveType);
        }

        WRAPPER_TO_PRIMITIVE_TYPES_MAP = unmodifiableMap(wrapperToPrimitiveTypesMap);
    }

    /**
     * A map with primitive type as key and its wrapper type
     * as value, for example: int.class -> Integer.class.
     */
    @Immutable
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPES_MAP;

    static {
        int size = PRIMITIVE_TYPES_ARRAY.length;
        Map<Class<?>, Class<?>> primitiveToWrapperTypesMap = newFixedLinkedHashMap(size);

        for (int i = 0; i < size; i++) {
            Class<?> primitiveType = PRIMITIVE_TYPES_ARRAY[i];
            Class<?> wrapperType = WRAPPER_TYPES_ARRAY[i];
            primitiveToWrapperTypesMap.put(primitiveType, wrapperType);
        }

        PRIMITIVE_TO_WRAPPER_TYPES_MAP = unmodifiableMap(primitiveToWrapperTypesMap);
    }

    /**
     * A map with primitive type name as key and corresponding primitive type as
     * value, for example: "int" -> "int.class".
     */
    @Immutable
    private static final Map<String, Class<?>> NAME_TO_TYPE_PRIMITIVE_MAP;

    static {
        Map<String, Class<?>> primitiveTypeNameMap = newFixedLinkedHashMap(PRIMITIVE_TYPES.size() + PRIMITIVE_ARRAY_TYPES.size());

        PRIMITIVE_TYPES.forEach(type -> {
            primitiveTypeNameMap.put(type.getName(), type);
        });

        PRIMITIVE_ARRAY_TYPES.forEach(type -> {
            primitiveTypeNameMap.put(type.getName(), type);
        });

        NAME_TO_TYPE_PRIMITIVE_MAP = unmodifiableMap(primitiveTypeNameMap);
    }

    static final Map<Class<?>, Boolean> concreteClassCache = synchronizedMap(new WeakHashMap<>());

    private static final FileExtensionFilter JAR_FILE_EXTENSION_FILTER = of(JAR_EXTENSION);

    /**
     * Checks if the specified object is an array.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isArray(new int[]{1, 2, 3});  // returns true
     * boolean result2 = ClassUtils.isArray("Hello");             // returns false
     * boolean result3 = ClassUtils.isArray(null);                // returns false
     * }</pre>
     *
     * @param object the object to check, may be {@code null}
     * @return {@code true} if the specified object is an array, {@code false} otherwise
     * @see #isArray(Class)
     * @see Class#isArray()
     */
    public static boolean isArray(Object object) {
        return isArray(getClass(object));
    }

    /**
     * Checks if the specified type is an array.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isArray(int[].class);     // returns true
     * boolean result2 = ClassUtils.isArray(String.class);    // returns false
     * boolean result3 = ClassUtils.isArray(null);           // returns false
     * }</pre>
     *
     * @param type the type to check, may be {@code null}
     * @return {@code true} if the specified type is an array class, {@code false} otherwise
     * @see Class#isArray()
     */
    public static boolean isArray(@Nullable Class<?> type) {
        return type != null && type.isArray();
    }

    /**
     * Checks if the specified type is a concrete class.
     * <p>
     * A concrete class is a class that:
     * <ul>
     *   <li>Is not an interface</li>
     *   <li>Is not an annotation</li>
     *   <li>Is not an enum</li>
     *   <li>Is not a synthetic class</li>
     *   <li>Is not a primitive type</li>
     *   <li>Is not an array</li>
     *   <li>Is not abstract</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isConcreteClass(String.class);  // returns true
     * boolean result2 = ClassUtils.isConcreteClass(List.class);    // returns false (interface)
     * boolean result3 = ClassUtils.isConcreteClass(null);         // returns false
     * }</pre>
     *
     * @param type the type to check, may be {@code null}
     * @return {@code true} if the specified type is a concrete class, {@code false} otherwise
     * @see #isGeneralClass(Class, Boolean)
     */
    public static boolean isConcreteClass(@Nullable Class<?> type) {
        if (type == null) {
            return false;
        }
        if (concreteClassCache.containsKey(type)) {
            return true;
        } else if (isGeneralClass(type, FALSE)) {
            concreteClassCache.put(type, TRUE);
            return true;
        }
        return false;
    }

    /**
     * Checks if the specified type is an abstract class.
     * <p>
     * An abstract class is a class that:
     * <ul>
     *   <li>Is not an interface</li>
     *   <li>Is not an annotation</li>
     *   <li>Is not an enum</li>
     *   <li>Is not a synthetic class</li>
     *   <li>Is not a primitive type</li>
     *   <li>Is not an array</li>
     *   <li>Is abstract</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * abstract class AbstractExample { }
     * class ConcreteExample { }
     *
     * boolean result1 = ClassUtils.isAbstractClass(AbstractExample.class); // returns true
     * boolean result2 = ClassUtils.isAbstractClass(ConcreteExample.class); // returns false
     * boolean result3 = ClassUtils.isAbstractClass(null);                  // returns false
     * }</pre>
     *
     * @param type the type to check, may be {@code null}
     * @return {@code true} if the specified type is an abstract class, {@code false} otherwise
     * @see #isGeneralClass(Class, Boolean)
     */
    public static boolean isAbstractClass(@Nullable Class<?> type) {
        return isGeneralClass(type, TRUE);
    }

    /**
     * Checks if the specified type is a general class.
     * <p>
     * A general class is a class that:
     * <ul>
     *   <li>Is not an interface</li>
     *   <li>Is not an annotation</li>
     *   <li>Is not an enum</li>
     *   <li>Is not a synthetic class</li>
     *   <li>Is not a primitive type</li>
     *   <li>Is not an array</li>
     *   <li>Is not abstract (by default)</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class ExampleClass {}
     * abstract class AbstractExample {}
     * interface ExampleInterface {}
     *
     * boolean result1 = ClassUtils.isGeneralClass(ExampleClass.class);      // returns true
     * boolean result2 = ClassUtils.isGeneralClass(AbstractExample.class);   // returns false (abstract)
     * boolean result3 = ClassUtils.isGeneralClass(ExampleInterface.class);  // returns false (interface)
     * boolean result4 = ClassUtils.isGeneralClass(null);                   // returns false
     * }</pre>
     *
     * @param type the type to check, may be {@code null}
     * @return {@code true} if the specified type is a general class, {@code false} otherwise
     * @see #isGeneralClass(Class, Boolean)
     */
    public static boolean isGeneralClass(@Nullable Class<?> type) {
        return isGeneralClass(type, FALSE);
    }

    /**
     * Is the specified type a general class or not?
     * <p>
     * If <code>isAbstract</code> == <code>null</code>,  it will not check <code>type</code> is abstract or not.
     *
     * @param type       the type
     * @param isAbstract optional abstract flag
     * @return true if type is a general (abstract) class, false otherwise.
     */
    protected static boolean isGeneralClass(@Nullable Class<?> type, @Nullable Boolean isAbstract) {
        if (type == null) {
            return false;
        }

        int mod = type.getModifiers();
        if (isAnnotation(mod)
                || io.microsphere.reflect.Modifier.isEnum(mod)
                || isInterface(mod)
                || isSynthetic(mod)
                || isPrimitive(type)
                || isArray(type)) {
            return false;
        }

        if (isAbstract != null) {
            return isAbstract(mod) == isAbstract.booleanValue();
        }
        return true;
    }

    /**
     * Checks if the specified class is a top-level class.
     * <p>
     * A top-level class is a class that is not a local class and not a member class.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class TopLevelClass {}
     *
     * class OuterClass {
     *     static class StaticNestedClass {}
     *     class InnerClass {}
     *
     *     void method() {
     *         class LocalClass {}
     *     }
     * }
     *
     * boolean result1 = ClassUtils.isTopLevelClass(TopLevelClass.class);     // returns true
     * boolean result2 = ClassUtils.isTopLevelClass(OuterClass.InnerClass.class); // returns false
     * boolean result3 = ClassUtils.isTopLevelClass(OuterClass.StaticNestedClass.class); // returns true
     * boolean result4 = ClassUtils.isTopLevelClass(null);                    // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the specified class is a top-level class, {@code false} otherwise
     * @see Class#isLocalClass()
     * @see Class#isMemberClass()
     */
    public static boolean isTopLevelClass(@Nullable Class<?> type) {
        return type != null && !type.isLocalClass() && !type.isMemberClass();
    }

    /**
     * Checks if the specified class is a primitive type.
     * <p>
     * This method returns {@code true} if the given class is one of the eight primitive types
     * ({@code boolean}, {@code byte}, {@code char}, {@code short}, {@code int}, {@code long}, {@code float}, {@code double})
     * or their corresponding array types. It returns {@code false} otherwise, including for wrapper classes.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isPrimitive(int.class);        // returns true
     * boolean result2 = ClassUtils.isPrimitive(Integer.class);    // returns false (wrapper class)
     * boolean result3 = ClassUtils.isPrimitive(int[].class);      // returns true (primitive array)
     * boolean result4 = ClassUtils.isPrimitive(String.class);     // returns false
     * boolean result5 = ClassUtils.isPrimitive(null);            // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the specified class is a primitive type or primitive array type, {@code false} otherwise
     * @see #PRIMITIVE_TYPES
     * @see Class#isPrimitive()
     */
    public static boolean isPrimitive(@Nullable Class<?> type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    /**
     * Checks if the specified class is a final class.
     * <p>
     * A final class is a class that cannot be extended by other classes.
     * This method returns {@code true} if the given class is marked with the {@code final} modifier,
     * and {@code false} otherwise. It also returns {@code false} if the class is {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * final class FinalExample {}
     * class NonFinalExample {}
     *
     * boolean result1 = ClassUtils.isFinal(FinalExample.class);   // returns true
     * boolean result2 = ClassUtils.isFinal(NonFinalExample.class); // returns false
     * boolean result3 = ClassUtils.isFinal(null);                 // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the specified class is a final class, {@code false} otherwise
     * @see java.lang.reflect.Modifier#isFinal(int)
     */
    public static boolean isFinal(@Nullable Class<?> type) {
        return type != null && Modifier.isFinal(type.getModifiers());
    }

    /**
     * Checks if the specified object is a simple type.
     * <p>
     * A simple type is defined as one of the following:
     * <ul>
     *   <li>{@link Void}</li>
     *   <li>{@link Boolean}</li>
     *   <li>{@link Character}</li>
     *   <li>{@link Byte}</li>
     *   <li>{@link Short}</li>
     *   <li>{@link Integer}</li>
     *   <li>{@link Long}</li>
     *   <li>{@link Float}</li>
     *   <li>{@link Double}</li>
     *   <li>{@link String}</li>
     *   <li>{@link BigDecimal}</li>
     *   <li>{@link BigInteger}</li>
     *   <li>{@link Date}</li>
     *   <li>{@link Object}</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isSimpleType("Hello");  // returns true
     * boolean result2 = ClassUtils.isSimpleType(123);      // returns true
     * boolean result3 = ClassUtils.isSimpleType(new ArrayList<>()); // returns false
     * boolean result4 = ClassUtils.isSimpleType(null);     // returns false
     * }</pre>
     *
     * @param object the object to check, may be {@code null}
     * @return {@code true} if the specified object is a simple type, {@code false} otherwise
     * @see #isSimpleType(Class)
     */
    public static boolean isSimpleType(@Nullable Object object) {
        return isSimpleType(getClass(object));
    }

    /**
     * Checks if the specified type is a simple type.
     * <p>
     * A simple type is defined as one of the following:
     * <ul>
     *   <li>{@link Void}</li>
     *   <li>{@link Boolean}</li>
     *   <li>{@link Character}</li>
     *   <li>{@link Byte}</li>
     *   <li>{@link Short}</li>
     *   <li>{@link Integer}</li>
     *   <li>{@link Long}</li>
     *   <li>{@link Float}</li>
     *   <li>{@link Double}</li>
     *   <li>{@link String}</li>
     *   <li>{@link BigDecimal}</li>
     *   <li>{@link BigInteger}</li>
     *   <li>{@link Date}</li>
     *   <li>{@link Object}</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isSimpleType(String.class);  // returns true
     * boolean result2 = ClassUtils.isSimpleType(List.class);    // returns false
     * boolean result3 = ClassUtils.isSimpleType(null);         // returns false
     * }</pre>
     *
     * @param type the type to check, may be {@code null}
     * @return {@code true} if the specified type is a simple type, {@code false} otherwise
     * @see #SIMPLE_TYPES
     */
    public static boolean isSimpleType(@Nullable Class<?> type) {
        return SIMPLE_TYPES.contains(type);
    }

    /**
     * Checks if the given object is an instance of {@link CharSequence}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isCharSequence("Hello");     // returns true
     * boolean result2 = ClassUtils.isCharSequence(new StringBuilder("World")); // returns true
     * boolean result3 = ClassUtils.isCharSequence(123);         // returns false
     * boolean result4 = ClassUtils.isCharSequence(null);        // returns false
     * }</pre>
     *
     * @param value the object to check, may be {@code null}
     * @return {@code true} if the object is an instance of {@link CharSequence}, {@code false} otherwise
     * @see #isCharSequence(Class)
     */
    public static boolean isCharSequence(@Nullable Object value) {
        return value instanceof CharSequence;
    }

    /**
     * Checks if the given type is a subtype of {@link CharSequence}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     boolean result = ClassUtils.isCharSequence(String.class); // returns true
     *     boolean result2 = ClassUtils.isCharSequence(Integer.class); // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the type is a subtype of {@link CharSequence}, {@code false} otherwise
     */
    public static boolean isCharSequence(@Nullable Class<?> type) {
        return isAssignableFrom(CharSequence.class, type);
    }

    /**
     * Checks if the given object is an instance of {@link Number}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isNumber(Integer.valueOf(10));  // returns true
     * boolean result2 = ClassUtils.isNumber(10.5);                 // returns true
     * boolean result3 = ClassUtils.isNumber("123");                // returns false
     * boolean result4 = ClassUtils.isNumber(null);                 // returns false
     * }</pre>
     *
     * @param value the object to check, may be {@code null}
     * @return {@code true} if the object is an instance of {@link Number}, {@code false} otherwise
     * @see #isNumber(Class)
     */
    public static boolean isNumber(@Nullable Object value) {
        return value instanceof Number;
    }

    /**
     * Checks if the given type is a subtype of {@link Number}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     boolean result = ClassUtils.isNumber(Integer.class); // returns true
     *     boolean result2 = ClassUtils.isNumber(String.class); // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the type is a subtype of {@link Number}, {@code false} otherwise
     */
    public static boolean isNumber(@Nullable Class<?> type) {
        return isAssignableFrom(Number.class, type);
    }

    /**
     * Checks if the given object is an instance of {@link Class}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isClass(String.class); // returns true
     * boolean result2 = ClassUtils.isClass("Hello");      // returns false
     * boolean result3 = ClassUtils.isClass(null);         // returns false
     * }</pre>
     *
     * @param object the object to check, may be {@code null}
     * @return {@code true} if the object is an instance of {@link Class}, {@code false} otherwise
     */
    public static boolean isClass(@Nullable Object object) {
        return object instanceof Class;
    }

    /**
     * Checks if the given object is an instance of {@link Enum}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * enum Color { RED, GREEN, BLUE }
     *
     * boolean result1 = ClassUtils.isEnum(Color.RED);  // returns true
     * boolean result2 = ClassUtils.isEnum("RED");      // returns false
     * boolean result3 = ClassUtils.isEnum(null);       // returns false
     * }</pre>
     *
     * @param object the object to check, may be {@code null}
     * @return {@code true} if the object is an instance of {@link Enum}, {@code false} otherwise
     * @see #isEnum(Class)
     */
    public static boolean isEnum(@Nullable Object object) {
        return object instanceof Enum;
    }

    /**
     * Checks if the specified class is an enum.
     * <p>
     * This method returns {@code true} if the given class is an enum type,
     * and {@code false} otherwise. It also returns {@code false} if the class is {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * enum Color { RED, GREEN, BLUE }
     *
     * boolean result1 = ClassUtils.isEnum(Color.class);  // returns true
     * boolean result2 = ClassUtils.isEnum(String.class); // returns false
     * boolean result3 = ClassUtils.isEnum(null);         // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the specified class is an enum, {@code false} otherwise
     * @see Class#isEnum()
     */
    public static boolean isEnum(@Nullable Class<?> type) {
        return type != null && type.isEnum();
    }

    /**
     * Resolves the primitive type corresponding to the given type.
     * <p>
     * If the provided type is already a primitive type, it is returned as-is.
     * If the provided type is a wrapper type (e.g., {@link Integer}, {@link Boolean}),
     * the corresponding primitive type (e.g., {@code int}, {@code boolean}) is returned.
     * If the provided type is neither a primitive nor a wrapper type, {@code null} is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> result1 = ClassUtils.resolvePrimitiveType(int.class);      // returns int.class
     * Class<?> result2 = ClassUtils.resolvePrimitiveType(Integer.class);  // returns int.class
     * Class<?> result3 = ClassUtils.resolvePrimitiveType(String.class);   // returns null
     * Class<?> result4 = ClassUtils.resolvePrimitiveType(null);          // returns null
     * }</pre>
     *
     * @param type the type to resolve, may be {@code null}
     * @return the corresponding primitive type if the input is a wrapper or primitive type,
     * otherwise {@code null}
     * @see #isPrimitive(Class)
     * @see #WRAPPER_TO_PRIMITIVE_TYPES_MAP
     */
    @Nullable
    public static Class<?> resolvePrimitiveType(@Nullable Class<?> type) {
        if (isPrimitive(type)) {
            return type;
        }
        return WRAPPER_TO_PRIMITIVE_TYPES_MAP.get(type);
    }

    /**
     * Resolve the wrapper class from the primitive type.
     * <p>
     * If the provided type is already a wrapper type, it is returned as-is.
     * If the provided type is a primitive type (e.g., {@code int}, {@code boolean}),
     * the corresponding wrapper type (e.g., {@link Integer}, {@link Boolean}) is returned.
     * If the provided type is neither a primitive nor a wrapper type, {@code null} is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> result1 = ClassUtils.resolveWrapperType(int.class);      // returns Integer.class
     * Class<?> result2 = ClassUtils.resolveWrapperType(Integer.class);  // returns Integer.class
     * Class<?> result3 = ClassUtils.resolveWrapperType(String.class);   // returns null
     * Class<?> result4 = ClassUtils.resolveWrapperType(null);          // returns null
     * }</pre>
     *
     * @param primitiveType the primitive type to resolve, may be {@code null}
     * @return the corresponding wrapper type if the input is a primitive or wrapper type,
     * otherwise {@code null}
     * @see #isWrapperType(Class)
     * @see #PRIMITIVE_TO_WRAPPER_TYPES_MAP
     */
    @Nullable
    public static Class<?> resolveWrapperType(@Nullable Class<?> primitiveType) {
        return tryResolveWrapperType(primitiveType, null);
    }

    /**
     * Attempts to resolve the wrapper type for the given primitive type.
     * <p>
     * If the provided type is already a wrapper type, it is returned as-is.
     * If the provided type is a primitive type (e.g., {@code int}, {@code boolean}),
     * the corresponding wrapper type (e.g., {@link Integer}, {@link Boolean}) is returned.
     * If the provided type is neither a primitive nor a wrapper type, the primitive type itself is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> result1 = ClassUtils.tryResolveWrapperType(int.class);      // returns Integer.class
     * Class<?> result2 = ClassUtils.tryResolveWrapperType(Integer.class);  // returns Integer.class
     * Class<?> result3 = ClassUtils.tryResolveWrapperType(String.class);   // returns String.class
     * Class<?> result4 = ClassUtils.tryResolveWrapperType(null);          // returns null
     * }</pre>
     *
     * @param primitiveType the primitive type to resolve, may be {@code null}
     * @return the corresponding wrapper type if the input is a primitive type,
     * the input type itself if it is a wrapper type, or the input type itself if it is neither
     * @see #isWrapperType(Class)
     * @see #PRIMITIVE_TO_WRAPPER_TYPES_MAP
     */
    public static Class<?> tryResolveWrapperType(@Nullable Class<?> primitiveType) {
        return tryResolveWrapperType(primitiveType, primitiveType);
    }

    /**
     * Attempts to resolve the wrapper type for the given primitive type.
     * <p>
     * If the provided type is already a wrapper type, it is returned as-is.
     * If the provided type is a primitive type (e.g., {@code int}, {@code boolean}),
     * the corresponding wrapper type (e.g., {@link Integer}, {@link Boolean}) is returned.
     * If the provided type is neither a primitive nor a wrapper type, the specified default type is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> result1 = ClassUtils.tryResolveWrapperType(int.class, Object.class);      // returns Integer.class
     * Class<?> result2 = ClassUtils.tryResolveWrapperType(Integer.class, Object.class);  // returns Integer.class
     * Class<?> result3 = ClassUtils.tryResolveWrapperType(String.class, Object.class);   // returns Object.class
     * Class<?> result4 = ClassUtils.tryResolveWrapperType(null, Object.class);          // returns Object.class
     * }</pre>
     *
     * @param type        the type to resolve, may be {@code null}
     * @param defaultType the default type to return if the input type is neither primitive nor wrapper type
     * @return the corresponding wrapper type if the input is a primitive type,
     * the input type itself if it is a wrapper type, or the specified default type if it is neither
     * @see #isWrapperType(Class)
     * @see #PRIMITIVE_TO_WRAPPER_TYPES_MAP
     */
    protected static Class<?> tryResolveWrapperType(@Nullable Class<?> type, @Nullable Class<?> defaultType) {
        if (isWrapperType(type)) {
            return type;
        }
        return PRIMITIVE_TO_WRAPPER_TYPES_MAP.getOrDefault(type, defaultType);
    }
    
    /**
     * Checks if the specified object is a wrapper type.
     * <p>
     * A wrapper type is one of the following:
     * <ul>
     *   <li>{@link Boolean}</li>
     *   <li>{@link Character}</li>
     *   <li>{@link Byte}</li>
     *   <li>{@link Short}</li>
     *   <li>{@link Integer}</li>
     *   <li>{@link Long}</li>
     *   <li>{@link Float}</li>
     *   <li>{@link Double}</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isWrapperType(Integer.valueOf(10));  // returns true
     * boolean result2 = ClassUtils.isWrapperType(10);                   // returns false (primitive type)
     * boolean result3 = ClassUtils.isWrapperType("Hello");              // returns false
     * boolean result4 = ClassUtils.isWrapperType(null);                 // returns false
     * }</pre>
     *
     * @param value the object to check, may be {@code null}
     * @return {@code true} if the specified object is a wrapper type, {@code false} otherwise
     * @see #isWrapperType(Class)
     */
    public static boolean isWrapperType(@Nullable Object value) {
        return isWrapperType(getClass(value));
    }

    /**
     * Checks if the specified class is a wrapper type.
     * <p>
     * A wrapper type is one of the following:
     * <ul>
     *   <li>{@link Boolean}</li>
     *   <li>{@link Character}</li>
     *   <li>{@link Byte}</li>
     *   <li>{@link Short}</li>
     *   <li>{@link Integer}</li>
     *   <li>{@link Long}</li>
     *   <li>{@link Float}</li>
     *   <li>{@link Double}</li>
     * </ul>
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isWrapperType(Integer.class);  // returns true
     * boolean result2 = ClassUtils.isWrapperType(int.class);      // returns false (primitive type)
     * boolean result3 = ClassUtils.isWrapperType(String.class);   // returns false
     * boolean result4 = ClassUtils.isWrapperType(null);          // returns false
     * }</pre>
     *
     * @param type the class to check, may be {@code null}
     * @return {@code true} if the specified class is a wrapper type, {@code false} otherwise
     * @see #WRAPPER_TYPES
     */
    public static boolean isWrapperType(@Nullable Class<?> type) {
        return WRAPPER_TYPES.contains(type);
    }

    /**
     * Checks if two array types are equivalent, including nested arrays.
     * <p>
     * This method compares the component types of two array classes recursively.
     * It returns {@code true} if both classes are arrays and their component types
     * are equal (considering nested arrays), {@code false} otherwise.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.arrayTypeEquals(int[].class, int[].class);           // returns true
     * boolean result2 = ClassUtils.arrayTypeEquals(int[][].class, int[][].class);       // returns true
     * boolean result3 = ClassUtils.arrayTypeEquals(int[].class, long[].class);          // returns false
     * boolean result4 = ClassUtils.arrayTypeEquals(int[].class, int.class);             // returns false
     * boolean result5 = ClassUtils.arrayTypeEquals(null, int[].class);                  // returns false
     * }</pre>
     *
     * @param oneArrayType     the first array type to compare, may be {@code null}
     * @param anotherArrayType the second array type to compare, may be {@code null}
     * @return {@code true} if both types are arrays with equivalent component types,
     * {@code false} otherwise
     * @see Class#getComponentType()
     * @see Objects#equals(Object, Object)
     */
    public static boolean arrayTypeEquals(@Nullable Class<?> oneArrayType, @Nullable Class<?> anotherArrayType) {
        if (!isArray(oneArrayType) || !isArray(anotherArrayType)) {
            return false;
        }
        Class<?> oneComponentType = oneArrayType.getComponentType();
        Class<?> anotherComponentType = anotherArrayType.getComponentType();
        if (isArray(oneComponentType) && isArray(anotherComponentType)) {
            return arrayTypeEquals(oneComponentType, anotherComponentType);
        } else {
            return Objects.equals(oneComponentType, anotherComponentType);
        }
    }

    /**
     * Resolve the given class name as a primitive class, if appropriate,
     * according to the JVM's naming rules for primitive classes.
     * <p>
     * This method checks if the provided name corresponds to a primitive type
     * (e.g., "int", "boolean") or a primitive array type (e.g., "[I", "[Z").
     * It returns the corresponding {@link Class} object if a match is found,
     * or {@code null} otherwise.
     * </p>
     * <p>
     * Note: This method does <i>not</i> support the "[]" suffix notation for
     * primitive arrays; this is only supported by {@link #forName(String)}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> intClass = ClassUtils.resolvePrimitiveClassForName("int");     // returns int.class
     * Class<?> boolArrayClass = ClassUtils.resolvePrimitiveClassForName("[Z"); // returns boolean[].class
     * Class<?> stringClass = ClassUtils.resolvePrimitiveClassForName("String"); // returns null
     * Class<?> nullResult = ClassUtils.resolvePrimitiveClassForName(null);    // returns null
     * }</pre>
     *
     * @param name the name of the potentially primitive class, may be {@code null}
     * @return the primitive class, or {@code null} if the name does not
     * denote a primitive class or primitive array class
     * @see #NAME_TO_TYPE_PRIMITIVE_MAP
     */
    @Nullable
    public static Class<?> resolvePrimitiveClassForName(@Nullable String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = NAME_TO_TYPE_PRIMITIVE_MAP.get(name);
        }
        return result;
    }

    /**
     * Resolve the package name of the given class.
     * <p>
     * This method returns the package name of the specified class. If the class is a primitive type,
     * or if the class is {@code null}, this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String packageName1 = ClassUtils.resolvePackageName(String.class);
     * // returns "java.lang"
     *
     * String packageName2 = ClassUtils.resolvePackageName(int.class);
     * // returns null (primitive type)
     *
     * String packageName3 = ClassUtils.resolvePackageName((Class<?>) null);
     * // returns null
     * }</pre>
     *
     * @param targetClass the class to resolve the package name for, may be {@code null}
     * @return the package name of the given class, or {@code null} if the class is primitive or {@code null}
     * @see #resolvePackageName(String)
     * @see #isPrimitive(Class)
     * @see #getTypeName(Class)
     */
    @Nullable
    public static String resolvePackageName(@Nullable Class<?> targetClass) {
        return isPrimitive(targetClass) ? null : resolvePackageName(getTypeName(targetClass));
    }

    /**
     * Resolve the package name from the given class name.
     * <p>
     * This method extracts the package name from a fully qualified class name.
     * If the class name does not contain a package (i.e., it's a default package),
     * this method returns an empty string. If the input is {@code null}, this method
     * returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String packageName1 = ClassUtils.resolvePackageName("java.lang.String");
     * // returns "java.lang"
     *
     * String packageName2 = ClassUtils.resolvePackageName("String");
     * // returns "" (empty string for default package)
     *
     * String packageName3 = ClassUtils.resolvePackageName(null);
     * // returns null
     * }</pre>
     *
     * @param className the fully qualified class name, may be {@code null}
     * @return the package name, or an empty string if the class is in the default package,
     * or {@code null} if the input is {@code null}
     * @see StringUtils#substringBeforeLast(String, String)
     */
    @Nonnull
    public static String resolvePackageName(@Nullable String className) {
        return substringBeforeLast(className, DOT);
    }

    /**
     * Finds all class names in the specified class path.
     * <p>
     * This method scans the given class path for class files and returns their fully qualified names.
     * The class path can be a directory or a JAR file. If the class path is a directory, the method
     * will recursively scan subdirectories if the {@code recursive} parameter is set to {@code true}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all class names in a directory, recursively
     * Set<String> classNames = ClassUtils.findClassNamesInClassPath("/path/to/classes", true);
     *
     * // Find all class names in a JAR file
     * Set<String> jarClassNames = ClassUtils.findClassNamesInClassPath("file:/path/to/library.jar", false);
     * }</pre>
     *
     * @param classPath the class path to scan, may be {@code null}
     * @param recursive whether to scan subdirectories recursively
     * @return a set of fully qualified class names found in the class path, or an empty set if none found
     * @see #findClassNamesInClassPath(File, boolean)
     */
    @Nonnull
    @Immutable
    public static Set<String> findClassNamesInClassPath(@Nullable String classPath, boolean recursive) {
        String protocol = resolveProtocol(classPath);
        final String resolvedClassPath;
        if (FILE_PROTOCOL.equals(protocol)) {
            resolvedClassPath = substringBetween(classPath, protocol + COLON_CHAR, ARCHIVE_ENTRY_SEPARATOR);
        } else {
            resolvedClassPath = classPath;
        }
        File classesFileHolder = new File(resolvedClassPath); // File or Directory
        return findClassNamesInClassPath(classesFileHolder, recursive);
    }

    /**
     * Finds all class names in the specified class path.
     * <p>
     * This method scans the given class path for class files and returns their fully qualified names.
     * The class path can be a directory or a JAR file. If the class path is a directory, the method
     * will recursively scan subdirectories if the {@code recursive} parameter is set to {@code true}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all class names in a directory, recursively
     * Set<String> classNames = ClassUtils.findClassNamesInClassPath(new File("/path/to/classes"), true);
     *
     * // Find all class names in a JAR file
     * Set<String> jarClassNames = ClassUtils.findClassNamesInClassPath(new File("/path/to/library.jar"), false);
     * }</pre>
     *
     * @param classPath the class path to scan, may be {@code null}
     * @param recursive whether to scan subdirectories recursively
     * @return a set of fully qualified class names found in the class path, or an empty set if none found
     * @see #findClassNamesInDirectory(File, boolean)
     * @see #findClassNamesInJarFile(File, boolean)
     */
    @Nonnull
    @Immutable
    public static Set<String> findClassNamesInClassPath(@Nullable File classPath, boolean recursive) {
        if (classPath == null || !classPath.exists()) {
            return emptySet();
        }

        Set<String> classNames = emptySet();
        if (classPath.isDirectory()) { // Directory
            classNames = findClassNamesInDirectory(classPath, recursive);
        } else if (classPath.isFile()) { // File
            if (JAR_FILE_EXTENSION_FILTER.accept(classPath)) { // JarFile
                classNames = findClassNamesInJarFile(classPath, recursive);
            }
        }

        if (isEmpty(classNames)) {
            if (logger.isTraceEnabled()) {
                logger.trace("No Class was found in the classPath : '{}' , recursive : {}", classPath, recursive);
            }
        }
        return classNames;
    }

    /**
     * Finds all class names in the specified directory.
     * <p>
     * This method scans the given directory for class files and returns their fully qualified names.
     * If the {@code recursive} parameter is set to {@code true}, the method will recursively scan
     * subdirectories. Only files with the ".class" extension are considered.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all class names in a directory, recursively
     * Set<String> classNames = ClassUtils.findClassNamesInDirectory(new File("/path/to/classes"), true);
     *
     * // Find all class names in a directory, non-recursively
     * Set<String> nonRecursiveClassNames = ClassUtils.findClassNamesInDirectory(new File("/path/to/classes"), false);
     * }</pre>
     *
     * @param classesDirectory the directory to scan for class files, may be {@code null}
     * @param recursive        whether to scan subdirectories recursively
     * @return a set of fully qualified class names found in the directory, or an empty set if none found
     * @see SimpleFileScanner#scan(File, boolean, IOFileFilter)
     * @see FileExtensionFilter#of(String)
     */
    @Nonnull
    @Immutable
    public static Set<String> findClassNamesInDirectory(@Nullable File classesDirectory, boolean recursive) {
        if (classesDirectory == null || !classesDirectory.exists()) {
            return emptySet();
        }

        Set<File> classFiles = SimpleFileScanner.INSTANCE.scan(classesDirectory, recursive, of(CLASS));
        if (isEmpty(classFiles)) {
            return emptySet();
        }

        Set<String> classNames = new LinkedHashSet<>();

        for (File classFile : classFiles) {
            String className = resolveClassName(classesDirectory, classFile);
            classNames.add(className);
        }

        return unmodifiableSet(classNames);
    }

    /**
     * Finds all class names in the specified JAR file.
     * <p>
     * This method scans the given JAR file for class files and returns their fully qualified names.
     * If the {@code recursive} parameter is set to {@code true}, the method will recursively scan
     * subdirectories within the JAR file. Only entries with the ".class" extension are considered.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all class names in a JAR file, recursively
     * Set<String> classNames = ClassUtils.findClassNamesInJarFile(new File("/path/to/library.jar"), true);
     *
     * // Find all class names in a JAR file, non-recursively
     * Set<String> nonRecursiveClassNames = ClassUtils.findClassNamesInJarFile(new File("/path/to/library.jar"), false);
     * }</pre>
     *
     * @param jarFile   the JAR file to scan for class files, may be {@code null}
     * @param recursive whether to scan subdirectories recursively
     * @return a set of fully qualified class names found in the JAR file, or an empty set if none found
     * @see ClassFileJarEntryFilter#INSTANCE
     * @see io.microsphere.io.scanner.SimpleJarEntryScanner#INSTANCE
     */
    @Nonnull
    @Immutable
    public static Set<String> findClassNamesInJarFile(@Nullable File jarFile, boolean recursive) {
        if (jarFile == null || !jarFile.exists()) {
            return emptySet();
        }
        Set<String> classNames;
        try {
            JarFile jarFile_ = new JarFile(jarFile);
            Set<JarEntry> jarEntries = INSTANCE.scan(jarFile_, recursive, ClassFileJarEntryFilter.INSTANCE);
            if (isEmpty(jarEntries)) {
                classNames = emptySet();
            } else {
                classNames = newLinkedHashSet();
                for (JarEntry jarEntry : jarEntries) {
                    String jarEntryName = jarEntry.getName();
                    String className = resolveClassName(jarEntryName);
                    if (isNotBlank(className)) {
                        classNames.add(className);
                    }
                }
                classNames = unmodifiableSet(classNames);
            }
        } catch (Exception e) {
            classNames = emptySet();
            if (logger.isTraceEnabled()) {
                logger.trace("The class names can't be resolved by SimpleJarEntryScanner#scan(jarFile = {} ," +
                        " recursive = {} , jarEntryFilter = ClassFileJarEntryFilter)", jarFile, recursive, e);
            }
        }
        return classNames;
    }

    /**
     * Resolves the fully qualified class name from a class file within a classes directory.
     * <p>
     * This method calculates the relative path of the class file from the classes directory,
     * then converts the path to a fully qualified class name by replacing path separators
     * with dots and removing the ".class" extension.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * File classesDir = new File("/project/target/classes");
     * File classFile = new File("/project/target/classes/com/example/MyClass.class");
     * String className = ClassUtils.resolveClassName(classesDir, classFile);
     * // Returns: "com.example.MyClass"
     * }</pre>
     *
     * @param classesDirectory the root directory containing compiled classes, must not be {@code null}
     * @param classFile        the class file to resolve the name for, must not be {@code null}
     * @return the fully qualified class name, never {@code null}
     * @see #resolveRelativePath(File, File)
     * @see #resolveClassName(String)
     */
    protected static String resolveClassName(File classesDirectory, File classFile) {
        String classFileRelativePath = resolveRelativePath(classesDirectory, classFile);
        return resolveClassName(classFileRelativePath);
    }

    /**
     * Resolves the fully qualified class name from a resource name.
     * <p>
     * This method converts a resource name (using '/' as separator) to a fully qualified
     * class name (using '.' as separator) by replacing slashes with dots and removing
     * the ".class" extension if present. It also handles cases where the resource name
     * might start with leading dots.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String className1 = ClassUtils.resolveClassName("com/example/MyClass.class");
     * // Returns: "com.example.MyClass"
     *
     * String className2 = ClassUtils.resolveClassName("com/example/MyClass");
     * // Returns: "com.example.MyClass"
     *
     * String className3 = ClassUtils.resolveClassName("./com/example/MyClass.class");
     * // Returns: "com.example.MyClass"
     *
     * String className4 = ClassUtils.resolveClassName(null);
     * // Returns: null
     * }</pre>
     *
     * @param resourceName the resource name to resolve, may be {@code null}
     * @return the fully qualified class name, or {@code null} if the input is {@code null}
     * @see #resolveClassName(File, File)
     */
    @Nullable
    public static String resolveClassName(@Nullable String resourceName) {
        if (resourceName == null) {
            return null;
        }
        String className = replace(resourceName, SLASH, DOT);
        className = substringBefore(className, CLASS_EXTENSION);
        while (startsWith(className, DOT)) {
            className = substringAfter(className, DOT);
        }
        return className;
    }

    /**
     * Get all super classes from the specified type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<Class<?>> superClasses = ClassUtils.getAllSuperClasses(String.class);
     * // Returns a list containing: [Object.class]
     *
     * List<Class<?>> noSuperClasses = ClassUtils.getAllSuperClasses(Object.class);
     * // Returns an empty list since Object has no superclass
     *
     * List<Class<?>> nullResult = ClassUtils.getAllSuperClasses(null);
     * // Returns an empty list since the input is null
     * }</pre>
     *
     * @param type the specified type, may be {@code null}
     * @return non-null read-only {@link List} of super classes
     * @see #findAllSuperClasses(Class, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> getAllSuperClasses(@Nullable Class<?> type) {
        return findAllSuperClasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all interfaces from the specified type.
     * <p>
     * This method returns a list of all interfaces implemented by the given class or interface,
     * including interfaces inherited from superclasses and superinterfaces. The returned list
     * is read-only and immutable.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // For a class implementing multiple interfaces
     * class Example implements Runnable, Cloneable {}
     * List<Class<?>> interfaces = ClassUtils.getAllInterfaces(Example.class);
     * // Returns a list containing: [Runnable.class, Cloneable.class]
     *
     * // For an interface extending other interfaces
     * interface SubInterface extends Serializable, Comparable<String> {}
     * List<Class<?>> superInterfaces = ClassUtils.getAllInterfaces(SubInterface.class);
     * // Returns a list containing: [Serializable.class, Comparable.class]
     *
     * // For a class with no interfaces
     * class PlainClass {}
     * List<Class<?>> noInterfaces = ClassUtils.getAllInterfaces(PlainClass.class);
     * // Returns an empty list
     *
     * // For null input
     * List<Class<?>> nullResult = ClassUtils.getAllInterfaces(null);
     * // Returns an empty list
     * }</pre>
     *
     * @param type the specified type, may be {@code null}
     * @return non-null read-only {@link List} of interfaces
     * @see #findAllInterfaces(Class, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> getAllInterfaces(@Nullable Class<?> type) {
        return findAllInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all inherited types from the specified type.
     * <p>
     * This method returns a list of all types inherited by the given class or interface,
     * including superclasses and interfaces. The returned list is read-only and immutable.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Parent {}
     * interface MyInterface {}
     * class Child extends Parent implements MyInterface {}
     *
     * List<Class<?>> inheritedTypes = ClassUtils.getAllInheritedTypes(Child.class);
     * // Returns a list containing Parent.class and MyInterface.class
     *
     * List<Class<?>> noInheritedTypes = ClassUtils.getAllInheritedTypes(Object.class);
     * // Returns an empty list since Object has no superclass or interfaces in this context
     *
     * List<Class<?>> nullResult = ClassUtils.getAllInheritedTypes(null);
     * // Returns an empty list since the input is null
     * }</pre>
     *
     * @param type the specified type, may be {@code null}
     * @return non-null read-only {@link List} of inherited types
     * @see #getAllInheritedClasses(Class)
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> getAllInheritedTypes(@Nullable Class<?> type) {
        return getAllInheritedClasses(type);
    }

    /**
     * Get all inherited classes from the specified type.
     * <p>
     * This method returns a list of all classes inherited by the given class,
     * including its superclasses. It does not include interfaces. The returned list
     * is read-only and immutable.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Parent {}
     * class Child extends Parent {}
     *
     * List<Class<?>> inheritedClasses = ClassUtils.getAllInheritedClasses(Child.class);
     * // Returns a list containing Parent.class
     *
     * List<Class<?>> noInheritedClasses = ClassUtils.getAllInheritedClasses(Object.class);
     * // Returns an empty list since Object has no superclass
     *
     * List<Class<?>> nullResult = ClassUtils.getAllInheritedClasses(null);
     * // Returns an empty list since the input is null
     * }</pre>
     *
     * @param type the specified type, may be {@code null}
     * @return non-null read-only {@link List} of inherited classes
     * @see #findAllInheritedClasses(Class, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> getAllInheritedClasses(@Nullable Class<?> type) {
        return findAllInheritedClasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all classes from the specified type, including the type itself, its superclasses, and interfaces.
     * <p>
     * This method returns a list of all classes related to the given class or interface,
     * including the class itself, its superclasses, and interfaces. The returned list
     * is read-only and immutable.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * class Parent {}
     * interface MyInterface {}
     * class Child extends Parent implements MyInterface {}
     *
     * List<Class<?>> allClasses = ClassUtils.getAllClasses(Child.class);
     * // Returns a list containing Child.class, Parent.class, MyInterface.class, and Object.class
     *
     * List<Class<?>> objectClasses = ClassUtils.getAllClasses(Object.class);
     * // Returns a list containing Object.class
     *
     * List<Class<?>> nullResult = ClassUtils.getAllClasses(null);
     * // Returns an empty list since the input is null
     * }</pre>
     *
     * @param type the specified type, may be {@code null}
     * @return non-null read-only {@link List} of all classes related to the specified type
     * @see #findAllClasses(Class, Predicate[])
     */
    public static List<Class<?>> getAllClasses(@Nullable Class<?> type) {
        return findAllClasses(type, EMPTY_PREDICATE_ARRAY);
    }


    /**
     * Find all super classes from the specified type, optionally filtering the results.
     * <p>
     * This method traverses up the inheritance hierarchy of the given class, collecting all its superclasses.
     * The results can be filtered using the provided {@link Predicate} filters. If no filters are provided,
     * all superclasses are returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all superclasses of String
     * List<Class<?>> superClasses = ClassUtils.findAllSuperClasses(String.class);
     * // Returns: [java.lang.Object]
     *
     * // Find superclasses with a filter
     * List<Class<?>> filtered = ClassUtils.findAllSuperClasses(ArrayList.class, cls -> !cls.equals(Object.class));
     * // Returns: [java.util.AbstractList, java.util.AbstractCollection]
     *
     * // Null input returns empty list
     * List<Class<?>> nullResult = ClassUtils.findAllSuperClasses(null);
     * // Returns: []
     * }</pre>
     *
     * @param type         the specified type to find superclasses for, may be {@code null}
     * @param classFilters optional filters to apply to the results
     * @return a non-null, read-only {@link List} of superclasses
     * @see #findTypes(Class, boolean, boolean, boolean, boolean, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> findAllSuperClasses(@Nullable Class<?> type, @Nullable Predicate<? super Class<?>>... classFilters) {
        return findTypes(type, false, true, true, false, classFilters);
    }

    /**
     * Find all interfaces from the specified type, optionally filtering the results.
     * <p>
     * This method traverses the inheritance hierarchy of the given class or interface,
     * collecting all interfaces it implements or extends. The results can be filtered
     * using the provided {@link Predicate} filters. If no filters are provided,
     * all interfaces are returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all interfaces implemented by ArrayList
     * List<Class<?>> interfaces = ClassUtils.findAllInterfaces(ArrayList.class);
     * // Returns: [java.util.List, java.util.Collection, java.lang.Iterable, java.util.RandomAccess, java.lang.Cloneable, java.io.Serializable]
     *
     * // Find interfaces with a filter
     * List<Class<?>> filtered = ClassUtils.findAllInterfaces(ArrayList.class, cls -> cls.getSimpleName().startsWith("C"));
     * // Returns: [java.util.Collection, java.lang.Cloneable]
     *
     * // Null input returns empty list
     * List<Class<?>> nullResult = ClassUtils.findAllInterfaces(null);
     * // Returns: []
     * }</pre>
     *
     * @param type             the specified type to find interfaces for, may be {@code null}
     * @param interfaceFilters optional filters to apply to the results
     * @return a non-null, read-only {@link List} of interfaces
     * @see #findTypes(Class, boolean, boolean, boolean, boolean, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> findAllInterfaces(@Nullable Class<?> type, @Nullable Predicate<? super Class<?>>... interfaceFilters) {
        return findTypes(type, false, true, false, true, interfaceFilters);
    }

    /**
     * Find all inherited classes from the specified type, optionally filtering the results.
     * <p>
     * This method traverses the inheritance hierarchy of the given class,
     * collecting all its superclasses and interfaces. The results can be filtered
     * using the provided {@link Predicate} filters. If no filters are provided,
     * all inherited classes are returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all inherited classes of ArrayList
     * List<Class<?>> inherited = ClassUtils.findAllInheritedClasses(ArrayList.class);
     * // Returns: [java.util.AbstractList, java.util.AbstractCollection, java.lang.Object,
     * //           java.util.List, java.util.Collection, java.lang.Iterable,
     * //           java.util.RandomAccess, java.lang.Cloneable, java.io.Serializable]
     *
     * // Find inherited classes with a filter
     * List<Class<?>> filtered = ClassUtils.findAllInheritedClasses(ArrayList.class,
     *         cls -> cls.getSimpleName().startsWith("A") || cls.getSimpleName().endsWith("able"));
     * // Returns: [java.util.AbstractList, java.util.AbstractCollection,
     * //           java.util.RandomAccess, java.lang.Cloneable, java.io.Serializable]
     *
     * // Null input returns empty list
     * List<Class<?>> nullResult = ClassUtils.findAllInheritedClasses(null);
     * // Returns: []
     * }</pre>
     *
     * @param type         the specified type to find inherited classes for, may be {@code null}
     * @param classFilters optional filters to apply to the results
     * @return a non-null, read-only {@link List} of inherited classes
     * @see #findTypes(Class, boolean, boolean, boolean, boolean, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> findAllInheritedClasses(@Nullable Class<?> type, @Nullable Predicate<? super Class<?>>... classFilters) {
        return findTypes(type, false, true, true, true, classFilters);
    }

    /**
     * Find all classes from the specified type, optionally filtering the results.
     * <p>
     * This method collects all classes related to the given class or interface,
     * including the class itself, its superclasses, and interfaces. The results
     * can be filtered using the provided {@link Predicate} filters. If no filters
     * are provided, all related classes are returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all classes related to ArrayList
     * List<Class<?>> allClasses = ClassUtils.findAllClasses(ArrayList.class);
     * // Returns: [java.util.ArrayList, java.util.AbstractList, java.util.AbstractCollection,
     * //           java.lang.Object, java.util.List, java.util.Collection, java.lang.Iterable,
     * //           java.util.RandomAccess, java.lang.Cloneable, java.io.Serializable]
     *
     * // Find classes with a filter
     * List<Class<?>> filtered = ClassUtils.findAllClasses(ArrayList.class,
     *         cls -> cls.getSimpleName().startsWith("A") || cls.getSimpleName().endsWith("able"));
     * // Returns: [java.util.ArrayList, java.util.AbstractList, java.util.AbstractCollection,
     * //           java.util.RandomAccess, java.lang.Cloneable, java.io.Serializable]
     *
     * // Null input returns empty list
     * List<Class<?>> nullResult = ClassUtils.findAllClasses(null);
     * // Returns: []
     * }</pre>
     *
     * @param type         the specified type to find classes for, may be {@code null}
     * @param classFilters optional filters to apply to the results
     * @return a non-null, read-only {@link List} of all classes related to the specified type
     * @see #findTypes(Class, boolean, boolean, boolean, boolean, Predicate[])
     */
    @Nonnull
    @Immutable
    public static List<Class<?>> findAllClasses(@Nullable Class<?> type, @Nullable Predicate<? super Class<?>>... classFilters) {
        return findTypes(type, true, true, true, true, classFilters);
    }

    /**
     * Finds types based on the specified criteria.
     * <p>
     * This method is a core utility for traversing class hierarchies and collecting types according to various flags.
     * It can include the original type, hierarchical types (superclasses), generic superclass, and/or interfaces.
     * The results can be filtered using the provided {@link Predicate} filters.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Find all superclasses of ArrayList
     * List<Class<?>> superClasses = ClassUtils.findTypes(ArrayList.class, false, true, true, false);
     * // Returns: [java.util.AbstractList, java.util.AbstractCollection, java.lang.Object]
     *
     * // Find all interfaces implemented by ArrayList
     * List<Class<?>> interfaces = ClassUtils.findTypes(ArrayList.class, false, true, false, true);
     * // Returns: [java.util.List, java.util.Collection, java.lang.Iterable,
     * //           java.util.RandomAccess, java.lang.Cloneable, java.io.Serializable]
     *
     * // Find the type itself and all its superclasses
     * List<Class<?>> selfAndSuper = ClassUtils.findTypes(String.class, true, true, true, false);
     * // Returns: [java.lang.String, java.lang.Object]
     *
     * // Null input returns empty list
     * List<Class<?>> nullResult = ClassUtils.findTypes(null, true, true, true, true);
     * // Returns: []
     * }</pre>
     *
     * @param type                     the specified type to find related types for, may be {@code null}
     * @param includeSelf              whether to include the type itself in the results
     * @param includeHierarchicalTypes whether to include superclasses in the results
     * @param includeGenericSuperclass whether to include the direct generic superclass in the results
     * @param includeGenericInterfaces whether to include interfaces in the results
     * @param typeFilters              optional filters to apply to the results
     * @return a non-null, read-only {@link List} of classes based on the specified criteria
     * @see TypeFinder#classFinder(Class, boolean, boolean, boolean, boolean)
     */
    @Nonnull
    @Immutable
    protected static List<Class<?>> findTypes(@Nullable Class<?> type, boolean includeSelf, boolean includeHierarchicalTypes,
                                              boolean includeGenericSuperclass, boolean includeGenericInterfaces,
                                              @Nullable Predicate<? super Class<?>>... typeFilters) {
        if (type == null || isPrimitive(type)) {
            return emptyList();
        }
        return classFinder(type, includeSelf, includeHierarchicalTypes, includeGenericSuperclass, includeGenericInterfaces).findTypes(typeFilters);
    }

    /**
     * Checks if the {@code superType} is assignable from the {@code targetType}.
     * <p>
     * This method is a null-safe variant of {@link Class#isAssignableFrom(Class)}.
     * It returns {@code false} if either of the arguments is {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isAssignableFrom(List.class, ArrayList.class);  // returns true
     * boolean result2 = ClassUtils.isAssignableFrom(String.class, Object.class);   // returns false
     * boolean result3 = ClassUtils.isAssignableFrom(null, String.class);           // returns false
     * boolean result4 = ClassUtils.isAssignableFrom(String.class, null);           // returns false
     * }</pre>
     *
     * @param superType  the super type to check against, may be {@code null}
     * @param targetType the target type to check, may be {@code null}
     * @return {@code true} if {@code superType} is assignable from {@code targetType}, {@code false} otherwise
     * @see Class#isAssignableFrom(Class)
     */
    public static boolean isAssignableFrom(@Nullable Class<?> superType, @Nullable Class<?> targetType) {
        // any argument is null
        if (superType == null || targetType == null) {
            return false;
        }
        // equals
        if (Objects.equals(superType, targetType)) {
            return true;
        }
        // isAssignableFrom
        return superType.isAssignableFrom(targetType);
    }

    /**
     * Alias of {@link #getClass(Object)}
     *
     * @see #getClass(Object)
     */
    @Nullable
    public static Class<?> getType(@Nullable Object value) {
        return getClass(value);
    }

    /**
     * Gets the {@link Class} of the given object.
     * <p>
     * This method returns the Class object that represents the runtime class of the specified object.
     * If the object is {@code null}, this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String str = "Hello";
     * Class<?> clazz = ClassUtils.getClass(str);
     * System.out.println(clazz); // prints: class java.lang.String
     *
     * Object obj = null;
     * Class<?> nullClazz = ClassUtils.getClass(obj);
     * System.out.println(nullClazz); // prints: null
     * }</pre>
     *
     * @param object the object to get the class from, may be {@code null}
     * @return the Class of the given object, or {@code null} if the object is {@code null}
     * @see Object#getClass()
     */
    @Nullable
    public static Class<?> getClass(@Nullable Object object) {
        return object == null ? null : object.getClass();
    }

    /**
     * Alias of {@link #getTypes(Object...)}
     *
     * @see #getTypes(Object...)
     */
    @Nonnull
    public static Class[] getTypes(@Nullable Object... values) {
        return getClasses(values);
    }

    /**
     * Get the {@link Class types} of the specified values
     *
     * @param values the values
     * @return If can't be resolved, return {@link ArrayUtils#EMPTY_CLASS_ARRAY empty class array}
     */
    @Nonnull
    public static Class<?>[] getClasses(@Nullable Object... values) {
        if (isEmpty(values)) {
            return EMPTY_CLASS_ARRAY;
        }
        int size = values.length;
        Class<?>[] types = new Class[size];
        for (int i = 0; i < size; i++) {
            types[i] = getClass(values[i]);
        }
        return types;
    }

    /**
     * Gets the type name of the given object.
     * <p>
     * This method returns the fully qualified name of the class of the specified object.
     * If the object is {@code null}, this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String str = "Hello";
     * String typeName = ClassUtils.getTypeName(str);
     * System.out.println(typeName); // prints: java.lang.String
     *
     * Object obj = null;
     * String nullTypeName = ClassUtils.getTypeName(obj);
     * System.out.println(nullTypeName); // prints: null
     * }</pre>
     *
     * @param value the object to get the type name from, may be {@code null}
     * @return the fully qualified name of the class of the given object, or {@code null} if the object is {@code null}
     * @see #getTypeName(Class)
     */
    @Nullable
    public static String getTypeName(@Nullable Object value) {
        return getTypeName(getClass(value));
    }

    /**
     * Gets the type name of the given class.
     * <p>
     * This method returns the fully qualified name of the specified class.
     * For array types, it returns the component type name followed by the appropriate
     * number of square brackets ("[]"). If the class is {@code null}, this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String typeName = ClassUtils.getTypeName(String.class);
     * System.out.println(typeName); // prints: java.lang.String
     *
     * String arrayTypeName = ClassUtils.getTypeName(int[].class);
     * System.out.println(arrayTypeName); // prints: int[]
     *
     * String nullTypeName = ClassUtils.getTypeName(null);
     * System.out.println(nullTypeName); // prints: null
     * }</pre>
     *
     * @param type the class to get the type name from, may be {@code null}
     * @return the fully qualified name of the given class, or {@code null} if the class is {@code null}
     * @see Class#getName()
     * @see Class#getComponentType()
     */
    @Nullable
    public static String getTypeName(@Nullable Class<?> type) {
        if (type == null) {
            return null;
        }
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                String name = getTypeName(cl);
                StringBuilder sb = new StringBuilder(name.length() + dimensions * 2);
                sb.append(name);
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
            }
        }
        return type.getName();
    }

    /**
     * Gets the simple name of the given class.
     * <p>
     * This method returns the simple name of the specified class as defined in the Java Language Specification.
     * For array types, it returns the component type's simple name followed by the appropriate number of square brackets ("[]").
     * If the class is {@code null}, this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String simpleName = ClassUtils.getSimpleName(String.class);
     * System.out.println(simpleName); // prints: String
     *
     * String arraySimpleName = ClassUtils.getSimpleName(int[].class);
     * System.out.println(arraySimpleName); // prints: int[]
     *
     * String nullSimpleName = ClassUtils.getSimpleName(null);
     * System.out.println(nullSimpleName); // prints: null
     * }</pre>
     *
     * @param type the class to get the simple name from, may be {@code null}
     * @return the simple name of the given class, or {@code null} if the class is {@code null}
     * @see Class#getSimpleName()
     * @see #getSimpleName(Class, boolean)
     */
    @Nullable
    public static String getSimpleName(@Nullable Class<?> type) {
        if (type == null) {
            return null;
        }
        boolean array = type.isArray();
        return getSimpleName(type, array);
    }

    private static String getSimpleName(Class<?> type, boolean array) {
        if (array) {
            return getSimpleName(type.getComponentType()) + "[]";
        }
        String simpleName = type.getName();
        Class<?> enclosingClass = type.getEnclosingClass();
        if (enclosingClass == null) { // top level class
            simpleName = simpleName.substring(simpleName.lastIndexOf(DOT_CHAR) + 1);
        } else {
            String ecName = enclosingClass.getName();
            simpleName = simpleName.substring(ecName.length());
            // Remove leading "\$[0-9]*" from the name
            int length = simpleName.length();
            if (length < 1 || simpleName.charAt(0) != DOLLAR_CHAR) throw new InternalError("Malformed class name");
            int index = 1;
            while (index < length && isAsciiDigit(simpleName.charAt(index))) index++;
            // Eventually, this is the empty string iff this is an anonymous class
            return simpleName.substring(index);
        }
        return simpleName;
    }

    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }

    /**
     * Checks if the {@code targetType} is derived from any of the specified {@code superTypes}.
     * <p>
     * This method returns {@code true} if the {@code targetType} is assignable from any of the
     * {@code superTypes}, meaning that {@code targetType} is the same as, or is a subclass or
     * subinterface of, any of the {@code superTypes}. It returns {@code false} if {@code targetType}
     * is {@code null}, if {@code superTypes} is empty, or if none of the {@code superTypes} are
     * assignable from {@code targetType}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean result1 = ClassUtils.isDerived(ArrayList.class, List.class, Collection.class);
     * // returns true because ArrayList implements List and Collection
     *
     * boolean result2 = ClassUtils.isDerived(String.class, Number.class);
     * // returns false because String does not implement or extend Number
     *
     * boolean result3 = ClassUtils.isDerived(null, Object.class);
     * // returns false because targetType is null
     *
     * boolean result4 = ClassUtils.isDerived(String.class);
     * // returns false because superTypes is empty
     * }</pre>
     *
     * @param targetType the target type to check, may be {@code null}
     * @param superTypes the super types to check against, may be {@code null} or empty
     * @return {@code true} if {@code targetType} is derived from any of the {@code superTypes},
     * {@code false} otherwise
     * @see #isAssignableFrom(Class, Class)
     */
    public static boolean isDerived(@Nullable Class<?> targetType, @Nullable Class<?>... superTypes) {
        if (targetType == null) { // any argument is null
            return false;
        }
        int length = length(superTypes);
        if (length == 0) { // superTypes is empty
            return false;
        }
        boolean derived = false;
        for (int i = 0; i < length; i++) {
            Class<?> superType = superTypes[i];
            if (isAssignableFrom(superType, targetType)) {
                derived = true;
                break;
            }
        }
        return derived;
    }

    /**
     * Creates a new instance of the specified class by finding a suitable constructor
     * that matches the provided arguments and invoking it.
     *
     * <p>This method searches for a declared constructor in the given class that
     * matches the number and types of the provided arguments. It handles primitive
     * types by resolving their wrapper types for comparison. If no matching constructor
     * is found, an {@link IllegalArgumentException} is thrown. If a matching constructor
     * is found, it is invoked using {@link ConstructorUtils#newInstance(Constructor, Object...)}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Creating an instance of a class with a parameterized constructor
     * MyClass instance = ClassUtils.newInstance(MyClass.class, "example", 42);
     *
     * // Creating an instance of a class with a no-arg constructor
     * MyClass instance2 = ClassUtils.newInstance(MyClass.class);
     *
     * // This will throw an IllegalArgumentException if no matching constructor exists
     * try {
     *     ClassUtils.newInstance(MyClass.class, "invalid", "arguments");
     * } catch (IllegalArgumentException e) {
     *     System.out.println("No matching constructor found: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param type the class of which to create a new instance
     * @param args the arguments to pass to the constructor
     * @param <T>  the type of the object to create
     * @return a new instance of the specified class
     * @throws IllegalArgumentException if no constructor matching the provided arguments is found
     * @see ConstructorUtils#newInstance(Constructor, Object...)
     */
    @Nonnull
    public static <T> T newInstance(@Nonnull Class<T> type, Object... args) {
        int length = length(args);

        List<Constructor<?>> constructors = findDeclaredConstructors(type, constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (length != parameterTypes.length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                Object arg = args[i];
                Class<?> parameterType = parameterTypes[i];
                if (isPrimitive(parameterType)) {
                    parameterType = resolveWrapperType(parameterType);
                }
                if (!parameterType.isInstance(arg)) {
                    return false;
                }
            }
            return true;
        });

        if (constructors.isEmpty()) {
            String message = format("No constructor[class : '{}'] matches the arguments : {}", getTypeName(type), arrayToString(args));
            throw new IllegalArgumentException(message);
        }

        Constructor<T> constructor = (Constructor<T>) constructors.get(0);
        return ConstructorUtils.newInstance(constructor, args);
    }

    /**
     * Gets the top-level component type of the given array object.
     * <p>
     * This method recursively retrieves the component type of an array until it reaches
     * the base (non-array) type. For example, for a 2D array {@code int[][]}, this method
     * will return {@code int.class}. If the input is not an array or is {@code null},
     * this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int[] array1D = {1, 2, 3};
     * Class<?> componentType1 = ClassUtils.getTopComponentType(array1D);
     * // Returns: int.class
     *
     * int[][] array2D = {{1, 2}, {3, 4}};
     * Class<?> componentType2 = ClassUtils.getTopComponentType(array2D);
     * // Returns: int.class
     *
     * String[] stringArray = {"a", "b"};
     * Class<?> componentType3 = ClassUtils.getTopComponentType(stringArray);
     * // Returns: String.class
     *
     * Object nonArray = "not an array";
     * Class<?> componentType4 = ClassUtils.getTopComponentType(nonArray);
     * // Returns: null
     *
     * Class<?> componentType5 = ClassUtils.getTopComponentType(null);
     * // Returns: null
     * }</pre>
     *
     * @param array the array object to get the top-level component type from, may be {@code null}
     * @return the top-level component type of the array, or {@code null} if the input is not an array or is {@code null}
     * @see #getTopComponentType(Class)
     */
    @Nullable
    public static Class<?> getTopComponentType(@Nullable Object array) {
        return getTopComponentType(getClass(array));
    }

    /**
     * Gets the top-level component type of the given array type.
     * <p>
     * This method recursively retrieves the component type of an array until it reaches
     * the base (non-array) type. For example, for a 2D array {@code int[][]}, this method
     * will return {@code int.class}. If the input is not an array or is {@code null},
     * this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> componentType1 = ClassUtils.getTopComponentType(int[].class);
     * // Returns: int.class
     *
     * Class<?> componentType2 = ClassUtils.getTopComponentType(int[][].class);
     * // Returns: int.class
     *
     * Class<?> componentType3 = ClassUtils.getTopComponentType(String[].class);
     * // Returns: String.class
     *
     * Class<?> componentType4 = ClassUtils.getTopComponentType(String.class);
     * // Returns: null
     *
     * Class<?> componentType5 = ClassUtils.getTopComponentType(null);
     * // Returns: null
     * }</pre>
     *
     * @param arrayType the array type to get the top-level component type from, may be {@code null}
     * @return the top-level component type of the array, or {@code null} if the input is not an array or is {@code null}
     * @see #isArray(Class)
     * @see Class#getComponentType()
     */
    @Nullable
    public static Class<?> getTopComponentType(@Nullable Class<?> arrayType) {
        if (!isArray(arrayType)) {
            return null;
        }
        Class<?> targetType = null;
        Class<?> componentType = arrayType.getComponentType();

        while (componentType != null) {
            targetType = componentType;
            componentType = getTopComponentType(componentType);
        }

        return targetType;
    }

    /**
     * Casts the given object to the specified type if possible.
     * <p>
     * This method checks if the provided object is an instance of the target cast type.
     * If it is, the object is cast to the target type and returned. Otherwise, {@code null} is returned.
     * This method is null-safe and will return {@code null} if either the object or the cast type is {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String str = "Hello";
     * CharSequence charSeq = ClassUtils.cast(str, CharSequence.class);
     * // Returns: "Hello" as CharSequence
     *
     * Integer num = 42;
     * String strNum = ClassUtils.cast(num, String.class);
     * // Returns: null (Integer is not an instance of String)
     *
     * Object nullObj = null;
     * String nullResult = ClassUtils.cast(nullObj, String.class);
     * // Returns: null (object is null)
     *
     * String str2 = "World";
     * String result = ClassUtils.cast(str2, null);
     * // Returns: null (castType is null)
     * }</pre>
     *
     * @param object   the object to cast, may be {@code null}
     * @param castType the type to cast the object to, may be {@code null}
     * @param <T>      the type to cast to
     * @return the casted object if it is an instance of the cast type, or {@code null} if the object
     * is not an instance of the cast type, or if either the object or cast type is {@code null}
     * @see Class#isInstance(Object)
     * @see Class#cast(Object)
     */
    @Nullable
    public static <T> T cast(@Nullable Object object, @Nullable Class<T> castType) {
        if (object == null || castType == null) {
            return null;
        }
        return castType.isInstance(object) ? castType.cast(object) : null;
    }

    private ClassUtils() {
    }

}
