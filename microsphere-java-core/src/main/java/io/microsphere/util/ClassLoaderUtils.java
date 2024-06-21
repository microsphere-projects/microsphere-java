/**
 *
 */
package io.microsphere.util;

import io.microsphere.classloading.URLClassPathHandle;
import io.microsphere.collection.CollectionUtils;
import io.microsphere.collection.ListUtils;
import io.microsphere.constants.Constants;
import io.microsphere.constants.FileConstants;
import io.microsphere.constants.PathConstants;
import io.microsphere.net.URLUtils;
import io.microsphere.reflect.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarFile;

import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.util.ClassUtils.getClassNamesInClassPath;
import static io.microsphere.util.ExceptionUtils.getStackTrace;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;


/**
 * {@link ClassLoader} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassLoader
 * @since 1.0.0
 */
public abstract class ClassLoaderUtils extends BaseUtils {

    protected static final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

    private static final Method findLoadedClassMethod = initFindLoadedClassMethod();

    private static final ConcurrentMap<String, Class<?>> loadedClassesCache = initLoadedClassesCache();

    private static final URLClassPathHandle urlClassPathHandle = initURLClassPathHandle();

    /**
     * Initializes {@link Method} for {@link ClassLoader#findLoadedClass(String)}
     *
     * @return {@link Method} for {@link ClassLoader#findLoadedClass(String)}
     */
    private static Method initFindLoadedClassMethod() {
        final Method findLoadedClassMethod;
        try {
            findLoadedClassMethod = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            findLoadedClassMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw jvmUnsupportedOperationException(e);
        }
        return findLoadedClassMethod;
    }

    private static ConcurrentMap<String, Class<?>> initLoadedClassesCache() {
        ConcurrentMap<String, Class<?>> loadedClassesCache = new ConcurrentHashMap<>(256);
        addShutdownHookCallback(loadedClassesCache::clear);
        return loadedClassesCache;
    }

    private static URLClassPathHandle initURLClassPathHandle() {
        List<URLClassPathHandle> urlClassPathHandles = loadServicesList(URLClassPathHandle.class);
        for (URLClassPathHandle urlClassPathHandle : urlClassPathHandles) {
            if (urlClassPathHandle.supports()) {
                return urlClassPathHandle;
            }
        }
        throw new IllegalStateException("No URLClassPathHandle found, Please check the META-INF/services/io.microsphere.classloading.URLClassPathHandle");
    }

    private static UnsupportedOperationException jvmUnsupportedOperationException(Throwable throwable) {
        String stackTrace = getStackTrace(throwable);
        String message = String.format("Current JVM[ Implementation : %s , Version : %s ] does not supported ! " + "Stack Trace : %s", SystemUtils.JAVA_VENDOR, SystemUtils.JAVA_VERSION, stackTrace);
        throw new UnsupportedOperationException(message);
    }

    /**
     * Returns the number of classes that are currently loaded in the Java virtual machine.
     *
     * @return the number of currently loaded classes.
     */
    public static int getLoadedClassCount() {
        return classLoadingMXBean.getLoadedClassCount();
    }

    /**
     * Returns the total number of classes unloaded since the Java virtual machine has started execution.
     *
     * @return the total number of unloaded classes.
     */
    public static long getUnloadedClassCount() {
        return classLoadingMXBean.getUnloadedClassCount();
    }

    /**
     * Tests if the verbose output for the class loading system is enabled.
     *
     * @return <tt>true</tt> if the verbose output for the class loading system is enabled; <tt>false</tt> otherwise.
     */
    public static boolean isVerbose() {
        return classLoadingMXBean.isVerbose();
    }

    /**
     * Enables or disables the verbose output for the class loading system.  The verbose output information and the
     * output stream to which the verbose information is emitted are implementation dependent.  Typically, a Java
     * virtual machine implementation prints a message each time a class file is loaded.
     * <p/>
     * <p>This method can be called by multiple threads concurrently. Each invocation of this method enables or disables
     * the verbose output globally.
     *
     * @param value <tt>true</tt> to enable the verbose output; <tt>false</tt> to disable.
     * @throws SecurityException if a security manager exists and the caller does not have ManagementPermission("control").
     */
    public static void setVerbose(boolean value) {
        classLoadingMXBean.setVerbose(value);
    }

