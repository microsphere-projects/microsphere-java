/**
 *
 */
package io.microsphere.util;


import io.microsphere.annotation.Nonnull;

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
 * {@link ClassPathUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassPathUtils
 * @since 1.0.0
 */
public abstract class ClassPathUtils implements Utils {

    protected static final RuntimeMXBean runtimeMXBean = getRuntimeMXBean();

    private static final Set<String> bootstrapClassPaths = initBootstrapClassPaths();

    private static final Set<String> classPaths = initClassPaths();

    private static Set<String> initBootstrapClassPaths() {
        if (runtimeMXBean.isBootClassPathSupported()) {
            return resolveClassPaths(runtimeMXBean.getBootClassPath());
        }
        return emptySet();
    }

    private static Set<String> initClassPaths() {
        return resolveClassPaths(runtimeMXBean.getClassPath());
    }

    private static Set<String> resolveClassPaths(String classPath) {
        String[] classPathsArray = split(classPath, PATH_SEPARATOR);
        return ofSet(classPathsArray);
    }


    /**
     * Get Bootstrap Class Paths {@link Set}
     *
     * @return If {@link RuntimeMXBean#isBootClassPathSupported()} == <code>false</code>, will return empty set.
     */
    @Nonnull
    public static Set<String> getBootstrapClassPaths() {
        return bootstrapClassPaths;
    }

    /**
     * Get {@link #classPaths}
     *
     * @return Class Paths {@link Set}
     **/
    @Nonnull
    public static Set<String> getClassPaths() {
        return classPaths;
    }

    /**
     * Get Class Location URL from specified class name at runtime
     *
     * @param className class name
     * @return If <code>className</code> associated class is loaded on {@link Thread#getContextClassLoader() Thread
     * context ClassLoader} , return class location URL, or return <code>null</code>
     * @see #getRuntimeClassLocation(Class)
     */
    public static URL getRuntimeClassLocation(String className) {
        ClassLoader classLoader = getDefaultClassLoader();
        if (isLoadedClass(classLoader, className)) {
            return getRuntimeClassLocation(resolveClass(className, classLoader));
        }
        return null;
    }

    /**
     * Get Class Location URL from specified {@link Class} at runtime
     *
     * @param type {@link Class}
     * @return If <code>type</code> is <code>{@link Class#isPrimitive() primitive type}</code>, <code>{@link
     * Class#isArray() array type}</code>, <code>{@link Class#isSynthetic() synthetic type}</code> or {a security
     * manager exists and its <code>checkPermission</code> method doesn't allow getting the ProtectionDomain., return
     * <code>null</code>
     */
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
}
