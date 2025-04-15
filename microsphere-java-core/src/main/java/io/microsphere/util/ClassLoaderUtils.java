/**
 *
 */
package io.microsphere.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.classloading.ServiceLoadingURLClassPathHandle;
import io.microsphere.classloading.URLClassPathHandle;
import io.microsphere.logging.Logger;
import io.microsphere.reflect.ReflectionUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.management.ClassLoadingMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarFile;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.constants.PathConstants.BACK_SLASH;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.lang.ClassDataRepository.INSTANCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.management.JmxUtils.getClassLoadingMXBean;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.reflect.ReflectionUtils.getCallerClass;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.values;
import static io.microsphere.util.StringUtils.contains;
import static io.microsphere.util.StringUtils.endsWith;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.SystemUtils.JAVA_VENDOR;
import static io.microsphere.util.SystemUtils.JAVA_VERSION;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;


/**
 * {@link ClassLoader} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassLoader
 * @since 1.0.0
 */
public abstract class ClassLoaderUtils implements Utils {

    private static final Logger logger = getLogger(ClassLoaderUtils.class);

    private static final Class<ClassLoader> classLoaderClass = ClassLoader.class;

    /**
     * The {@link Method} name of {@link ClassLoader#findLoadedClass(String)}
     */
    private static final String findLoadedClassMethodName = "findLoadedClass";

    private static final String classesFieldName = "classes";

    protected static final ClassLoadingMXBean classLoadingMXBean = getClassLoadingMXBean();

    private static final ConcurrentMap<String, Class<?>> loadedClassesCache = new ConcurrentHashMap<>(256);

    private static final URLClassPathHandle urlClassPathHandle = new ServiceLoadingURLClassPathHandle();

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
     * Returns the total number of classes that have been loaded since the Java virtual machine has started execution.
     *
     * @return the total number of classes loaded.
     */
    public static long getTotalLoadedClassCount() {
        return classLoadingMXBean.getTotalLoadedClassCount();
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
            classLoader = currentThread().getContextClassLoader();
        } catch (Throwable ignored) {
        }

        if (classLoader == null) {
            classLoader = ClassLoaderUtils.class.getClassLoader();
        }

