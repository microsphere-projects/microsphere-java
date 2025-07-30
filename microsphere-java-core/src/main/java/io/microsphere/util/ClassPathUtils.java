/**
 *
 */
package io.microsphere.util;


import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Set;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.constants.SeparatorConstants.PATH_SEPARATOR;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.isLoadedClass;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.StringUtils.split;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Collections.emptySet;

/**
 * {@link ClassPathUtils} is an abstract utility class that provides methods for retrieving various class path-related information.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Retrieving bootstrap class paths</li>
 *     <li>Retrieving application class paths</li>
 *     <li>Locating the runtime URL of a class by name or type</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Get all application class paths
 * Set<String> classPaths = ClassPathUtils.getClassPaths();
 * for (String path : classPaths) {
 *     System.out.println("ClassPath: " + path);
 * }
 *
 * // Get the location of a specific class
 * URL location = ClassPathUtils.getRuntimeClassLocation(io.microsphere.util.ClassPathUtils.class);
 * if (location != null) {
 *     System.out.println("Class Location: " + location);
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassPathUtils
 * @since 1.0.0
 */
public abstract class ClassPathUtils implements Utils {

    protected static final RuntimeMXBean runtimeMXBean = getRuntimeMXBean();

    @Immutable
    private static final Set<String> bootstrapClassPaths = initBootstrapClassPaths();

    @Immutable
    private static final Set<String> classPaths = initClassPaths();

    @Nonnull
    @Immutable
    private static Set<String> initBootstrapClassPaths() {
        if (runtimeMXBean.isBootClassPathSupported()) {
            return resolveClassPaths(runtimeMXBean.getBootClassPath());
        }
        return emptySet();
    }

    @Nonnull
    @Immutable
    private static Set<String> initClassPaths() {
        return resolveClassPaths(runtimeMXBean.getClassPath());
    }

    @Nonnull
    @Immutable
    private static Set<String> resolveClassPaths(String classPath) {
        String[] classPathsArray = split(classPath, PATH_SEPARATOR);
        return ofSet(classPathsArray);
    }

    /**
     * Returns the set of bootstrap class paths.
     *
     * <p>If {@link RuntimeMXBean#isBootClassPathSupported()} returns <code>false</code>,
     * this method will return an empty set.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> bootClassPaths = ClassPathUtils.getBootstrapClassPaths();
     * for (String path : bootClassPaths) {
     *     System.out.println("Bootstrap ClassPath: " + path);
     * }
     * }</pre>
     *
     * @return a non-null set of bootstrap class paths; if boot class path is not supported,
     * returns an empty set.
     */
    @Nonnull
    public static Set<String> getBootstrapClassPaths() {
        return bootstrapClassPaths;
    }

    /**
     * Returns the set of application class paths.
     *
     * <p>This method provides access to the class paths used by the Java runtime for loading application classes.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> classPaths = ClassPathUtils.getClassPaths();
     * for (String path : classPaths) {
     *     System.out.println("ClassPath: " + path);
     * }
     * }</pre>
     *
     * @return a non-null set of class paths; if no class paths are available, returns an empty set.
     * @see RuntimeMXBean#getClassPath()
     */
    @Nonnull
    public static Set<String> getClassPaths() {
        return classPaths;
    }

    /**
     * Get Class Location URL from specified class name at runtime.
     *
     * <p>If the class associated with the provided {@code className} is loaded by the default class loader
     * (see {@link ClassLoaderUtils#getDefaultClassLoader()}), this method returns the location URL of that class.
     * Otherwise, it returns <code>null</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String className = "io.microsphere.util.ClassPathUtils";
     * URL classLocation = ClassPathUtils.getRuntimeClassLocation(className);
     * if (classLocation != null) {
     *     System.out.println("Class Location: " + classLocation);
     * } else {
     *     System.out.println("Class not found or not loadable.");
     * }
     * }</pre>
     *
     * @param className the fully qualified name of the class to find the location for
     * @return the URL representing the location of the class if it is loaded by the default class loader;
     * otherwise, <code>null</code>
     * @see #getRuntimeClassLocation(Class)
     */
    @Nullable
    public static URL getRuntimeClassLocation(String className) {
        ClassLoader classLoader = getDefaultClassLoader();
        if (isLoadedClass(classLoader, className)) {
            return getRuntimeClassLocation(resolveClass(className, classLoader));
        }
        return null;
    }

    /**
     * Get Class Location URL from specified {@link Class} at runtime.
     *
     * <p>This method determines the location (URL) from which the provided class was loaded. If the class is a
     * primitive type, array type, or synthetic type, or if a security manager prevents access to the protection domain,
     * this method returns <code>null</code>.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> type = io.microsphere.util.ClassPathUtils.class;
     * URL classLocation = ClassPathUtils.getRuntimeClassLocation(type);
     * if (classLocation != null) {
     *     System.out.println("Class Location: " + classLocation);
     * } else {
     *     System.out.println("Class location could not be determined.");
     * }
     * }</pre>
     *
     * @param type The class for which to find the location.
     * @return The URL representing the location of the class if it can be determined; otherwise, <code>null</code>.
     */
    @Nullable
    public static URL getRuntimeClassLocation(Class<?> type) {
        ClassLoader classLoader = type.getClassLoader();
        URL location = null;
        if (classLoader != null) { // Non-Bootstrap
            try {
                ProtectionDomain protectionDomain = type.getProtectionDomain();
                CodeSource codeSource = protectionDomain.getCodeSource();
                location = codeSource == null ? null : codeSource.getLocation();
            } catch (SecurityException exception) {

            }
        } else if (!type.isPrimitive() && !type.isArray() && !type.isSynthetic()) { // Bootstrap ClassLoader
            // Class was loaded by Bootstrap ClassLoader
            location = getClassResource(getSystemClassLoader(), type.getName());
        }
        return location;
    }

    private ClassPathUtils() {
    }
}
