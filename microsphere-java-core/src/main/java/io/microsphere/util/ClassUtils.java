/**
 *
 */
package io.microsphere.util;

import io.microsphere.collection.CollectionUtils;
import io.microsphere.collection.MapUtils;
import io.microsphere.constants.Constants;
import io.microsphere.constants.FileConstants;
import io.microsphere.constants.PathConstants;
import io.microsphere.filter.ClassFileJarEntryFilter;
import io.microsphere.io.FileUtils;
import io.microsphere.io.scanner.SimpleFileScanner;
import io.microsphere.io.scanner.SimpleJarEntryScanner;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.collection.SetUtils.asSet;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.constants.FileConstants.CLASS;
import static io.microsphere.constants.FileConstants.JAR;
import static io.microsphere.lang.function.Streams.filterAll;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.ConstructorUtils.getDeclaredConstructors;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.length;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * {@link Class} utility class
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassUtils
 * @since 1.0.0
 */
public abstract class ClassUtils extends BaseUtils {

    /**
     * Suffix for array class names: "[]"
     */
    public static final String ARRAY_SUFFIX = "[]";

    /**
     * @see {@link Class#ANNOTATION}
     */
    private static final int ANNOTATION = 0x00002000;

    /**
     * @see {@link Class#ENUM}
     */
    private static final int ENUM = 0x00004000;

