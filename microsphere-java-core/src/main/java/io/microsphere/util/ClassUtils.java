/**
 *
 */
package io.microsphere.util;

import io.microsphere.filter.ClassFileJarEntryFilter;
import io.microsphere.io.filter.FileExtensionFilter;
import io.microsphere.io.scanner.SimpleFileScanner;
import io.microsphere.io.scanner.SimpleJarEntryScanner;
import io.microsphere.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.constants.FileConstants.CLASS;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.constants.FileConstants.JAR_EXTENSION;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.microsphere.constants.SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.io.FileUtils.resolveRelativePath;
import static io.microsphere.lang.function.Predicates.EMPTY_PREDICATE_ARRAY;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.resolveProtocol;
import static io.microsphere.reflect.ConstructorUtils.findDeclaredConstructors;
import static io.microsphere.reflect.Modifier.ANNOTATION;
import static io.microsphere.reflect.Modifier.ENUM;
import static io.microsphere.reflect.Modifier.SYNTHETIC;
import static io.microsphere.reflect.TypeFinder.classFinder;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.StringUtils.isNotBlank;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.startsWith;
import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.StringUtils.substringBefore;
import static io.microsphere.util.StringUtils.substringBeforeLast;
import static io.microsphere.util.StringUtils.substringBetween;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * {@link Class} utility class
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see ClassUtils
 * @since 1.0.0
 */
public abstract class ClassUtils extends BaseUtils {

    private final static Logger logger = getLogger(ClassUtils.class);

    /**
     * Suffix for array class names: "[]"
     */
    public static final String ARRAY_SUFFIX = "[]";

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
    public static final Set<Class<?>> SIMPLE_TYPES = ofSet(
            Void.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            BigDecimal.class,
            BigInteger.class,
            Date.class,
            Object.class);

    public static final Set<Class<?>> PRIMITIVE_TYPES = ofSet(
            Void.TYPE,
            Boolean.TYPE,
            Character.TYPE,
            Byte.TYPE,
            Short.TYPE,
            Integer.TYPE,
            Long.TYPE,
            Float.TYPE,
            Double.TYPE
    );

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
     * A map with primitive type name as key and corresponding primitive type as
     * value, for example: "int" -> "int.class".
     */
    private static final Map<String, Class<?>> PRIMITIVE_TYPE_NAME_MAP;

    /**
     * A map with primitive wrapper type as key and corresponding primitive type
     * as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP;

    /**
     * A map with primitive type as key and its wrapper type
     * as value, for example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_TYPE_MAP;

    static final Map<Class<?>, Boolean> concreteClassCache = synchronizedMap(new WeakHashMap<>());

    private static final FileExtensionFilter JAR_FILE_EXTENSION_FILTER = FileExtensionFilter.of(JAR_EXTENSION);

    static {
        PRIMITIVE_WRAPPER_TYPE_MAP = ofMap(
                Void.class, Void.TYPE,
                Boolean.class, Boolean.TYPE,
                Byte.class, Byte.TYPE,
                Character.class, Character.TYPE,
                Short.class, Short.TYPE,
                Integer.class, Integer.TYPE,
                Long.class, Long.TYPE,
                Float.class, Float.TYPE,
                Double.class, Double.TYPE
        );
    }

    static {
        WRAPPER_PRIMITIVE_TYPE_MAP = ofMap(
                Void.TYPE, Void.class,
                Boolean.TYPE, Boolean.class,
                Byte.TYPE, Byte.class,
                Character.TYPE, Character.class,
                Short.TYPE, Short.class,
                Boolean.TYPE, Boolean.class,
                Integer.TYPE, Integer.class,
                Long.TYPE, Long.class,
                Float.TYPE, Float.class,
                Double.TYPE, Double.class
        );
    }

    static {
        Map<String, Class<?>> primitiveTypeNameMap = newFixedHashMap(17);

        PRIMITIVE_TYPES.forEach(type -> {
            primitiveTypeNameMap.put(type.getName(), type);
        });

        PRIMITIVE_ARRAY_TYPES.forEach(type -> {
            primitiveTypeNameMap.put(type.getName(), type);
        });

        PRIMITIVE_TYPE_NAME_MAP = unmodifiableMap(primitiveTypeNameMap);
    }

    /**
     * The specified type is array or not?*
     *
     * @param type the type to test
     * @return <code>true</code> if the specified type is an array class,
     * <code>false</code> otherwise
     * @see Class#isArray()
     */
    public static boolean isArray(Class<?> type) {
        return type != null && type.isArray();
    }