        if (classLoader == null) {
            // classLoader is null indicates the bootstrap ClassLoader
            try {
                classLoader = getSystemClassLoader();
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
    @Nullable
    public static ClassLoader getCallerClassLoader() {
        return getCallerClassLoader(4);
    }

    /**
     * Find Loaded {@link Class} under specified inheritable {@link ClassLoader} and class names
     *
     * @param classLoader {@link ClassLoader}
     * @param classNames  class names
     * @return {@link Class} if loaded, or <code>null</code>
     */
    public static Set<Class<?>> findLoadedClasses(@Nullable ClassLoader classLoader, String... classNames) {
        return findLoadedClasses(classLoader, ofSet(classNames));
    }

    /**
     * Find Loaded {@link Class} under specified inheritable {@link ClassLoader} and class names
     *
     * @param classLoader {@link ClassLoader}
     * @param classNames  class names set
     * @return {@link Class} if loaded, or <code>null</code>
     */
    public static Set<Class<?>> findLoadedClasses(@Nullable ClassLoader classLoader, Iterable<String> classNames) {
        int size = size(classNames);
        if (size < 1) {
            return emptySet();
        }
        Set<Class<?>> loadedClasses = newLinkedHashSet(size);
        for (String className : classNames) {
            Class<?> class_ = findLoadedClass(classLoader, className);
            if (class_ != null) {
                loadedClasses.add(class_);
            }
        }
        return unmodifiableSet(loadedClasses);
    }

    /**
     * Check specified {@link Class} is loaded on specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param type        {@link Class}
     * @return If Loaded , return <code>true</code> , or <code>false</code>
     */
    public static boolean isLoadedClass(@Nullable ClassLoader classLoader, Class<?> type) {
        return isLoadedClass(classLoader, type.getName());
    }

    /**
     * Check specified {@link Class#getName() class name}  is loaded on specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   {@link Class#getName() class name}
     * @return If Loaded , return <code>true</code> , or <code>false</code>
     */
    public static boolean isLoadedClass(@Nullable ClassLoader classLoader, String className) {
        return findLoadedClass(classLoader, className) != null;
    }

    /**
     * Find Loaded {@link Class} under specified inheritable {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   class name
     * @return {@link Class} if loaded , or <code>null</code>
     */
    public static Class<?> findLoadedClass(@Nullable ClassLoader classLoader, String className) {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        Class<?> loadedClass = invokeFindLoadedClassMethod(actualClassLoader, className);
        if (loadedClass == null) {
            Set<ClassLoader> classLoaders = doGetInheritableClassLoaders(actualClassLoader);
            for (ClassLoader loader : classLoaders) {
                loadedClass = invokeFindLoadedClassMethod(loader, className);
                if (loadedClass != null) {
                    break;
                }
            }
        }
        return loadedClass;
    }

    /**
     * Loaded specified class name under {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   the name of {@link Class}
     * @return {@link Class} if can be loaded
     */
    @Nullable
    public static Class<?> loadClass(@Nullable ClassLoader classLoader, @Nullable String className) {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        return doLoadClass(actualClassLoader, className);
    }

    /**
     * Loaded specified class name under {@link ClassLoader}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   the name of {@link Class}
     * @param cached      the resolved class is required to be cached or not
     * @return {@link Class} if can be loaded
     */
    public static Class<?> loadClass(@Nullable ClassLoader classLoader, @Nullable String className, boolean cached) {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        if (cached) {
            String cacheKey = buildCacheKey(actualClassLoader, className);
            return loadedClassesCache.computeIfAbsent(cacheKey, k -> doLoadClass(actualClassLoader, className));
        }
        return doLoadClass(actualClassLoader, className);
    }

    protected static Class<?> doLoadClass(ClassLoader classLoader, String className) {
        if (isBlank(className)) {
            return null;
        }
        try {
            return classLoader.loadClass(className);
        } catch (Throwable e) {
            if (logger.isTraceEnabled()) {
                logger.trace("The Class[name : '{}'] can't be loaded from the ClassLoader : {}", className, classLoader, e);
            }
        }
        return null;
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
    public static Set<URL> getResources(@Nullable ClassLoader classLoader, ResourceType resourceType, String resourceName) throws NullPointerException, IOException {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        String normalizedResourceName = resourceType.resolve(resourceName);
        Enumeration<URL> resources = actualClassLoader.getResources(normalizedResourceName);
        return resources != null && resources.hasMoreElements() ? ofSet(resources) : emptySet();
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
    public static Set<URL> getResources(@Nullable ClassLoader classLoader, String resourceName) throws NullPointerException, IOException {
        Set<URL> resourceURLs = emptySet();
        for (ResourceType resourceType : ResourceType.values()) {
            resourceURLs = getResources(classLoader, resourceType, resourceName);
            if (isNotEmpty(resourceURLs)) {
                break;
            }
        }
        return resourceURLs;
    }

    /**
     * Get the resource URL under specified resource name
     *
     * @param resourceName resource name ，e.g : <br /> <ul> <li>Resource Name :<code>"/com/abc/def.log"</code></li> <li>Class Name :
     *                     <code>"java.lang.String"</code></li> </ul>
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getResource(String resourceName) throws NullPointerException {
        return getResource(null, resourceName);
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
    public static URL getResource(@Nullable ClassLoader classLoader, String resourceName) throws NullPointerException {
        URL resourceURL = null;
        for (ResourceType resourceType : values()) {
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
    public static URL getResource(@Nullable ClassLoader classLoader, ResourceType resourceType, String resourceName) throws NullPointerException {
        String normalizedResourceName = resourceType.resolve(resourceName);
        return normalizedResourceName == null ? null : findClassLoader(classLoader).getResource(normalizedResourceName);
    }

    /**
     * Get the {@link Class} resource URL under specified {@link Class#getName() Class name}
     *
     * @param classLoader ClassLoader
     * @param className   class name
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getClassResource(@Nullable ClassLoader classLoader, String className) {
        final String resourceName = className + CLASS_EXTENSION;
        return getResource(classLoader, ResourceType.CLASS, resourceName);
    }

    /**
     * Get the {@link Class} resource URL under specified {@link Class}
     *
     * @param type {@link Class type}
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getClassResource(Class<?> type) {
        return getClassResource(getClassLoader(type), type);
    }

    /**
     * Get the {@link Class} resource URL under specified {@link Class}
     *
     * @param classLoader ClassLoader
     * @param type        {@link Class type}
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If any argument is <code>null</code>
     */
    public static URL getClassResource(@Nullable ClassLoader classLoader, Class<?> type) {
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
    public static Set<ClassLoader> getInheritableClassLoaders(@Nullable ClassLoader classLoader) throws NullPointerException {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        return unmodifiableSet(doGetInheritableClassLoaders(actualClassLoader));
    }

    @Nonnull
    static Set<ClassLoader> doGetInheritableClassLoaders(ClassLoader classLoader) throws NullPointerException {
        Set<ClassLoader> classLoadersSet = newLinkedHashSet();
        classLoadersSet.add(classLoader);
        ClassLoader parentClassLoader = classLoader.getParent();
        while (parentClassLoader != null) {
            classLoadersSet.add(parentClassLoader);
            parentClassLoader = parentClassLoader.getParent();
        }
        return classLoadersSet;
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
    public static Map<ClassLoader, Set<Class<?>>> getAllLoadedClassesMap(@Nullable ClassLoader classLoader) throws UnsupportedOperationException {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = new LinkedHashMap();
        Set<ClassLoader> classLoadersSet = doGetInheritableClassLoaders(actualClassLoader);
        for (ClassLoader loader : classLoadersSet) {
            allLoadedClassesMap.put(loader, getLoadedClasses(actualClassLoader));
        }
        return unmodifiableMap(allLoadedClassesMap);
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
    public static Set<Class<?>> getAllLoadedClasses(@Nullable ClassLoader classLoader) throws UnsupportedOperationException {
        Set<Class<?>> allLoadedClassesSet = newLinkedHashSet();
        Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = getAllLoadedClassesMap(classLoader);
        for (Set<Class<?>> loadedClassesSet : allLoadedClassesMap.values()) {
            allLoadedClassesSet.addAll(loadedClassesSet);
        }
        return unmodifiableSet(allLoadedClassesSet);
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
    public static Set<Class<?>> getLoadedClasses(@Nullable ClassLoader classLoader) throws UnsupportedOperationException {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        Field field = findField(classLoaderClass, classesFieldName);
        List<Class<?>> classes = getFieldValue(actualClassLoader, field);
        return classes == null ? emptySet() : ofSet(classes);
    }

    /**
     * Find loaded classes {@link Set} in class path
     *
     * @param classLoader {@link ClassLoader}
     * @return Read-only {@link Set}
     * @throws UnsupportedOperationException If JVM does not support
     */
    public static Set<Class<?>> findLoadedClassesInClassPath(@Nullable ClassLoader classLoader) throws UnsupportedOperationException {
        Set<String> classNames = INSTANCE.getAllClassNamesInClassPaths();
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
    public static Set<Class<?>> findLoadedClassesInClassPaths(@Nullable ClassLoader classLoader, Set<String> classPaths) throws UnsupportedOperationException {
        Set<Class<?>> loadedClasses = newLinkedHashSet();
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
    public static Set<Class<?>> findLoadedClassesInClassPath(@Nullable ClassLoader classLoader, String classPath) throws UnsupportedOperationException {
        Set<String> classNames = INSTANCE.getClassNamesInClassPath(classPath, true);
        return findLoadedClasses(classLoader, classNames);
    }

    /**
     * Test the specified class name is present in the {@link #getDefaultClassLoader() default ClassLoader}
     *
     * @param className the name of {@link Class}
     * @return If found, return <code>true</code>
     */
    public static boolean isPresent(@Nullable String className) {
        return resolveClass(className) != null;
    }

    /**
     * Test the specified class name is present in the {@link ClassLoader}
     *
     * @param className   the name of {@link Class}
     * @param classLoader {@link ClassLoader}
     * @return If found, return <code>true</code>
     */
    public static boolean isPresent(@Nullable String className, @Nullable ClassLoader classLoader) {
        return resolveClass(className, classLoader) != null;
    }

    /**
     * Resolve the {@link Class} by the specified name in the {@link #getDefaultClassLoader() default ClassLoader}
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
        if (isBlank(className)) {
            return null;
        }

        Class<?> targetClass = null;
        try {
            ClassLoader targetClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
            targetClass = loadClass(targetClassLoader, className, cached);
        } catch (Throwable ignored) { // Ignored
        }
        return targetClass;
    }

    public static Set<URL> findAllClassPathURLs(@Nullable ClassLoader classLoader) {

        ClassLoader actualClassLoader = findClassLoader(classLoader);

        Set<URL> allClassPathURLs = newLinkedHashSet();

        URL[] classPathURLs = urlClassPathHandle.getURLs(actualClassLoader);

        addAll(allClassPathURLs, classPathURLs);

        ClassLoader parentClassLoader = actualClassLoader.getParent();
        if (parentClassLoader != null) {
            allClassPathURLs.addAll(findAllClassPathURLs(parentClassLoader));
        }
        return allClassPathURLs;
    }

    public static boolean removeClassPathURL(@Nullable ClassLoader classLoader, URL url) {
        if (!(classLoader instanceof SecureClassLoader)) {
            return false;
        }
        return urlClassPathHandle.removeURL(classLoader, url);
    }

    @Nullable
    public static URLClassLoader findURLClassLoader(@Nullable ClassLoader classLoader) {
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

    /**
     * Create a new instance of {@link URLClassLoader}
     *
     * @param urls the urls
     * @return non-null {@link URLClassLoader}
     * @throws IllegalArgumentException if the <code>urls</code> is null or contains null element
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull Iterable<URL> urls) {
        return newURLClassLoader(urls, null);
    }

    /**
     * Create a new instance of {@link URLClassLoader}
     *
     * @param urls the urls
     * @return non-null {@link URLClassLoader}
     * @throws IllegalArgumentException if the <code>urls</code> is null or contains null element
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull Iterable<URL> urls, @Nullable ClassLoader classLoader) {
        assertNotNull(urls, () -> "The 'urls' must not be null");
        URL[] urlsArray = asArray(urls, URL.class);
        return newURLClassLoader(urlsArray, classLoader);
    }

    /**
     * Create a new instance of {@link URLClassLoader}
     *
     * @param urls the urls
     * @return non-null {@link URLClassLoader}
     * @throws IllegalArgumentException if the <code>urls</code> is null or contains null element
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull URL[] urls) {
        return newURLClassLoader(urls, null);
    }

    /**
     * Create a new instance of {@link URLClassLoader}
     *
     * @param urls               the urls
     * @param initializedLoaders the loaders of URLClassPath will be initialized or not
     * @return non-null {@link URLClassLoader}
     * @throws IllegalArgumentException if the <code>urls</code> is null or contains null element
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull URL[] urls, boolean initializedLoaders) {
        return newURLClassLoader(urls, null, initializedLoaders);
    }

    /**
     * Create a new instance of {@link URLClassLoader}
     *
     * @param urls   the urls
     * @param parent the {@link ClassLoader} as parent
     * @return non-null {@link URLClassLoader}
     * @throws IllegalArgumentException if the <code>urls</code> is null or contains null element
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull URL[] urls, @Nullable ClassLoader parent) throws IllegalArgumentException {
        return newURLClassLoader(urls, parent, false);
    }

    /**
     * Create a new instance of {@link URLClassLoader}
     *
     * @param urls               the urls
     * @param parent             the {@link ClassLoader} as parent
     * @param initializedLoaders the loaders of URLClassPath will be initialized or not
     * @return non-null {@link URLClassLoader}
     * @throws IllegalArgumentException if the <code>urls</code> is null or contains null element
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull URL[] urls, @Nullable ClassLoader parent, boolean initializedLoaders) throws IllegalArgumentException {
        assertNotNull(urls, () -> "The 'urls' must not be null");
        assertNoNullElements(urls, () -> "Any element of 'urls' must not be null");
        URLClassLoader urlClassLoader = new URLClassLoader(urls, parent);
        if (initializedLoaders) {
            urlClassPathHandle.initializeLoaders(urlClassLoader);
        }
        return urlClassLoader;
    }

    /**
     * Try to find the instance of {@link URLClassLoader} from the specified {@link ClassLoader},
     * if it can't be found, it will try to find all the {@link URL} of class-paths from the specified {@link ClassLoader},
     * and then new a instance of {@link URLClassLoader} upon those urls.
     *
     * @param classLoader {@link ClassLoader}
     * @return non-null {@link URLClassLoader}
     */
    @Nonnull
    public static URLClassLoader resolveURLClassLoader(@Nullable ClassLoader classLoader) {
        URLClassLoader urlClassLoader = findURLClassLoader(classLoader);
        if (urlClassLoader == null) {
            Set<URL> urls = findAllClassPathURLs(classLoader);
            urlClassLoader = newURLClassLoader(urls, classLoader);
        }
        return urlClassLoader;
    }

    /**
     * Return the ClassLoader from the caller class
     *
     * @return the ClassLoader (only {@code null} if the caller class was absent
     * @see ReflectionUtils#getCallerClass()
     */
    static ClassLoader getCallerClassLoader(int invocationFrame) {
        ClassLoader classLoader = null;
        Class<?> callerClass = getCallerClass(invocationFrame);
        if (callerClass != null) {
            classLoader = callerClass.getClassLoader();
        }
        return classLoader;
    }

    @Nullable
    static ClassLoader findClassLoader(@Nullable ClassLoader classLoader) {
        return classLoader == null ? getDefaultClassLoader() : classLoader;
    }

    /**
     * Invoke the {@link MethodHandle} of {@link ClassLoader#findLoadedClass(String)}
     *
     * @param classLoader {@link ClassLoader}
     * @param className   the class name
     * @return <code>null</code> if not loaded or can't be loaded
     */
    static Class<?> invokeFindLoadedClassMethod(ClassLoader classLoader, String className) {
        Class<?> loadedClass = null;
        try {
            Method findLoadedClassMethod = findMethod(ClassLoader.class, findLoadedClassMethodName, String.class);
            loadedClass = invokeMethod(classLoader, findLoadedClassMethod, className);
        } catch (Throwable e) {
            logOnFindLoadedClassInvocationFailed(classLoader, className, e);
        }
        return loadedClass;
    }

    static void logOnFindLoadedClassInvocationFailed(ClassLoader classLoader, String className, Throwable e) {
        logger.error("The findLoadedClass(className : '{}' : String) method of java.lang.ClassLoader[{}] can't be invoked in the current JVM[vendor : {} , version : {}]",
                className, classLoader, JAVA_VENDOR, JAVA_VERSION, e);
    }

    static String buildCacheKey(ClassLoader classLoader, String className) {
        String cacheKey = className + classLoader.hashCode();
        return cacheKey;
    }

    /**
     * Resource Type
     */
    public enum ResourceType {

        DEFAULT {
            @Override
            boolean supports(String name) {
                return true;
            }

            @Override
            public String normalize(String name) {
                return name;
            }


        }, CLASS {
            @Override
            boolean supports(String name) {
                return endsWith(name, CLASS_EXTENSION);
            }

            @Override
            public String normalize(String name) {
                if (name == null) {
                    return null;
                }
                int index = name.lastIndexOf(CLASS_EXTENSION);
                String className = index > -1 ? name.substring(0, index) : name;
                className = className.replace(DOT_CHAR, SLASH_CHAR);
                return className + CLASS_EXTENSION;
            }


        }, PACKAGE {
            @Override
            boolean supports(String name) {
                if (name == null) {
                    return false;
                }
                //TODO: use regexp to match more precise
                return !CLASS.supports(name) && !contains(name, SLASH) && !contains(name, BACK_SLASH);
            }

            @Override
            String normalize(String name) {
                if (name == null) {
                    return null;
                }
                String packageName = name.replace(DOT_CHAR, SLASH_CHAR);
                if (!packageName.endsWith(SLASH)) {
                    return packageName + SLASH;
                }
                return packageName;
            }

        };

        /**
         * resolves resource name
         *
         * @param name resource name
         * @return a newly resolved resource name
         */
        public String resolve(String name) {
            if (isBlank(name) || !supports(name)) {
                return null;
            }

            String normalizedName = normalize(name);

            normalizedName = normalizePath(normalizedName);

            // Remove the character "/" in the start of String if found
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
        abstract boolean supports(String name);

        /**
         * Normalizes resource name
         *
         * @param name resource name
         * @return normalized resource name
         */
        abstract String normalize(String name);

    }
}