    /**
     * @see {@link Class#SYNTHETIC}
     */
    private static final int SYNTHETIC = 0x00001000;

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
    public static final Set<Class<?>> SIMPLE_TYPES = of(
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

    public static final Set<Class<?>> PRIMITIVE_TYPES = of(
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

    /**
     * Prefix for internal array class names: "[L"
     */
    private static final String INTERNAL_ARRAY_PREFIX = "[L";

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

    private static final Map<String, Set<String>> classPathToClassNamesMap = initClassPathToClassNamesMap();

    private static final Map<String, String> classNameToClassPathsMap = initClassNameToClassPathsMap();

    private static final Map<String, Set<String>> packageNameToClassNamesMap = initPackageNameToClassNamesMap();

    static {
        PRIMITIVE_WRAPPER_TYPE_MAP = MapUtils.of(
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
        WRAPPER_PRIMITIVE_TYPE_MAP = MapUtils.of(
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
        Map<String, Class<?>> typeNamesMap = new HashMap<>(16);
        List<Class<?>> primitiveTypeNames = new ArrayList<>(16);
        primitiveTypeNames.addAll(asList(boolean.class, byte.class, char.class, double.class,
                float.class, int.class, long.class, short.class));
        primitiveTypeNames.addAll(asList(boolean[].class, byte[].class, char[].class, double[].class,
                float[].class, int[].class, long[].class, short[].class));
        for (Class<?> primitiveTypeName : primitiveTypeNames) {
            typeNamesMap.put(primitiveTypeName.getName(), primitiveTypeName);
        }
        PRIMITIVE_TYPE_NAME_MAP = unmodifiableMap(typeNamesMap);
    }

    private ClassUtils() {

    }

    /**
     * The specified type is array or not?
     * <p>
     * It's an optimized alternative for {@link Class#isArray()}).
     *
     * @param type the type to test
     * @return <code>true</code> if the specified type is an array class,
     * <code>false</code> otherwise
     * @see Class#isArray()
     */
    public static boolean isArray(Class<?> type) {
        return type != null && type.getName().startsWith("[");
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
        return isGeneralClass(type, null);
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

        if (isInterface(mod)
                || isAnnotation(mod)
                || isEnum(mod)
                || isSynthetic(mod)
                || type.isPrimitive()
                || type.isArray()) {
            return false;
        }

        if (isAbstract != null) {
            return isAbstract(mod) == isAbstract.booleanValue();
        }

        return true;
    }

    public static boolean isTopLevelClass(Class<?> type) {
        if (type == null) {
            return false;
        }

        return !type.isLocalClass() && !type.isMemberClass();
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

    public static Object convertPrimitive(Class<?> type, String value) {
        if (value == null) {
            return null;
        } else if (type == char.class || type == Character.class) {
            return value.length() > 0 ? value.charAt(0) : '\0';
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        try {
            if (type == byte.class || type == Byte.class) {
                return Byte.valueOf(value);
            } else if (type == short.class || type == Short.class) {
                return Short.valueOf(value);
            } else if (type == int.class || type == Integer.class) {
                return Integer.valueOf(value);
            } else if (type == long.class || type == Long.class) {
                return Long.valueOf(value);
            } else if (type == float.class || type == Float.class) {
                return Float.valueOf(value);
            } else if (type == double.class || type == Double.class) {
                return Double.valueOf(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return value;
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
        return WRAPPER_PRIMITIVE_TYPE_MAP.containsKey(type);
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
     * @param modifiers {@link Class#getModifiers()}
     * @return true if this class's modifiers represents an annotation type; false otherwise
     * @see Class#isAnnotation()
     */
    public static boolean isAnnotation(int modifiers) {
        return (modifiers & ANNOTATION) != 0;
    }

    /**
     * @param modifiers {@link Class#getModifiers()}
     * @return true if this class's modifiers represents an enumeration type; false otherwise
     * @see Class#isEnum()
     */
    public static boolean isEnum(int modifiers) {
        return (modifiers & ENUM) != 0;
    }

    /**
     * @param modifiers {@link Class#getModifiers()}
     * @return true if this class's modifiers represents a synthetic type; false otherwise
     * @see Class#isSynthetic()
     */
    public static boolean isSynthetic(int modifiers) {
        return (modifiers & SYNTHETIC) != 0;
    }


    private static Map<String, Set<String>> initClassPathToClassNamesMap() {
        Map<String, Set<String>> classPathToClassNamesMap = new LinkedHashMap<>();
        Set<String> classPaths = new LinkedHashSet<>();
        classPaths.addAll(ClassPathUtils.getBootstrapClassPaths());
        classPaths.addAll(ClassPathUtils.getClassPaths());
        for (String classPath : classPaths) {
            Set<String> classNames = findClassNamesInClassPath(classPath, true);
            classPathToClassNamesMap.put(classPath, classNames);
        }
        return Collections.unmodifiableMap(classPathToClassNamesMap);
    }

    private static Map<String, String> initClassNameToClassPathsMap() {
        Map<String, String> classNameToClassPathsMap = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : classPathToClassNamesMap.entrySet()) {
            String classPath = entry.getKey();
            Set<String> classNames = entry.getValue();
            for (String className : classNames) {
                classNameToClassPathsMap.put(className, classPath);
            }
        }

        return Collections.unmodifiableMap(classNameToClassPathsMap);
    }

    private static Map<String, Set<String>> initPackageNameToClassNamesMap() {
        Map<String, Set<String>> packageNameToClassNamesMap = new LinkedHashMap();
        for (Map.Entry<String, String> entry : classNameToClassPathsMap.entrySet()) {
            String className = entry.getKey();
            String packageName = resolvePackageName(className);
            Set<String> classNamesInPackage = packageNameToClassNamesMap.get(packageName);
            if (classNamesInPackage == null) {
                classNamesInPackage = new LinkedHashSet();
                packageNameToClassNamesMap.put(packageName, classNamesInPackage);
            }
            classNamesInPackage.add(className);
        }

        return Collections.unmodifiableMap(packageNameToClassNamesMap);
    }

    /**
     * Get all package names in {@link ClassPathUtils#getClassPaths() class paths}
     *
     * @return all package names in class paths
     */
    @Nonnull
    public static Set<String> getAllPackageNamesInClassPaths() {
        return packageNameToClassNamesMap.keySet();
    }

    /**
     * Resolve package name under specified class name
     *
     * @param className class name
     * @return package name
     */
    @Nullable
    public static String resolvePackageName(String className) {
        return StringUtils.substringBeforeLast(className, ".");
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
        File classesFileHolder = new File(classPath); // JarFile or Directory
        if (classesFileHolder.isDirectory()) { //Directory
            return findClassNamesInDirectory(classesFileHolder, recursive);
        } else if (classesFileHolder.isFile() && classPath.endsWith(FileConstants.JAR_EXTENSION)) { //JarFile
            return findClassNamesInJarFile(classesFileHolder, recursive);
        }
        return Collections.emptySet();
    }

    /**
     * Find all class names in class path
     *
     * @param archiveFile JarFile or class patch directory
     * @param recursive   is recursive on sub directories
     * @return all class names in class path
     */
    public static Set<String> findClassNamesInClassPath(File archiveFile, boolean recursive) {
        if (archiveFile == null || !archiveFile.exists()) {
            return emptySet();
        }
        if (archiveFile.isDirectory()) { // Directory
            return findClassNamesInArchiveDirectory(archiveFile, recursive);
        } else if (archiveFile.isFile() && archiveFile.getName().endsWith(JAR)) { //JarFile
            return findClassNamesInArchiveFile(archiveFile, recursive);
        }
        return emptySet();
    }

    protected static Set<String> findClassNamesInArchiveDirectory(File classesDirectory, boolean recursive) {
        Set<String> classNames = new LinkedHashSet<>();
        SimpleFileScanner simpleFileScanner = SimpleFileScanner.INSTANCE;
        Set<File> classFiles = simpleFileScanner.scan(classesDirectory, recursive, new SuffixFileFilter(CLASS));
        for (File classFile : classFiles) {
            String className = resolveClassName(classesDirectory, classFile);
            classNames.add(className);
        }
        return classNames;
    }

    protected static Set<String> findClassNamesInArchiveFile(File jarFile, boolean recursive) {
        Set<String> classNames = new LinkedHashSet<>();
        SimpleJarEntryScanner simpleJarEntryScanner = SimpleJarEntryScanner.INSTANCE;
        try {
            JarFile jarFile_ = new JarFile(jarFile);
            Set<JarEntry> jarEntries = simpleJarEntryScanner.scan(jarFile_, recursive, ClassFileJarEntryFilter.INSTANCE);
            for (JarEntry jarEntry : jarEntries) {
                String jarEntryName = jarEntry.getName();
                String className = resolveClassName(jarEntryName);
                if (StringUtils.isNotBlank(className)) {
                    classNames.add(className);
                }
            }
        } catch (Exception ignored) {
        }
        return classNames;
    }

    /**
     * Find class path under specified class name
     *
     * @param type class
     * @return class path
     */
    @Nullable
    public static String findClassPath(Class<?> type) {
        return findClassPath(type.getName());
    }

    /**
     * Find class path under specified class name
     *
     * @param className class name
     * @return class path
     */
    @Nullable
    public static String findClassPath(String className) {
        return classNameToClassPathsMap.get(className);
    }

    /**
     * Gets class name {@link Set} under specified class path
     *
     * @param classPath class path
     * @param recursive is recursive on sub directories
     * @return non-null {@link Set}
     */
    @Nonnull
    public static Set<String> getClassNamesInClassPath(String classPath, boolean recursive) {
        Set<String> classNames = classPathToClassNamesMap.get(classPath);
        if (CollectionUtils.isEmpty(classNames)) {
            classNames = findClassNamesInClassPath(classPath, recursive);
        }
        return classNames;
    }

    /**
     * Gets class name {@link Set} under specified package
     *
     * @param onePackage one package
     * @return non-null {@link Set}
     */
    @Nonnull
    public static Set<String> getClassNamesInPackage(Package onePackage) {
        return getClassNamesInPackage(onePackage.getName());
    }

    /**
     * Gets class name {@link Set} under specified package name
     *
     * @param packageName package name
     * @return non-null {@link Set}
     */
    @Nonnull
    public static Set<String> getClassNamesInPackage(String packageName) {
        Set<String> classNames = packageNameToClassNamesMap.get(packageName);
        return classNames == null ? Collections.emptySet() : classNames;
    }


    protected static Set<String> findClassNamesInDirectory(File classesDirectory, boolean recursive) {
        Set<String> classNames = new LinkedHashSet();
        SimpleFileScanner simpleFileScanner = SimpleFileScanner.INSTANCE;
        Set<File> classFiles = simpleFileScanner.scan(classesDirectory, recursive, new SuffixFileFilter(FileConstants.CLASS_EXTENSION));
        for (File classFile : classFiles) {
            String className = resolveClassName(classesDirectory, classFile);
            classNames.add(className);
        }
        return classNames;
    }

    protected static Set<String> findClassNamesInJarFile(File jarFile, boolean recursive) {
        if (!jarFile.exists()) {
            return Collections.emptySet();
        }

        Set<String> classNames = new LinkedHashSet();

        SimpleJarEntryScanner simpleJarEntryScanner = SimpleJarEntryScanner.INSTANCE;
        try {
            JarFile jarFile_ = new JarFile(jarFile);
            Set<JarEntry> jarEntries = simpleJarEntryScanner.scan(jarFile_, recursive, ClassFileJarEntryFilter.INSTANCE);

            for (JarEntry jarEntry : jarEntries) {
                String jarEntryName = jarEntry.getName();
                String className = resolveClassName(jarEntryName);
                if (StringUtils.isNotBlank(className)) {
                    classNames.add(className);
                }
            }

        } catch (Exception e) {

        }

        return classNames;
    }

    protected static String resolveClassName(File classesDirectory, File classFile) {
        String classFileRelativePath = FileUtils.resolveRelativePath(classesDirectory, classFile);
        return resolveClassName(classFileRelativePath);
    }

    /**
     * Resolve resource name to class name
     *
     * @param resourceName resource name
     * @return class name
     */
    public static String resolveClassName(String resourceName) {
        String className = StringUtils.replace(resourceName, PathConstants.SLASH, Constants.DOT);
        className = StringUtils.substringBefore(className, FileConstants.CLASS_EXTENSION);
        while (StringUtils.startsWith(className, Constants.DOT)) {
            className = StringUtils.substringAfter(className, Constants.DOT);
        }
        return className;
    }

    /**
     * The map of all class names in {@link ClassPathUtils#getClassPaths() class path} , the class path for one {@link
     * JarFile} or classes directory as key , the class names set as value
     *
     * @return Read-only
     */
    @Nonnull
    public static Map<String, Set<String>> getClassPathToClassNamesMap() {
        return classPathToClassNamesMap;
    }

    /**
     * The set of all class names in {@link ClassPathUtils#getClassPaths() class path}
     *
     * @return Read-only
     */
    @Nonnull
    public static Set<String> getAllClassNamesInClassPaths() {
        Set<String> allClassNames = new LinkedHashSet();
        for (Set<String> classNames : classPathToClassNamesMap.values()) {
            allClassNames.addAll(classNames);
        }
        return Collections.unmodifiableSet(allClassNames);
    }


    /**
     * Get {@link Class}'s code source location URL
     *
     * @param type
     * @return If , return <code>null</code>.
     * @throws NullPointerException If <code>type</code> is <code>null</code> , {@link NullPointerException} will be thrown.
     */
    public static URL getCodeSourceLocation(Class<?> type) throws NullPointerException {

        URL codeSourceLocation = null;
        ClassLoader classLoader = type.getClassLoader();

        if (classLoader == null) { // Bootstrap ClassLoader or type is primitive or void
            String path = findClassPath(type);
            if (StringUtils.isNotBlank(path)) {
                try {
                    codeSourceLocation = new File(path).toURI().toURL();
                } catch (MalformedURLException ignored) {
                    codeSourceLocation = null;
                }
            }
        } else {
            ProtectionDomain protectionDomain = type.getProtectionDomain();
            CodeSource codeSource = protectionDomain == null ? null : protectionDomain.getCodeSource();
            codeSourceLocation = codeSource == null ? null : codeSource.getLocation();
        }
        return codeSourceLocation;
    }

    /**
     * Get all super classes from the specified type
     *
     * @param type         the specified type
     * @param classFilters the filters for classes
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getAllSuperClasses(Class<?> type, Predicate<Class<?>>... classFilters) {

        Set<Class<?>> allSuperClasses = new LinkedHashSet<>();

        Class<?> superClass = type.getSuperclass();
        while (superClass != null) {
            // add current super class
            allSuperClasses.add(superClass);
            superClass = superClass.getSuperclass();
        }

        return unmodifiableSet(filterAll(allSuperClasses, classFilters));
    }

    /**
     * Get all interfaces from the specified type
     *
     * @param type             the specified type
     * @param interfaceFilters the filters for interfaces
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getAllInterfaces(Class<?> type, Predicate<Class<?>>... interfaceFilters) {
        if (type == null || type.isPrimitive()) {
            return emptySet();
        }

        Set<Class<?>> allInterfaces = new LinkedHashSet<>();
        Set<Class<?>> resolved = new LinkedHashSet<>();
        Queue<Class<?>> waitResolve = new LinkedList<>();

        resolved.add(type);
        Class<?> clazz = type;
        while (clazz != null) {

            Class<?>[] interfaces = clazz.getInterfaces();

            if (isNotEmpty(interfaces)) {
                // add current interfaces
                Arrays.stream(interfaces).filter(resolved::add).forEach(cls -> {
                    allInterfaces.add(cls);
                    waitResolve.add(cls);
                });
            }

            // add all super classes to waitResolve
            getAllSuperClasses(clazz).stream().filter(resolved::add).forEach(waitResolve::add);

            clazz = waitResolve.poll();
        }

        return filterAll(allInterfaces, interfaceFilters);
    }

    /**
     * Get all inherited types from the specified type
     *
     * @param type        the specified type
     * @param typeFilters the filters for types
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getAllInheritedTypes(Class<?> type, Predicate<Class<?>>... typeFilters) {
        // Add all super classes
        Set<Class<?>> types = new LinkedHashSet<>(getAllSuperClasses(type, typeFilters));
        // Add all interface classes
        types.addAll(getAllInterfaces(type, typeFilters));
        return unmodifiableSet(types);
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
     * Is generic class or not?
     *
     * @param type the target type
     * @return if the target type is not null or <code>void</code> or Void.class, return <code>true</code>, or false
     */
    public static boolean isGenericClass(Class<?> type) {
        return type != null && !void.class.equals(type) && !Void.class.equals(type);
    }

    /**
     * Resolve the types of the specified values
     *
     * @param values the values
     * @return If can't be resolved, return {@link ArrayUtils#EMPTY_CLASS_ARRAY empty class array}
     */
    public static Class[] getTypes(Object... values) {

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
     * Get all classes from the specified type with filters
     *
     * @param type         the specified type
     * @param classFilters class filters
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getAllClasses(Class<?> type, Predicate<Class<?>>... classFilters) {
        return getAllClasses(type, true, classFilters);
    }

    /**
     * Get all classes(may include self type) from the specified type with filters
     *
     * @param type         the specified type
     * @param includedSelf included self type or not
     * @param classFilters class filters
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getAllClasses(Class<?> type, boolean includedSelf, Predicate<Class<?>>... classFilters) {
        if (type == null || type.isPrimitive()) {
            return emptySet();
        }

        List<Class<?>> allClasses = new LinkedList<>();

        Class<?> superClass = type.getSuperclass();
        while (superClass != null) {
            // add current super class
            allClasses.add(superClass);
            superClass = superClass.getSuperclass();
        }

        // FIFO -> FILO
        Collections.reverse(allClasses);

        if (includedSelf) {
            allClasses.add(type);
        }

        // Keep the same order from List
        return asSet(filterAll(allClasses, classFilters));
    }

    /**
     * the semantics is same as {@link Class#isAssignableFrom(Class)}
     *
     * @param targetType the target type
     * @param superTypes the super types
     * @return see {@link Class#isAssignableFrom(Class)}
     * @since 1.0.0
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

        List<Constructor<?>> constructors = getDeclaredConstructors(type, constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (length != parameterTypes.length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                Object arg = args[i];
                Class<?> parameterType = parameterTypes[i];
                if (!parameterType.isInstance(arg)) {
                    return false;
                }
            }
            return true;
        });

        if (constructors.isEmpty()) {
            String message = format("No constructor[class : '{}'] matches the arguments : {}", getTypeName(type), Arrays.asList(args));
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
}