    /**
     * Returns the total number of classes that have been loaded since the Java virtual machine has started execution.
     *
     * @return the total number of classes loaded.
     */
    public static long getTotalLoadedClassCount() {
        return classLoadingMXBean.getTotalLoadedClassCount();
    }

    /**
     * Get the default the ClassLoader to use.
     *
     * @return the ClassLoader (only {@code null} if even the system ClassLoader isn't accessible)
     * @see Thread#getContextClassLoader()
     * @see Class#getClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     * @see ReflectionUtils#getCallerClass()
     */
    @Nullable
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignored) {
        }

        if (classLoader == null) { // If the ClassLoader is also not found,
            // try to get the ClassLoader from the Caller class
            Class<?> callerClass = ReflectionUtils.getCallerClass(3);
            if (callerClass != null) {
                classLoader = callerClass.getClassLoader();
            }
        }

        if (classLoader == null) {
            classLoader = ClassLoaderUtils.class.getClassLoader();
        }

        if (classLoader == null) {
            // classLoader is null indicates the bootstrap ClassLoader
            try {
                classLoader = ClassLoader.getSystemClassLoader();
            } catch (Throwable ignored) {
            }
        }
        return classLoader;
    }

    /**
     * Get the ClassLoader from the loaded class if present.
     *
     * @param loadedClass the optional class was loaded by some {@link ClassLoader}
     * @return the ClassLoader (only {@code null} if even the system ClassLoader isn't accessible)
     * @see #getDefaultClassLoader()
     */
    @Nullable
    public static ClassLoader getClassLoader(@Nullable Class<?> loadedClass) {
        ClassLoader classLoader = null;
        try {
            if (loadedClass == null) {
                classLoader = getCallerClassLoader(4);
            } else {
                classLoader = loadedClass.getClassLoader();
            }
        } catch (SecurityException ignored) {
        }
        return classLoader == null ? getDefaultClassLoader() : classLoader;
    }

    /**
     * Return the ClassLoader from the caller class
     *
     * @return the ClassLoader (only {@code null} if the caller class was absent
     * @see ReflectionUtils#getCallerClass()
     */
    public static ClassLoader getCallerClassLoader() {
        return getCallerClassLoader(4);
    }

    /**
     * Return the ClassLoader from the caller class
     *
     * @return the ClassLoader (only {@code null} if the caller class was absent
     * @see ReflectionUtils#getCallerClass()
     */
    private static ClassLoader getCallerClassLoader(int invocationFrame) {
        ClassLoader classLoader = null;
        Class<?> callerClass = ReflectionUtils.getCallerClass(invocationFrame);
        if (callerClass != null) {
            classLoader = callerClass.getClassLoader();
        }
        return classLoader;
    }

    /**
     * Find Loaded {@link Class} under specified inheritable {@link ClassLoader} and class names
     *
     * @param classLoader {@link ClassLoader}
     * @param classNames  class names set
     * @return {@link Class} if loaded , or <code>null</code>
     */
    public static Set<Class<?>> findLoadedClasses(ClassLoader classLoader, Set<String> classNames) {
        Set<Class<?>> loadedClasses = new LinkedHashSet();
        for (String className : classNames) {
            Class<?> class_ = findLoadedClass(classLoader, className);
            if (class_ != null) {
                loadedClasses.add(class_);
            }
        }
        return Collections.unmodifiableSet(loadedClasses);
    }

    /**
     * Check specified {@link Class} is loaded on specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param type        {@link Class}
     * @return If Loaded , return <code>true</code> , or <code>false</code>
     */
    public static boolean isLoadedClass(ClassLoader classLoader, Class<?> type) {
        return isLoadedClass(classLoader, type.getName());
    }

    /**
     * Check specified {@link Class#getName() class name}  is loaded on specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   {@link Class#getName() class name}
     * @return If Loaded , return <code>true</code> , or <code>false</code>
     */
    public static boolean isLoadedClass(ClassLoader classLoader, String className) {
        return findLoadedClass(classLoader, className) != null;
    }

    /**
     * Find Loaded {@link Class} under specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   class name
     * @return {@link Class} if loaded , or <code>null</code>
     */
    public static Class<?> findLoadedClass(ClassLoader classLoader, String className) {
        Class<?> loadedClass = null;
        Set<ClassLoader> classLoaders = getInheritableClassLoaders(classLoader);
        try {
            for (ClassLoader loader : classLoaders) {
                loadedClass = (Class<?>) findLoadedClassMethod.invoke(loader, className);
                if (loadedClass != null) {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return loadedClass;
    }

    /**
     * Loaded specified class name under {@link ClassLoader}
     *
     * @param className   the name of {@link Class}
     * @param classLoader {@link ClassLoader}
     * @return {@link Class} if can be loaded
     */
    @Nullable
    public static Class<?> loadClass(@Nonnull String className, @Nonnull ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Loaded specified class name under {@link ClassLoader}
     *
     * @param className   the name of {@link Class}
     * @param classLoader {@link ClassLoader}
     * @param cached      the resolved class is required to be cached or not
     * @return {@link Class} if can be loaded
     */
    public static Class<?> loadClass(String className, ClassLoader classLoader, boolean cached) {
        Class loadedClass = null;
        if (cached) {
            String cacheKey = buildCacheKey(className, classLoader);
            loadedClass = loadedClassesCache.computeIfAbsent(cacheKey, k -> execute(() -> loadClass(className, classLoader)));
        } else {
            loadedClass = loadClass(className, classLoader);
        }
        return loadedClass;
    }

    /**
     * Get the resource URLs Set under specified resource name and type
     *
     * @param classLoader  ClassLoader
     * @param resourceType {@link ResourceType} Enum
     * @param resourceName resource name ，e.g : <br /> <ul> <li>Resource Name :<code>"/com/abc/def.log"</code></li> <li>Class Name :
     *                     <code>"java.lang.String"</code></li> </ul>
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     * @throws IOException
     */
    public static Set<URL> getResources(ClassLoader classLoader, ResourceType resourceType, String resourceName) throws NullPointerException, IOException {
        String normalizedResourceName = resourceType.resolve(resourceName);
        Enumeration<URL> resources = classLoader.getResources(normalizedResourceName);
        return resources != null && resources.hasMoreElements() ? new LinkedHashSet(ListUtils.toList(resources)) : Collections.emptySet();
    }

    /**
     * Get the resource URLs list under specified resource name
     *
     * @param classLoader  ClassLoader
     * @param resourceName resource name ，e.g : <br /> <ul> <li>Resource Name :<code>"/com/abc/def.log"</code></li> <li>Class Name :
     *                     <code>"java.lang.String"</code></li> </ul>
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     * @throws IOException
     */
    public static Set<URL> getResources(ClassLoader classLoader, String resourceName) throws NullPointerException, IOException {
        Set<URL> resourceURLs = Collections.emptySet();
        for (ResourceType resourceType : ResourceType.values()) {
            resourceURLs = getResources(classLoader, resourceType, resourceName);
            if (CollectionUtils.isNotEmpty(resourceURLs)) {
                break;
            }
        }
        return resourceURLs;
    }

    /**
     * Get the resource URL under specified resource name
     *
     * @param classLoader  ClassLoader
     * @param resourceName resource name ，e.g : <br /> <ul> <li>Resource Name :<code>"/com/abc/def.log"</code></li> <li>Class Name :
     *                     <code>"java.lang.String"</code></li> </ul>
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getResource(ClassLoader classLoader, String resourceName) throws NullPointerException {
        URL resourceURL = null;
        for (ResourceType resourceType : ResourceType.values()) {
            resourceURL = getResource(classLoader, resourceType, resourceName);
            if (resourceURL != null) {
                break;
            }
        }
        return resourceURL;
    }

    /**
     * Get the resource URL under specified resource name and type
     *
     * @param classLoader  ClassLoader
     * @param resourceType {@link ResourceType} Enum
     * @param resourceName resource name ，e.g : <br /> <ul> <li>Resource Name :<code>"/com/abc/def.log"</code></li> <li>Class Name :
     *                     <code>"java.lang.String"</code></li> </ul>
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getResource(ClassLoader classLoader, ResourceType resourceType, String resourceName) throws NullPointerException {
        String normalizedResourceName = resourceType.resolve(resourceName);
        return classLoader.getResource(normalizedResourceName);
    }


    /**
     * Get the {@link Class} resource URL under specified {@link Class#getName() Class name}
     *
     * @param classLoader ClassLoader
     * @param className   class name
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getClassResource(ClassLoader classLoader, String className) {
        final String resourceName = className + FileConstants.CLASS_EXTENSION;
        return getResource(classLoader, ResourceType.CLASS, resourceName);
    }

    /**
     * Get the {@link Class} resource URL under specified {@link Class}
     *
     * @param classLoader ClassLoader
     * @param type        {@link Class type}
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     * @version 1.0.0
     * @since 1.0.0
     */
    public static URL getClassResource(ClassLoader classLoader, Class<?> type) {
        String resourceName = type.getName();
        return getClassResource(classLoader, resourceName);
    }

    /**
     * Get all Inheritable {@link ClassLoader ClassLoaders} {@link Set} (including {@link ClassLoader} argument)
     *
     * @param classLoader {@link ClassLoader}
     * @return Read-only {@link Set}
     * @throws NullPointerException If <code>classLoader</code> argument is <code>null</code>
     */
    @Nonnull
    public static Set<ClassLoader> getInheritableClassLoaders(ClassLoader classLoader) throws NullPointerException {
        Set<ClassLoader> classLoadersSet = new LinkedHashSet();
        classLoadersSet.add(classLoader);
        ClassLoader parentClassLoader = classLoader.getParent();
        while (parentClassLoader != null) {
            classLoadersSet.add(parentClassLoader);
            parentClassLoader = parentClassLoader.getParent();
        }
        return Collections.unmodifiableSet(classLoadersSet);
    }

    /**
     * Get all loaded classes {@link Map} under specified inheritable {@link ClassLoader} , {@link ClassLoader} as key ,
     * its loaded classes {@link Set} as value.
     *
     * @param classLoader {@link ClassLoader}
     * @return Read-only Map
     * @throws UnsupportedOperationException
     * @throws NullPointerException          If <code>classLoader</code> argument is <code>null</code>
     */
    @Nonnull
    public static Map<ClassLoader, Set<Class<?>>> getAllLoadedClassesMap(ClassLoader classLoader) throws UnsupportedOperationException {
        Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = new LinkedHashMap();
        Set<ClassLoader> classLoadersSet = getInheritableClassLoaders(classLoader);
        for (ClassLoader loader : classLoadersSet) {
            allLoadedClassesMap.put(loader, getLoadedClasses(loader));
        }
        return Collections.unmodifiableMap(allLoadedClassesMap);
    }

    /**
     * Get all loaded classes {@link Set} under specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @return Read-only {@link Set}
     * @throws UnsupportedOperationException If JVM does not support
     * @throws NullPointerException          If <code>classLoader</code> argument is <code>null</code>
     */
    @Nonnull
    public static Set<Class<?>> getAllLoadedClasses(ClassLoader classLoader) throws UnsupportedOperationException {
        Set<Class<?>> allLoadedClassesSet = new LinkedHashSet();
        Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = getAllLoadedClassesMap(classLoader);
        for (Set<Class<?>> loadedClassesSet : allLoadedClassesMap.values()) {
            allLoadedClassesSet.addAll(loadedClassesSet);
        }
        return Collections.unmodifiableSet(allLoadedClassesSet);
    }

    /**
     * Get loaded classes {@link Set} under specified {@link ClassLoader}( not all inheritable {@link ClassLoader
     * ClassLoaders})
     *
     * @param classLoader {@link ClassLoader}
     * @return Read-only {@link Set}
     * @throws UnsupportedOperationException If JVM does not support
     * @throws NullPointerException          If <code>classLoader</code> argument is <code>null</code>
     * @see #getAllLoadedClasses(ClassLoader)
     */
    @Nonnull
    public static Set<Class<?>> getLoadedClasses(ClassLoader classLoader) throws UnsupportedOperationException {
        final Set<Class<?>> classesSet;
        try {
            List<Class<?>> classes = getFieldValue(classLoader, "classes");
            classesSet = new LinkedHashSet(classes);
        } catch (Throwable e) {
            throw jvmUnsupportedOperationException(e);
        }
        return Collections.unmodifiableSet(new LinkedHashSet(classesSet));
    }

    /**
     * Find loaded classes {@link Set} in class path
     *
     * @param classLoader {@link ClassLoader}
     * @return Read-only {@link Set}
     * @throws UnsupportedOperationException If JVM does not support
     */
    public static Set<Class<?>> findLoadedClassesInClassPath(ClassLoader classLoader) throws UnsupportedOperationException {
        Set<String> classNames = ClassUtils.getAllClassNamesInClassPaths();
        return findLoadedClasses(classLoader, classNames);
    }

    /**
     * Find loaded classes {@link Set} in class paths {@link Set}
     *
     * @param classLoader {@link ClassLoader}
     * @param classPaths  the class paths for the {@link Set} of {@link JarFile} or classes directory
     * @return Read-only {@link Set}
     * @throws UnsupportedOperationException If JVM does not support
     * @see #findLoadedClass(ClassLoader, String)
     */
    public static Set<Class<?>> findLoadedClassesInClassPaths(ClassLoader classLoader, Set<String> classPaths) throws UnsupportedOperationException {
        Set<Class<?>> loadedClasses = new LinkedHashSet();
        for (String classPath : classPaths) {
            loadedClasses.addAll(findLoadedClassesInClassPath(classLoader, classPath));
        }
        return loadedClasses;
    }

    /**
     * Find loaded classes {@link Set} in class path
     *
     * @param classLoader {@link ClassLoader}
     * @param classPath   the class path for one {@link JarFile} or classes directory
     * @return Read-only {@link Set}
     * @throws UnsupportedOperationException If JVM does not support
     * @see #findLoadedClass(ClassLoader, String)
     */
    public static Set<Class<?>> findLoadedClassesInClassPath(ClassLoader classLoader, String classPath) throws UnsupportedOperationException {
        Set<String> classNames = getClassNamesInClassPath(classPath, true);
        return findLoadedClasses(classLoader, classNames);
    }

    /**
     * Test the specified class name is present in the {@link ClassLoader}
     *
     * @param className   the name of {@link Class}
     * @param classLoader {@link ClassLoader}
     * @return If found, return <code>true</code>
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        return resolveClass(className, classLoader) != null;
    }

    /**
     * Resolve the {@link Class} by the specified name using {@link #getDefaultClassLoader()}
     *
     * @param className the name of {@link Class}
     * @return If can't be resolved , return <code>null</code>
     */
    public static Class<?> resolveClass(@Nullable String className) {
        return resolveClass(className, getDefaultClassLoader());
    }

    /**
     * Resolve the {@link Class} by the specified name and {@link ClassLoader}
     *
     * @param className   the name of {@link Class}
     * @param classLoader {@link ClassLoader}
     * @return If can't be resolved , return <code>null</code>
     */
    public static Class<?> resolveClass(@Nullable String className, @Nullable ClassLoader classLoader) {
        return resolveClass(className, classLoader, false);
    }

    /**
     * Resolve the {@link Class} by the specified name and {@link ClassLoader}
     *
     * @param className   the name of {@link Class}
     * @param classLoader {@link ClassLoader}
     * @param cached      the resolved class is required to be cached or not
     * @return If can't be resolved , return <code>null</code>
     */
    public static Class<?> resolveClass(@Nullable String className, @Nullable ClassLoader classLoader, boolean cached) {
        if (className == null) {
            return null;
        }

        Class<?> targetClass = null;
        try {
            ClassLoader targetClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
            targetClass = loadClass(className, targetClassLoader, cached);
        } catch (Throwable ignored) { // Ignored
        }
        return targetClass;
    }

    public static Set<URL> findAllClassPathURLs(ClassLoader classLoader) {

        Set<URL> allClassPathURLs = new LinkedHashSet<>();

        URL[] classPathURLs = urlClassPathHandle.getURLs(classLoader);
        int length = classPathURLs.length;

        allClassPathURLs.addAll(Arrays.asList(classPathURLs).subList(0, length));

        ClassLoader parentClassLoader = classLoader.getParent();
        if (parentClassLoader != null) {
            allClassPathURLs.addAll(findAllClassPathURLs(parentClassLoader));
        }
        return allClassPathURLs;
    }

    public static boolean removeClassPathURL(ClassLoader classLoader, URL url) {
        if (!(classLoader instanceof URLClassLoader)) {
            return false;
        }
        return urlClassPathHandle.removeURL(classLoader, url);
    }

    public static URLClassLoader findURLClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        }
        URLClassLoader urlClassLoader = null;
        if (classLoader instanceof URLClassLoader) {
            urlClassLoader = (URLClassLoader) classLoader;
        } else {
            urlClassLoader = findURLClassLoader(classLoader.getParent());
        }
        return urlClassLoader;
    }

    private static URL toURL(String path) {
        URL url = null;
        try {
            URI uri = new File(path).toURI();
            url = uri.toURL();
        } catch (Exception ignored) {
        }
        return url;
    }

    private static String buildCacheKey(String className, ClassLoader classLoader) {
        String cacheKey = className + classLoader.hashCode();
        return cacheKey;
    }

    /**
     * Resource Type
     */
    public enum ResourceType {

        DEFAULT {
            @Override
            boolean supported(String name) {
                return true;
            }

            @Override
            public String normalize(String name) {
                return name;
            }


        }, CLASS {
            @Override
            boolean supported(String name) {
                return StringUtils.endsWith(name, FileConstants.CLASS_EXTENSION);
            }

            @Override
            public String normalize(String name) {
                String className = StringUtils.replace(name, FileConstants.CLASS_EXTENSION, StringUtils.EMPTY);
                return StringUtils.replace(className, Constants.DOT, PathConstants.SLASH) + FileConstants.CLASS_EXTENSION;
            }


        }, PACKAGE {
            @Override
            boolean supported(String name) {
                //TODO: use regexp to match more precise
                return !CLASS.supported(name) && !StringUtils.contains(name, PathConstants.SLASH) && !StringUtils.contains(name, PathConstants.BACK_SLASH);
            }

            @Override
            String normalize(String name) {
                return StringUtils.replace(name, Constants.DOT, PathConstants.SLASH) + PathConstants.SLASH;
            }


        };

        /**
         * resolves resource name
         *
         * @param name resource name
         * @return a newly resolved resource name
         */
        public String resolve(String name) {
            String normalizedName = supported(name) ? normalize(name) : null;
            if (normalizedName == null) return normalizedName;

            normalizedName = URLUtils.normalizePath(normalizedName);

            // 除去开头的"/"
            while (normalizedName.startsWith("/")) {
                normalizedName = normalizedName.substring(1);
            }

            return normalizedName;
        }

        /**
         * Is supported specified resource name in current resource type
         *
         * @param name resource name
         * @return If supported , return <code>true</code> , or return <code>false</code>
         */
        abstract boolean supported(String name);

        /**
         * Normalizes resource name
         *
         * @param name resource name
         * @return normalized resource name
         */
        abstract String normalize(String name);


    }


}