    /**
     * Is the specified type a concrete class or not?
     *
     * @param type type to check
     * @return <code>true</code> if concrete class, <code>false</code> otherwise.
     */
    public static boolean isConcreteClass(Class<?> type) {

        if (type == null) {
            return false;
        }

        if (concreteClassCache.containsKey(type)) {
            return true;
        }

        if (isGeneralClass(type, Boolean.FALSE)) {
            concreteClassCache.put(type, Boolean.TRUE);
            return true;
        }

        return false;
    }

    /**
     * Is the specified type a abstract class or not?
     * <p>
     *
     * @param type the type
     * @return true if type is a abstract class, false otherwise.
     */
    public static boolean isAbstractClass(Class<?> type) {
        return isGeneralClass(type, Boolean.TRUE);
    }

    /**
     * Is the specified type a general class or not?
     * <p>
     *
     * @param type the type
     * @return true if type is a general class, false otherwise.
     */
    public static boolean isGeneralClass(Class<?> type) {
        return isGeneralClass(type, Boolean.FALSE);
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
    protected static boolean isGeneralClass(Class<?> type, Boolean isAbstract) {

        if (type == null) {
            return false;
        }

        int mod = type.getModifiers();

        if (isAnnotation(mod)
                || isEnum(mod)
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

    public static boolean isTopLevelClass(Class<?> type) {
        return type != null && !type.isLocalClass() && !type.isMemberClass();
    }


    /**
     * The specified type is primitive type or simple type
     * <p>
     * It's an optimized implementation for {@link Class#isPrimitive()}.
     *
     * @param type the type to test
     * @return
     * @see Class#isPrimitive()
     */
    public static boolean isPrimitive(Class<?> type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    public static boolean isFinal(Class<?> type) {
        return type != null && Modifier.isFinal(type.getModifiers());
    }

    /**
     * The specified type is simple type or not
     *
     * @param type the type to test
     * @return if <code>type</code> is one element of {@link #SIMPLE_TYPES}, return <code>true</code>, or <code>false</code>
     * @see #SIMPLE_TYPES
     */
    public static boolean isSimpleType(Class<?> type) {
        return SIMPLE_TYPES.contains(type);
    }

    /**
     * Resolve the primitive class from the specified type
     *
     * @param type the specified type
     * @return <code>null</code> if not found
     */
    public static Class<?> resolvePrimitiveType(Class<?> type) {
        if (isPrimitive(type)) {
            return type;
        }
        return PRIMITIVE_WRAPPER_TYPE_MAP.get(type);
    }

    /**
     * Resolve the wrapper class from the primitive type
     *
     * @param primitiveType the primitive type
     * @return <code>null</code> if not found
     */
    public static Class<?> resolveWrapperType(Class<?> primitiveType) {
        if (PRIMITIVE_WRAPPER_TYPE_MAP.containsKey(primitiveType)) {
            return primitiveType;
        }
        return WRAPPER_PRIMITIVE_TYPE_MAP.get(primitiveType);
    }

    public static boolean isWrapperType(Class<?> type) {
        return PRIMITIVE_WRAPPER_TYPE_MAP.containsKey(type);
    }

    public static boolean arrayTypeEquals(Class<?> oneArrayType, Class<?> anotherArrayType) {
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
     * Resolve the given class name as primitive class, if appropriate,
     * according to the JVM's naming rules for primitive classes.
     * <p>
     * Also supports the JVM's internal class names for primitive arrays. Does
     * <i>not</i> support the "[]" suffix notation for primitive arrays; this is
     * only supported by {@link #forName}.
     *
     * @param name the name of the potentially primitive class
     * @return the primitive class, or <code>null</code> if the name does not
     * denote a primitive class or primitive array class
     */
    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 8) {
            // Could be a primitive - likely.
            result = PRIMITIVE_TYPE_NAME_MAP.get(name);
        }
        return result;
    }

    /**
     * Resolve package name under specified class
     *
     * @param targetClass target class
     * @return package name
     */
    @Nullable
    public static String resolvePackageName(Class<?> targetClass) {
        return isPrimitive(targetClass) ? null : resolvePackageName(getTypeName(targetClass));
    }

    /**
     * Resolve package name under specified class name
     *
     * @param className class name
     * @return package name
     */
    @Nullable
    public static String resolvePackageName(String className) {
        return substringBeforeLast(className, ".");
    }

    /**
     * Find all class names in class path
     *
     * @param classPath class path
     * @param recursive is recursive on sub directories
     * @return all class names in class path
     */
    @Nonnull
    public static Set<String> findClassNamesInClassPath(String classPath, boolean recursive) {
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
     * Find all class names in class path
     *
     * @param classPath JarFile or class patch directory
     * @param recursive is recursive on sub directories
     * @return all class names in class path
     */
    public static Set<String> findClassNamesInClassPath(File classPath, boolean recursive) {
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
     * Find all class names in directory
     *
     * @param classesDirectory a directory to be found
     * @param recursive        is recursive on sub directories
     * @return all class names in directory
     */
    public static Set<String> findClassNamesInDirectory(File classesDirectory, boolean recursive) {
        if (classesDirectory == null || !classesDirectory.exists()) {
            return emptySet();
        }

        Set<File> classFiles = SimpleFileScanner.INSTANCE.scan(classesDirectory, recursive, FileExtensionFilter.of(CLASS));
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
     * Find all class names in jar file
     *
     * @param jarFile   jar file
     * @param recursive is recursive on sub directories
     * @return all class names in jar file
     */
    public static Set<String> findClassNamesInJarFile(File jarFile, boolean recursive) {
        if (jarFile == null || !jarFile.exists()) {
            return emptySet();
        }
        Set<String> classNames;
        try {
            JarFile jarFile_ = new JarFile(jarFile);
            Set<JarEntry> jarEntries = SimpleJarEntryScanner.INSTANCE.scan(jarFile_, recursive, ClassFileJarEntryFilter.INSTANCE);
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

    protected static String resolveClassName(File classesDirectory, File classFile) {
        String classFileRelativePath = resolveRelativePath(classesDirectory, classFile);
        return resolveClassName(classFileRelativePath);
    }

    /**
     * Resolve resource name to class name
     *
     * @param resourceName resource name
     * @return class name
     */
    public static String resolveClassName(String resourceName) {
        String className = replace(resourceName, SLASH, DOT);
        className = substringBefore(className, CLASS_EXTENSION);
        while (startsWith(className, DOT)) {
            className = substringAfter(className, DOT);
        }
        return className;
    }

    /**
     * Get all super classes from the specified type
     *
     * @param type the specified type
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> getAllSuperClasses(Class<?> type) {
        return findAllSuperClasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all interfaces from the specified type
     *
     * @param type the specified type
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> getAllInterfaces(Class<?> type) {
        return findAllInterfaces(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all inherited types from the specified type
     *
     * @param type the specified type
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> getAllInheritedTypes(Class<?> type) {
        return getAllInheritedClasses(type);
    }

    /**
     * Get all inherited classes from the specified type
     *
     * @param type the specified type
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> getAllInheritedClasses(Class<?> type) {
        return findAllInheritedClasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Get all classes from the specified type
     *
     * @param type the specified type
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> getAllClasses(Class<?> type) {
        return findAllClasses(type, EMPTY_PREDICATE_ARRAY);
    }

    /**
     * Find all super classes from the specified type
     *
     * @param type         the specified type
     * @param classFilters the filters for classes
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> findAllSuperClasses(Class<?> type, Predicate<? super Class<?>>... classFilters) {
        return findTypes(type, false, true, true, false, classFilters);
    }

    /**
     * find all interfaces from the specified type
     *
     * @param type             the specified type
     * @param interfaceFilters the filters for interfaces
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> findAllInterfaces(Class<?> type, Predicate<? super Class<?>>... interfaceFilters) {
        return findTypes(type, false, true, false, true, interfaceFilters);
    }

    /**
     * Find all inherited classes from the specified type
     *
     * @param type         the specified type
     * @param classFilters the filters for types
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> findAllInheritedClasses(Class<?> type, Predicate<? super Class<?>>... classFilters) {
        return findTypes(type, false, true, true, true, classFilters);
    }

    /**
     * Find all classes from the specified type with filters
     *
     * @param type         the specified type
     * @param classFilters class filters
     * @return non-null read-only {@link List}
     */
    public static List<Class<?>> findAllClasses(Class<?> type, Predicate<? super Class<?>>... classFilters) {
        return findTypes(type, true, true, true, true, classFilters);
    }

    protected static List<Class<?>> findTypes(Class<?> type, boolean includeSelf, boolean includeHierarchicalTypes,
                                              boolean includeGenericSuperclass, boolean includeGenericInterfaces,
                                              Predicate<? super Class<?>>... typeFilters) {
        if (type == null || isPrimitive(type)) {
            return emptyList();
        }
        return classFinder(type, includeSelf, includeHierarchicalTypes, includeGenericSuperclass, includeGenericInterfaces).findTypes(typeFilters);
    }

    /**
     * the semantics is same as {@link Class#isAssignableFrom(Class)}
     *
     * @param superType  the super type
     * @param targetType the target type
     * @return see {@link Class#isAssignableFrom(Class)}
     */
    public static boolean isAssignableFrom(Class<?> superType, Class<?> targetType) {
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
     * Resolve the types of the specified values
     *
     * @param values the values
     * @return If can't be resolved, return {@link ArrayUtils#EMPTY_CLASS_ARRAY empty class array}
     */
    public static Class[] getTypes(Object... values) {
        return resolveTypes(values);
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
     * Get the name of the specified type
     *
     * @param type the specified type
     * @return non-null
     */
    public static String getTypeName(Class<?> type) {
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
     * Get the simple name of the specified type
     *
     * @param type the specified type
     * @return non-null
     */
    public static String getSimpleName(Class<?> type) {
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
            simpleName = simpleName.substring(simpleName.lastIndexOf(".") + 1);
        } else {
            String ecName = enclosingClass.getName();
            simpleName = simpleName.substring(ecName.length());
            // Remove leading "\$[0-9]*" from the name
            int length = simpleName.length();
            if (length < 1 || simpleName.charAt(0) != '$') throw new InternalError("Malformed class name");
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
     * the semantics is same as {@link Class#isAssignableFrom(Class)}
     *
     * @param targetType the target type
     * @param superTypes the super types
     * @return see {@link Class#isAssignableFrom(Class)}
     */
    public static boolean isDerived(Class<?> targetType, Class<?>... superTypes) {
        // any argument is null
        if (superTypes == null || superTypes.length == 0 || targetType == null) {
            return false;
        }
        boolean derived = false;
        for (Class<?> superType : superTypes) {
            if (isAssignableFrom(superType, targetType)) {
                derived = true;
                break;
            }
        }
        return derived;
    }

    public static <T> T newInstance(Class<T> type, Object... args) {
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
        return execute(() -> constructor.newInstance(args));
    }

    public static Class<?> getTopComponentType(Object array) {
        return array == null ? null : getTopComponentType(array.getClass());
    }

    public static Class<?> getTopComponentType(Class<?> arrayType) {
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
     * Cast the given object to the specified type
     *
     * @param object   the object
     * @param castType the type to cast
     * @param <T>      the type to cast
     * @return the casted instance if and only if <code>object</code> is an instance of <code>castType</code> ,
     * <code>null</code> otherwise
     */
    public static <T> T cast(Object object, Class<T> castType) {
        if (object == null || castType == null) {
            return null;
        }
        return castType.isInstance(object) ? castType.cast(object) : null;
    }

    /**
     * @param modifiers {@link Class#getModifiers()}
     * @return true if this class's modifiers represents an annotation type; false otherwise
     * @see Class#isAnnotation()
     * @see io.microsphere.reflect.Modifier#ANNOTATION
     */
    static boolean isAnnotation(int modifiers) {
        return ANNOTATION.matches(modifiers);
    }

    /**
     * @param modifiers {@link Class#getModifiers()}
     * @return true if this class's modifiers represents an enumeration type; false otherwise
     * @see Class#isEnum()
     * @see io.microsphere.reflect.Modifier#ENUM
     */
    static boolean isEnum(int modifiers) {
        return ENUM.matches(modifiers);
    }

    /**
     * @param modifiers {@link Class#getModifiers()}
     * @return true if this class's modifiers represents a synthetic type; false otherwise
     * @see Class#isSynthetic()
     * @see io.microsphere.reflect.Modifier#SYNTHETIC
     */
    static boolean isSynthetic(int modifiers) {
        return SYNTHETIC.matches(modifiers);
    }
}
