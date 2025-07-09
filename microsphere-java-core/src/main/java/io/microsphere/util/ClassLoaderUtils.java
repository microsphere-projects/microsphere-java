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
import java.io.InputStream;
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

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.constants.PathConstants.BACK_SLASH;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.io.IOUtils.copyToString;
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
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int loadedClassCount = ClassLoaderUtils.getLoadedClassCount();
     * System.out.println("Currently loaded classes: " + loadedClassCount);
     * }</pre>
     *
     * @return the number of currently loaded classes.
     */
    public static int getLoadedClassCount() {
        return classLoadingMXBean.getLoadedClassCount();
    }

    /**
     * Returns the total number of classes that have been unloaded since the Java virtual machine has started execution.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long unloadedClassCount = ClassLoaderUtils.getUnloadedClassCount();
     * System.out.println("Total unloaded classes: " + unloadedClassCount);
     * }</pre>
     *
     * @return the total number of classes unloaded.
     */
    public static long getUnloadedClassCount() {
        return classLoadingMXBean.getUnloadedClassCount();
    }

    /**
     * Returns the total number of classes that have been loaded since the Java virtual machine has started execution.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * long totalLoadedClassCount = ClassLoaderUtils.getTotalLoadedClassCount();
     * System.out.println("Total loaded classes: " + totalLoadedClassCount);
     * }</pre>
     *
     * @return the total number of classes loaded.
     */
    public static long getTotalLoadedClassCount() {
        return classLoadingMXBean.getTotalLoadedClassCount();
    }

    /**
     * Checks if the Java virtual machine is currently running with verbose class loading output enabled.
     *
     * <p>
     * This method delegates to the underlying JVM's {@link ClassLoadingMXBean#isVerbose()} to determine whether verbose mode is active.
     * When verbose mode is enabled, the JVM typically prints a message each time a class file is loaded.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean isVerbose = ClassLoaderUtils.isVerbose();
     * if (isVerbose) {
     *     System.out.println("JVM is running in verbose class loading mode.");
     * } else {
     *     System.out.println("Verbose class loading mode is disabled.");
     * }
     * }</pre>
     *
     * @return <tt>true</tt> if the JVM is running with verbose class loading output enabled; <tt>false</tt> otherwise.
     */
    public static boolean isVerbose() {
        return classLoadingMXBean.isVerbose();
    }

    /**
     * Enables or disables the verbose output for the class loading system.
     *
     * <p>
     * When verbose mode is enabled, the JVM typically prints a message each time a class file is loaded.
     * The verbose output information and the output stream to which it is emitted are implementation-dependent.
     * This method can be called by multiple threads concurrently. Each invocation enables or disables the verbose output globally.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Enable verbose class loading output
     * ClassLoaderUtils.setVerbose(true);
     *
     * // Disable verbose class loading output
     * ClassLoaderUtils.setVerbose(false);
     * }</pre>
     *
     * @param value <tt>true</tt> to enable verbose output; <tt>false</tt> to disable it.
     * @throws SecurityException if a security manager exists and the caller does not have the required {@link java.lang.management.ManagementPermission} with "control" action.
     */
    public static void setVerbose(boolean value) {
        classLoadingMXBean.setVerbose(value);
    }

    /**
     * Retrieves the default ClassLoader to use when none is explicitly provided.
     * This method attempts to find the most appropriate ClassLoader by checking:
     * <ol>
     *   <li>The context ClassLoader of the current thread</li>
     *   <li>If that's not available, the ClassLoader that loaded this class ({@link ClassLoaderUtils})</li>
     *   <li>Finally, if neither is available, it falls back to the system ClassLoader</li>
     * </ol>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
     * if (classLoader != null) {
     *     System.out.println("Using default ClassLoader: " + classLoader);
     * } else {
     *     System.out.println("No suitable ClassLoader found.");
     * }
     * }</pre>
     *
     * @return the default ClassLoader, or {@code null} if no suitable ClassLoader could be determined
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
     * <p>If the provided {@code loadedClass} is null, this method attempts to find the ClassLoader of the caller class using
     * the specified stack frame depth. If that fails, it falls back to the system ClassLoader.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Case 1: Get ClassLoader from a known class
     * ClassLoader classLoader = ClassLoaderUtils.getClassLoader(MyClass.class);
     * System.out.println("ClassLoader for MyClass: " + classLoader);
     * }</pre>
     *
     * <pre>{@code
     * // Case 2: Get ClassLoader when no class is provided (uses caller's ClassLoader)
     * ClassLoader classLoader = ClassLoaderUtils.getClassLoader(null);
     * System.out.println("Default ClassLoader: " + classLoader);
     * }</pre>
     *
     * @param loadedClass the optional class was loaded by some {@link ClassLoader}
     * @return the ClassLoader (only {@code null} if even the system ClassLoader isn't accessible)
     * @see #getCallerClassLoader()
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
     * Retrieves the {@link ClassLoader} of the caller class from the call stack.
     *
     * <p>This method is a convenience wrapper that determines the caller's class by analyzing the call stack,
     * and then returns the associated ClassLoader for that class. It uses a fixed stack frame depth of 4
     * to identify the caller, which is suitable for most direct usage scenarios.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader callerClassLoader = ClassLoaderUtils.getCallerClassLoader();
     * if (callerClassLoader != null) {
     *     System.out.println("Caller ClassLoader: " + callerClassLoader);
     * } else {
     *     System.out.println("Caller ClassLoader is not available.");
     * }
     * }</pre>
     *
     * @return the ClassLoader of the caller class, or {@code null} if it cannot be determined
     * @see ReflectionUtils#getCallerClass()
     */
    @Nullable
    public static ClassLoader getCallerClassLoader() {
        return getCallerClassLoader(4);
    }

    /**
     * Finds and returns a set of loaded classes for the given class names using the specified ClassLoader.
     *
     * <p>If the provided ClassLoader is null, it will use the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Find loaded classes with a specific ClassLoader
     * ClassLoader classLoader = MyClass.class.getClassLoader();
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClasses(classLoader, "com.example.ClassA", "com.example.ClassB");
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to find loaded classes
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClasses(null, "com.example.ClassC", "com.example.ClassD");
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for loading classes, may be null
     * @param classNames  the array of class names to check if they are already loaded
     * @return a set of loaded classes corresponding to the provided class names; the set will be empty if none of the classes are loaded
     */
    @Nonnull
    public static Set<Class<?>> findLoadedClasses(@Nullable ClassLoader classLoader, String... classNames) {
        return findLoadedClasses(classLoader, ofSet(classNames));
    }

    /**
     * Finds and returns a set of loaded classes for the given class names using the specified ClassLoader.
     *
     * <p>If the provided ClassLoader is null, it will use the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Find loaded classes with a specific ClassLoader
     * ClassLoader classLoader = MyClass.class.getClassLoader();
     * Iterable<String> classNames = Arrays.asList("com.example.ClassA", "com.example.ClassB");
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClasses(classLoader, classNames);
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to find loaded classes
     * Iterable<String> classNames = Arrays.asList("com.example.ClassC", "com.example.ClassD");
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClasses(null, classNames);
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for loading classes, may be null
     * @param classNames  the iterable collection of class names to check if they are already loaded
     * @return a set of loaded classes corresponding to the provided class names; the set will be empty if none of the classes are loaded
     */
    @Nonnull
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
     * Checks if the specified class is already loaded by the given ClassLoader or its parent hierarchy.
     *
     * <p>If the provided {@link ClassLoader} is null, it will use the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Check if a class is loaded using a specific ClassLoader
     * ClassLoader classLoader = MyClass.class.getClassLoader();
     * boolean isLoaded = ClassLoaderUtils.isLoadedClass(classLoader, MyClass.class);
     * System.out.println("Is MyClass loaded? " + isLoaded);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to check if a class is loaded
     * boolean isLoaded = ClassLoaderUtils.isLoadedClass(null, SomeClass.class);
     * System.out.println("Is SomeClass loaded? " + isLoaded);
     * }</pre>
     *
     * @param classLoader the ClassLoader to check, may be null
     * @param type        the Class object representing the class to check
     * @return true if the class is already loaded; false otherwise
     */
    public static boolean isLoadedClass(@Nullable ClassLoader classLoader, Class<?> type) {
        return isLoadedClass(classLoader, type.getName());
    }

    /**
     * Checks if the specified class name is already loaded by the given ClassLoader or its parent hierarchy.
     *
     * <p>
     * If the provided {@link ClassLoader} is null, it will use the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Check if a class is loaded using a specific ClassLoader
     * ClassLoader classLoader = MyClass.class.getClassLoader();
     * boolean isLoaded = ClassLoaderUtils.isLoadedClass(classLoader, "com.example.MyClass");
     * System.out.println("Is com.example.MyClass loaded? " + isLoaded);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to check if a class is loaded
     * boolean isLoaded = ClassLoaderUtils.isLoadedClass(null, "com.example.SomeClass");
     * System.out.println("Is com.example.SomeClass loaded? " + isLoaded);
     * }</pre>
     *
     * @param classLoader the ClassLoader to check, may be null
     * @param className   the fully qualified name of the class to check
     * @return true if the class is already loaded; false otherwise
     */
    public static boolean isLoadedClass(@Nullable ClassLoader classLoader, String className) {
        return findLoadedClass(classLoader, className) != null;
    }

    /**
     * Finds a class that has already been loaded by the given {@link ClassLoader} or its parent hierarchy.
     *
     * <p>If the provided {@link ClassLoader} is null, it will use the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Find a loaded class using a specific ClassLoader
     * ClassLoader classLoader = MyClass.class.getClassLoader();
     * String className = "com.example.MyClass";
     * Class<?> loadedClass = ClassLoaderUtils.findLoadedClass(classLoader, className);
     * if (loadedClass != null) {
     *     System.out.println(className + " is already loaded.");
     * } else {
     *     System.out.println(className + " is not loaded yet.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to find a loaded class
     * String className = "com.example.SomeClass";
     * Class<?> loadedClass = ClassLoaderUtils.findLoadedClass(null, className);
     * if (loadedClass != null) {
     *     System.out.println(className + " is already loaded.");
     * } else {
     *     System.out.println(className + " is not loaded yet.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to check, may be null
     * @param className   the fully qualified name of the class to check
     * @return the {@link Class} object if the class is already loaded; null otherwise
     */
    @Nullable
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
     * Loads the class with the specified name using the provided ClassLoader or the default ClassLoader if none is specified.
     *
     * <p>
     * This method attempts to load the class using the given ClassLoader. If the ClassLoader is null, it uses the default one determined by
     * {@link #getDefaultClassLoader()}. The actual loading is delegated to the {@link ClassLoader#loadClass(String)} method.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Load a class using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Class<?> loadedClass = ClassLoaderUtils.loadClass(customClassLoader, "com.example.MyClass");
     * if (loadedClass != null) {
     *     System.out.println("Class loaded successfully: " + loadedClass.getName());
     * } else {
     *     System.out.println("Failed to load class.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to load a class
     * Class<?> loadedClass = ClassLoaderUtils.loadClass(null, "com.example.AnotherClass");
     * if (loadedClass != null) {
     *     System.out.println("Class loaded successfully: " + loadedClass.getName());
     * } else {
     *     System.out.println("Failed to load class.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for loading the class, may be null
     * @param className   the fully qualified name of the class to load
     * @return the loaded Class object if successful, or null if the class could not be found or loaded
     */
    @Nullable
    public static Class<?> loadClass(@Nullable ClassLoader classLoader, @Nullable String className) {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        return doLoadClass(actualClassLoader, className);
    }

    /**
     * Loads the class with the specified name using the provided ClassLoader or the default ClassLoader if none is specified,
     * optionally caching the result for faster subsequent lookups.
     *
     * <p>
     * This method attempts to load the class using the given ClassLoader. If the ClassLoader is null, it uses the default one determined by
     * {@link #getDefaultClassLoader()}. The actual loading is delegated to the {@link ClassLoader#loadClass(String)} method.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Load a class using a specific ClassLoader without caching
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Class<?> loadedClass = ClassLoaderUtils.loadClass(customClassLoader, "com.example.MyClass", false);
     * if (loadedClass != null) {
     *     System.out.println("Class loaded successfully: " + loadedClass.getName());
     * } else {
     *     System.out.println("Failed to load class.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to load a class with caching enabled
     * Class<?> loadedClass = ClassLoaderUtils.loadClass(null, "com.example.AnotherClass", true);
     * if (loadedClass != null) {
     *     System.out.println("Class loaded successfully: " + loadedClass.getName());
     * } else {
     *     System.out.println("Failed to load class.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for loading the class, may be null
     * @param className   the fully qualified name of the class to load
     * @param cached      whether to cache the loaded class for faster subsequent lookups
     * @return the loaded Class object if successful, or null if the class could not be found or loaded
     */
    @Nullable
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
     * Retrieves a set of URLs representing resources with the specified type and name using the provided ClassLoader.
     *
     * <p>If the given ClassLoader is null, this method uses the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Get all URLs for a specific resource using a custom ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * ResourceType resourceType = ResourceType.CLASS;
     * String resourceName = "com.example.MyClass.class";
     * Set<URL> resourceURLs = ClassLoaderUtils.getResources(customClassLoader, resourceType, resourceName);
     * System.out.println("Resource URLs: " + resourceURLs);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get URLs for a resource
     * ResourceType resourceType = ResourceType.PACKAGE;
     * String resourceName = "com.example";
     * Set<URL> resourceURLs = ClassLoaderUtils.getResources(null, resourceType, resourceName);
     * System.out.println("Resource URLs: " + resourceURLs);
     * }</pre>
     *
     * @param classLoader  the ClassLoader to use for locating resources; may be null
     * @param resourceType the type of resource to locate (e.g., CLASS, PACKAGE); see {@link ResourceType}
     * @param resourceName the name of the resource to locate
     * @return a set of URLs pointing to the resources found; returns an empty set if none are found
     * @throws NullPointerException if the resourceName is null
     * @throws IOException          if I/O errors occur while searching for resources
     */
    @Nonnull
    public static Set<URL> getResources(@Nullable ClassLoader classLoader, @Nonnull ResourceType resourceType, String resourceName) throws NullPointerException, IOException {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        String normalizedResourceName = resourceType.resolve(resourceName);
        Enumeration<URL> resources = actualClassLoader.getResources(normalizedResourceName);
        return resources != null && resources.hasMoreElements() ? ofSet(resources) : emptySet();
    }

    /**
     * Retrieves a set of URLs representing resources with the specified name using the provided ClassLoader.
     *
     * <p>This method attempts to locate resources across all known {@link ResourceType} categories (e.g., CLASS, PACKAGE).
     * It iterates through each resource type and returns the first non-empty set of URLs it finds. If no matching resources
     * are found for any type, an empty set is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Get all URLs for a specific resource using a custom ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String resourceName = "com.example.MyClass.class";
     * Set<URL> resourceURLs = ClassLoaderUtils.getResources(customClassLoader, resourceName);
     * System.out.println("Resource URLs: " + resourceURLs);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get URLs for a resource
     * String resourceName = "com.example";
     * Set<URL> resourceURLs = ClassLoaderUtils.getResources(null, resourceName);
     * System.out.println("Resource URLs: " + resourceURLs);
     * }</pre>
     *
     * @param classLoader  the ClassLoader to use for locating resources; may be null
     * @param resourceName the name of the resource to locate
     * @return a set of URLs pointing to the resources found; returns an empty set if none are found
     * @throws NullPointerException if the resourceName is null
     * @throws IOException          if I/O errors occur while searching for resources
     */
    @Nonnull
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
     * Get the resource URL under specified resource name using the default ClassLoader.
     *
     * <p>
     * This method attempts to locate a resource with the given name by checking various resource types (e.g., CLASS, PACKAGE)
     * and returns the first matching URL it finds. If no matching resource is found, it returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Retrieve a resource URL using the default ClassLoader
     * URL resourceUrl = ClassLoaderUtils.getResource("com.example.MyClass.class");
     * if (resourceUrl != null) {
     *     System.out.println("Resource found at: " + resourceUrl);
     * } else {
     *     System.out.println("Resource not found.");
     * }
     * }</pre>
     *
     * @param resourceName the name of the resource to locate
     * @return the resource URL under the specified resource name, or {@code null} if not found
     * @throws NullPointerException if the resourceName is {@code null}
     */
    @Nullable
    public static URL getResource(String resourceName) throws NullPointerException {
        return getResource(null, resourceName);
    }

    /**
     * Retrieves a resource URL for the specified resource name using the provided ClassLoader.
     *
     * <p>This method attempts to locate a resource by checking various resource types (e.g., CLASS, PACKAGE)
     * and returns the first matching URL it finds. If no matching resource is found, it returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Retrieve a resource URL using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String resourceName = "com.example.MyClass.class";
     * URL resourceUrl = ClassLoaderUtils.getResource(customClassLoader, resourceName);
     * if (resourceUrl != null) {
     *     System.out.println("Resource found at: " + resourceUrl);
     * } else {
     *     System.out.println("Resource not found.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to retrieve a resource URL
     * String resourceName = "com.example.SomeResource.txt";
     * URL resourceUrl = ClassLoaderUtils.getResource(null, resourceName);
     * if (resourceUrl != null) {
     *     System.out.println("Resource found at: " + resourceUrl);
     * } else {
     *     System.out.println("Resource not found.");
     * }
     * }</pre>
     *
     * @param classLoader  the ClassLoader to use for locating resources; may be null
     * @param resourceName the name of the resource to locate
     * @return the resource URL under the specified resource name, or {@code null} if not found
     * @throws NullPointerException if the resourceName is {@code null}
     */
    @Nullable
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
     * Retrieves a resource URL for the specified resource name using the provided ClassLoader and resource type.
     *
     * <p>This method attempts to locate a resource by resolving the resourceName according to the given {@link ResourceType}.
     * If the ClassLoader is null, it uses the default ClassLoader determined by {@link #getDefaultClassLoader()}.
     * The resolved resource name is normalized and searched through the provided ClassLoader's {@link ClassLoader#getResource(String)} method.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Retrieve a class resource using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String className = "com.example.MyClass.class";
     * URL resourceUrl = ClassLoaderUtils.getResource(customClassLoader, ResourceType.CLASS, className);
     * if (resourceUrl != null) {
     *     System.out.println("Resource found at: " + resourceUrl);
     * } else {
     *     System.out.println("Resource not found.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to retrieve a package resource
     * String packageName = "com.example";
     * URL resourceUrl = ClassLoaderUtils.getResource(null, ResourceType.PACKAGE, packageName);
     * if (resourceUrl != null) {
     *     System.out.println("Resource found at: " + resourceUrl);
     * } else {
     *     System.out.println("Resource not found.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 3: Use the default ClassLoader to retrieve a general resource
     * String resourceName = "config.properties";
     * URL resourceUrl = ClassLoaderUtils.getResource(null, ResourceType.DEFAULT, resourceName);
     * if (resourceUrl != null) {
     *     System.out.println("Resource found at: " + resourceUrl);
     * } else {
     *     System.out.println("Resource not found.");
     * }
     * }</pre>
     *
     * @param classLoader  the ClassLoader to use for locating resources; may be null
     * @param resourceType the type of resource to locate (e.g., CLASS, PACKAGE); see {@link ResourceType}
     * @param resourceName the name of the resource to locate
     * @return the resource URL under the specified resource name and type, or {@code null} if not found
     * @throws NullPointerException if the resourceName is {@code null}
     */
    @Nullable
    public static URL getResource(@Nullable ClassLoader classLoader, ResourceType resourceType, String resourceName) throws NullPointerException {
        String normalizedResourceName = resourceType.resolve(resourceName);
        URL resource = normalizedResourceName == null ? null : findClassLoader(classLoader).getResource(normalizedResourceName);
        if (logger.isTraceEnabled()) {
            logger.trace("To find the resource[name : '{}' , normalized : '{}' , type = {} , ClassLoader : {}] : {}",
                    resourceName, normalizedResourceName, resourceType, classLoader, resource);
        }
        return resource;
    }

    /**
     * Gets the content of the specified resource as a String using the default ClassLoader.
     *
     * <p>This method attempts to locate the resource using the default ClassLoader determined by
     * {@link #getDefaultClassLoader()} and reads its content into a String. If the resource cannot be found or read,
     * an exception is thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try {
     *     String content = ClassLoaderUtils.getResourceAsString("example.txt");
     *     System.out.println("Resource content: " + content);
     * } catch (IOException e) {
     *     System.err.println("Failed to read resource: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param resourceName the name of the resource to load
     * @return the content of the resource as a String
     * @throws NullPointerException if the resourceName is null
     * @throws IOException          if an I/O error occurs while reading the resource
     */
    @Nullable
    public static String getResourceAsString(String resourceName) throws NullPointerException, IOException {
        return getResourceAsString(null, resourceName);
    }

    /**
     * Gets the content of the specified resource as a String using the provided ClassLoader.
     *
     * <p>This method attempts to locate the resource using the given ClassLoader and reads its content into a String.
     * If the resource cannot be found or read, an exception is thrown. If the provided ClassLoader is null, it uses the
     * default ClassLoader determined by {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try {
     *     ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     *     String content = ClassLoaderUtils.getResourceAsString(customClassLoader, "example.txt");
     *     System.out.println("Resource content: " + content);
     * } catch (IOException e) {
     *     System.err.println("Failed to read resource: " + e.getMessage());
     * }
     * }</pre>
     *
     * <pre>{@code
     * try {
     *     // Use the default ClassLoader
     *     String content = ClassLoaderUtils.getResourceAsString(null, "config.properties");
     *     System.out.println("Configuration content: " + content);
     * } catch (IOException e) {
     *     System.err.println("Failed to read resource: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param classLoader  the ClassLoader to use for locating resources; may be null
     * @param resourceName the name of the resource to locate
     * @return the content of the resource as a String, or null if the resource could not be found
     * @throws NullPointerException if the resourceName is null
     * @throws IOException          if an I/O error occurs while reading the resource
     */
    @Nullable
    public static String getResourceAsString(@Nullable ClassLoader classLoader, String resourceName) throws NullPointerException, IOException {
        URL resource = getResource(classLoader, resourceName);
        if (resource == null) {
            return null;
        }
        final String content;
        try (InputStream inputStream = resource.openStream()) {
            content = copyToString(inputStream);
        }
        return content;
    }

    /**
     * Retrieves the resource URL for the class file corresponding to the specified class name.
     *
     * <p>This method resolves the resource path based on the fully qualified name of the class,
     * appending the ".class" extension, and locating it through the provided ClassLoader. If the
     * ClassLoader is null, the default ClassLoader determined by
     * {@link #getDefaultClassLoader()} will be used.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Get the resource URL using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String className = "com.example.MyClass";
     * URL classResourceUrl = ClassLoaderUtils.getClassResource(customClassLoader, className);
     * if (classResourceUrl != null) {
     *     System.out.println("Found class resource at: " + classResourceUrl);
     * } else {
     *     System.out.println("Class resource not found.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get the class resource
     * String className = "com.example.AnotherClass";
     * URL classResourceUrl = ClassLoaderUtils.getClassResource(null, className);
     * if (classResourceUrl != null) {
     *     System.out.println("Found class resource at: " + classResourceUrl);
     * } else {
     *     System.out.println("Class resource not found.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for locating the class resource; may be null
     * @param className   the fully qualified name of the class for which the resource URL is to be retrieved
     * @return the URL to the class file resource, or {@code null} if not found
     * @throws NullPointerException if the className is null
     */
    public static URL getClassResource(@Nullable ClassLoader classLoader, String className) {
        final String resourceName = className + CLASS_EXTENSION;
        return getResource(classLoader, ResourceType.CLASS, resourceName);
    }

    /**
     * Returns the resource URL for the class file corresponding to the specified class.
     *
     * <p>This method uses the class loader of the given class to locate its corresponding class file resource.
     * It constructs the resource path based on the fully qualified name of the class, appending the ".class" extension,
     * and resolving it according to the rules defined by the underlying class loader.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example: Get the resource URL for a specific class
     * Class<?> myClass = MyClass.class;
     * URL classResourceUrl = ClassLoaderUtils.getClassResource(myClass);
     * if (classResourceUrl != null) {
     *     System.out.println("Found class resource at: " + classResourceUrl);
     * } else {
     *     System.out.println("Class resource not found.");
     * }
     * }</pre>
     *
     * @param type the class for which the resource URL is to be retrieved
     * @return the URL to the class file resource, or {@code null} if not found
     * @throws NullPointerException if the provided class is {@code null}
     */
    @Nullable
    public static URL getClassResource(Class<?> type) {
        return getClassResource(getClassLoader(type), type);
    }

    /**
     * Get the {@link Class} resource URL under specified {@link Class}
     *
     * <p>This method constructs the resource path based on the fully qualified name of the class,
     * appending the ".class" extension, and resolving it according to the rules defined by the
     * underlying class loader. If the provided ClassLoader is null, it uses the default ClassLoader
     * determined by {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Get the resource URL using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Class<?> myClass = MyClass.class;
     * URL classResourceUrl = ClassLoaderUtils.getClassResource(customClassLoader, myClass);
     * if (classResourceUrl != null) {
     *     System.out.println("Found class resource at: " + classResourceUrl);
     * } else {
     *     System.out.println("Class resource not found.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get the class resource
     * Class<?> anotherClass = AnotherClass.class;
     * URL classResourceUrl = ClassLoaderUtils.getClassResource(null, anotherClass);
     * if (classResourceUrl != null) {
     *     System.out.println("Found class resource at: " + classResourceUrl);
     * } else {
     *     System.out.println("Class resource not found.");
     * }
     * }</pre>
     *
     * @param classLoader ClassLoader
     * @param type        {@link Class type}
     * @return the resource URL under specified resource name and type
     * @throws NullPointerException If <code>type</code> argument is <code>null</code>
     */
    @Nullable
    public static URL getClassResource(@Nullable ClassLoader classLoader, Class<?> type) {
        String resourceName = type.getName();
        return getClassResource(classLoader, resourceName);
    }

    /**
     * Retrieves a set of inheritable ClassLoader instances starting from the specified ClassLoader,
     * including its parent hierarchy. This method ensures that all ClassLoaders in the chain are included.
     *
     * <p>
     * If the provided ClassLoader is null, the default ClassLoader determined by
     * {@link #getDefaultClassLoader()} will be used as the starting point.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Get inheritable ClassLoaders from a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Set<ClassLoader> inheritableLoaders = ClassLoaderUtils.getInheritableClassLoaders(customClassLoader);
     * System.out.println("Inheritable ClassLoaders: " + inheritableLoaders);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get inheritable ClassLoaders
     * Set<ClassLoader> inheritableLoaders = ClassLoaderUtils.getInheritableClassLoaders(null);
     * System.out.println("Inheritable ClassLoaders: " + inheritableLoaders);
     * }</pre>
     *
     * @param classLoader the starting ClassLoader to retrieve inheritable ClassLoaders from; may be null
     * @return an unmodifiable set of ClassLoader instances representing the inheritance chain
     * @throws NullPointerException if the resolved ClassLoader (from parameter or default) is null
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
     * Retrieves a map of ClassLoader instances to their corresponding sets of loaded classes.
     * This method traverses the inheritance hierarchy of the provided ClassLoader (or uses the default ClassLoader if none is specified)
     * and collects all classes loaded by each ClassLoader in the hierarchy.
     *
     * <p>If the provided ClassLoader is null, this method uses the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Get loaded classes map using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Map<ClassLoader, Set<Class<?>>> loadedClassesMap = ClassLoaderUtils.getAllLoadedClassesMap(customClassLoader);
     * for (Map.Entry<ClassLoader, Set<Class<?>>> entry : loadedClassesMap.entrySet()) {
     *     System.out.println("ClassLoader: " + entry.getKey());
     *     System.out.println("Loaded Classes: " + entry.getValue());
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get the loaded classes map
     * Map<ClassLoader, Set<Class<?>>> loadedClassesMap = ClassLoaderUtils.getAllLoadedClassesMap(null);
     * for (Map.Entry<ClassLoader, Set<Class<?>>> entry : loadedClassesMap.entrySet()) {
     *     System.out.println("ClassLoader: " + entry.getKey());
     *     System.out.println("Loaded Classes: " + entry.getValue());
     * }
     * }</pre>
     *
     * @param classLoader the starting ClassLoader to retrieve loaded classes from; may be null
     * @return an unmodifiable map where keys are ClassLoader instances and values are sets of loaded classes
     * @throws UnsupportedOperationException if the JVM does not support introspection of loaded classes
     * @throws NullPointerException          if the resolved ClassLoader (from parameter or default) is null
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
     * Retrieves a set of all classes that have been loaded by the specified ClassLoader and its parent hierarchy.
     *
     * <p>
     * This method aggregates all classes loaded by each ClassLoader in the inheritance chain starting from
     * the provided ClassLoader. If the provided ClassLoader is null, it uses the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Get all loaded classes using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.getAllLoadedClasses(customClassLoader);
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get all loaded classes
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.getAllLoadedClasses(null);
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * @param classLoader the ClassLoader to start retrieving loaded classes from; may be null
     * @return an unmodifiable set containing all classes loaded by the specified ClassLoader and its parents
     * @throws UnsupportedOperationException if the JVM does not support introspection of loaded classes
     * @see #getAllLoadedClassesMap(ClassLoader)
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
     * Retrieves the set of classes that have been loaded by the specified ClassLoader.
     *
     * <p>
     * This method accesses the internal list of loaded classes maintained by the ClassLoader. Note that not all JVM implementations
     * expose this list, and in such cases, an {@link UnsupportedOperationException} may be thrown.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Get loaded classes from a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.getLoadedClasses(customClassLoader);
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get loaded classes
     * Set<Class<?>> loadedClasses = ClassLoaderUtils.getLoadedClasses(null);
     * System.out.println("Loaded Classes: " + loadedClasses);
     * }</pre>
     *
     * @param classLoader the ClassLoader to retrieve loaded classes from; may be null
     * @return a read-only set containing the classes loaded by the specified ClassLoader
     * @throws UnsupportedOperationException if the JVM does not support introspection of loaded classes
     * @throws NullPointerException          if the resolved ClassLoader (from parameter or default) is null
     */
    @Nonnull
    public static Set<Class<?>> getLoadedClasses(@Nullable ClassLoader classLoader) throws UnsupportedOperationException {
        ClassLoader actualClassLoader = findClassLoader(classLoader);
        Field field = findField(classLoaderClass, classesFieldName);
        List<Class<?>> classes = getFieldValue(actualClassLoader, field);
        return classes == null ? emptySet() : ofSet(classes);
    }

    /**
     * Retrieves a set of classes that have been loaded from the class path using the specified ClassLoader or the default one.
     *
     * <p>This method finds all class names present in the class paths and then attempts to locate and return the corresponding
     * loaded classes. If no ClassLoader is provided, it uses the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Find loaded classes using the default ClassLoader
     * try {
     *     Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClassesInClassPath(null);
     *     System.out.println("Loaded classes from class path: " + loadedClasses);
     * } catch (UnsupportedOperationException e) {
     *     System.err.println("Operation not supported by the current JVM.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Find loaded classes using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * try {
     *     Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClassesInClassPath(customClassLoader);
     *     System.out.println("Loaded classes from class path: " + loadedClasses);
     * } catch (UnsupportedOperationException e) {
     *     System.err.println("Operation not supported by the current JVM.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for finding loaded classes; may be null
     * @return an unmodifiable set of classes loaded from the class path
     * @throws UnsupportedOperationException if the JVM does not support introspection of loaded classes
     */
    @Nonnull
    public static Set<Class<?>> findLoadedClassesInClassPath(@Nullable ClassLoader classLoader) throws UnsupportedOperationException {
        Set<String> classNames = INSTANCE.getAllClassNamesInClassPaths();
        return findLoadedClasses(classLoader, classNames);
    }

    /**
     * Finds and returns a set of classes that have been loaded from the specified class paths using the provided ClassLoader.
     *
     * <p>This method iterates through each class path in the given set and attempts to locate and load the corresponding
     * classes using the specified ClassLoader. If a class is found, it is added to the resulting set of loaded classes.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Find loaded classes from specific class paths using a custom ClassLoader
     * Set<String> classPaths = new HashSet<>(Arrays.asList("path/to/classes", "lib/dependency.jar"));
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * try {
     *     Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClassesInClassPaths(customClassLoader, classPaths);
     *     System.out.println("Loaded Classes: " + loadedClasses);
     * } catch (UnsupportedOperationException e) {
     *     System.err.println("Operation not supported by the current JVM.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to find loaded classes from specific class paths
     * Set<String> classPaths = new HashSet<>(Arrays.asList("com/example/app", "libs/utils.jar"));
     * try {
     *     Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClassesInClassPaths(null, classPaths);
     *     System.out.println("Loaded Classes: " + loadedClasses);
     * } catch (UnsupportedOperationException e) {
     *     System.err.println("Operation not supported by the current JVM.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for finding loaded classes; may be null
     * @param classPaths  the set of class paths (directories or JAR files) to search for classes
     * @return an unmodifiable set containing all classes loaded from the specified class paths
     * @throws UnsupportedOperationException if the JVM does not support introspection of loaded classes
     */
    @Nonnull
    public static Set<Class<?>> findLoadedClassesInClassPaths(@Nullable ClassLoader classLoader, Set<String> classPaths) throws UnsupportedOperationException {
        Set<Class<?>> loadedClasses = newLinkedHashSet();
        for (String classPath : classPaths) {
            loadedClasses.addAll(findLoadedClassesInClassPath(classLoader, classPath));
        }
        return loadedClasses;
    }

    /**
     * Retrieves a set of classes that have been loaded from the specified class path using the provided ClassLoader.
     *
     * <p>This method finds all class names present in the given class path and attempts to locate and return the corresponding
     * loaded classes using the specified ClassLoader. If no ClassLoader is provided, it uses the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Find loaded classes from a specific class path using a custom ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String classPath = "path/to/classes";
     * try {
     *     Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClassesInClassPath(customClassLoader, classPath);
     *     System.out.println("Loaded Classes: " + loadedClasses);
     * } catch (UnsupportedOperationException e) {
     *     System.err.println("Operation not supported by the current JVM.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to find loaded classes from a class path
     * String classPath = "com/example/app";
     * try {
     *     Set<Class<?>> loadedClasses = ClassLoaderUtils.findLoadedClassesInClassPath(null, classPath);
     *     System.out.println("Loaded Classes: " + loadedClasses);
     * } catch (UnsupportedOperationException e) {
     *     System.err.println("Operation not supported by the current JVM.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to use for finding loaded classes; may be null
     * @param classPath   the class path (directory or JAR file) to search for classes
     * @return an unmodifiable set containing all classes loaded from the specified class path
     * @throws UnsupportedOperationException if the JVM does not support introspection of loaded classes
     */
    @Nonnull
    public static Set<Class<?>> findLoadedClassesInClassPath(@Nullable ClassLoader classLoader, String classPath) throws UnsupportedOperationException {
        Set<String> classNames = INSTANCE.getClassNamesInClassPath(classPath, true);
        return findLoadedClasses(classLoader, classNames);
    }

    /**
     * Checks if a class with the specified name is present in the default ClassLoader.
     *
     * <p>
     * This method uses the default ClassLoader determined by {@link #getDefaultClassLoader()}
     * to check whether the class exists and can be resolved. If the class name is blank or
     * resolution fails, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean present = ClassLoaderUtils.isPresent("java.lang.String");
     * System.out.println("Is java.lang.String present? " + present);
     * }</pre>
     *
     * @param className the fully qualified name of the class to check
     * @return {@code true} if the class is present and can be resolved; {@code false} otherwise
     */
    public static boolean isPresent(@Nullable String className) {
        return resolveClass(className) != null;
    }

    /**
     * Checks if a class with the specified name is present in the given ClassLoader or the default ClassLoader.
     *
     * <p>
     * This method attempts to resolve the class using the provided ClassLoader. If the ClassLoader is null,
     * it uses the default ClassLoader determined by {@link #getDefaultClassLoader()}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Check if a class is present using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * boolean present = ClassLoaderUtils.isPresent("com.example.MyClass", customClassLoader);
     * System.out.println("Is com.example.MyClass present? " + present);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to check if a class is present
     * boolean present = ClassLoaderUtils.isPresent("java.lang.String", null);
     * System.out.println("Is java.lang.String present? " + present);
     * }</pre>
     *
     * @param className   the fully qualified name of the class to check
     * @param classLoader the ClassLoader to use for checking presence, may be null
     * @return true if the class is present and can be resolved; false otherwise
     */
    public static boolean isPresent(@Nullable String className, @Nullable ClassLoader classLoader) {
        return resolveClass(className, classLoader) != null;
    }

    /**
     * Resolves a class by its name using the default ClassLoader.
     *
     * <p>This method attempts to load and return the {@link Class} object for the specified class name.
     * If the class cannot be found or loaded, it returns {@code null}.
     * The default ClassLoader is determined by {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example: Resolve a class using the default ClassLoader
     * String className = "java.lang.String";
     * Class<?> resolvedClass = ClassLoaderUtils.resolveClass(className);
     * if (resolvedClass != null) {
     *     System.out.println("Class resolved: " + resolvedClass.getName());
     * } else {
     *     System.out.println("Class not found or could not be loaded.");
     * }
     * }</pre>
     *
     * @param className the fully qualified name of the class to resolve
     * @return the resolved Class object if successful; {@code null} if the class cannot be found or loaded
     */
    @Nullable
    public static Class<?> resolveClass(@Nullable String className) {
        return resolveClass(className, getDefaultClassLoader());
    }

    /**
     * Resolves a class by its name using the provided ClassLoader or the default ClassLoader if none is specified.
     *
     * <p>This method attempts to load and return the {@link Class} object for the specified class name.
     * If the class cannot be found or loaded, it returns {@code null}. The actual loading is delegated to
     * the {@link #loadClass(ClassLoader, String, boolean)} method with caching disabled by default.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Resolve a class using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String className = "com.example.MyClass";
     * Class<?> resolvedClass = ClassLoaderUtils.resolveClass(className, customClassLoader);
     * if (resolvedClass != null) {
     *     System.out.println("Class resolved: " + resolvedClass.getName());
     * } else {
     *     System.out.println("Class not found or could not be loaded.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to resolve a class
     * String className = "java.lang.String";
     * Class<?> resolvedClass = ClassLoaderUtils.resolveClass(className, null);
     * if (resolvedClass != null) {
     *     System.out.println("Class resolved: " + resolvedClass.getName());
     * } else {
     *     System.out.println("Class not found or could not be loaded.");
     * }
     * }</pre>
     *
     * @param className   the fully qualified name of the class to resolve; may be blank or null
     * @param classLoader the ClassLoader to use for resolving the class; may be null
     * @return the resolved Class object if successful; {@code null} if the class cannot be found or loaded
     */
    @Nullable
    public static Class<?> resolveClass(@Nullable String className, @Nullable ClassLoader classLoader) {
        return resolveClass(className, classLoader, false);
    }

    /**
     * Resolves a class by its name using the provided ClassLoader or the default ClassLoader if none is specified,
     * optionally caching the result for faster subsequent lookups.
     *
     * <p>
     * This method attempts to load and return the {@link Class} object for the specified class name.
     * If the class cannot be found or loaded, it returns {@code null}. The actual loading is delegated to
     * the {@link #loadClass(ClassLoader, String, boolean)} method, with caching behavior controlled by the
     * <code>cached</code> parameter.
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Resolve a class using a specific ClassLoader without caching
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * String className = "com.example.MyClass";
     * Class<?> resolvedClass = ClassLoaderUtils.resolveClass(className, customClassLoader, false);
     * if (resolvedClass != null) {
     *     System.out.println("Class resolved: " + resolvedClass.getName());
     * } else {
     *     System.out.println("Class not found or could not be loaded.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to resolve a class with caching enabled
     * String className = "java.lang.String";
     * Class<?> resolvedClass = ClassLoaderUtils.resolveClass(className, null, true);
     * if (resolvedClass != null) {
     *     System.out.println("Class resolved: " + resolvedClass.getName());
     * } else {
     *     System.out.println("Class not found or could not be loaded.");
     * }
     * }</pre>
     *
     * @param className   the fully qualified name of the class to resolve; may be blank or null
     * @param classLoader the ClassLoader to use for resolving the class; may be null
     * @param cached      whether to cache the loaded class for faster subsequent lookups
     * @return the resolved Class object if successful; {@code null} if the class cannot be found or loaded
     */
    @Nullable
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

    /**
     * Retrieves all URLs from the class path associated with the specified ClassLoader and its parent hierarchy.
     *
     * <p>This method aggregates the URLs from the class paths across the entire inheritance chain of the provided
     * ClassLoader. If the provided ClassLoader is null, it uses the default ClassLoader determined by
     * {@link #getDefaultClassLoader()}.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Get all class path URLs using a specific ClassLoader
     * ClassLoader customClassLoader = MyCustomClassLoader.getInstance();
     * Set<URL> classPathURLs = ClassLoaderUtils.findAllClassPathURLs(customClassLoader);
     * System.out.println("Class-Path URLs: " + classPathURLs);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Use the default ClassLoader to get all class path URLs
     * Set<URL> classPathURLs = ClassLoaderUtils.findAllClassPathURLs(null);
     * System.out.println("Class-Path URLs: " + classPathURLs);
     * }</pre>
     *
     * @param classLoader the ClassLoader to retrieve class path URLs from; may be null
     * @return a set of URLs representing the class paths in the inheritance hierarchy of the ClassLoader
     */
    @Nonnull
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

    /**
     * Removes the specified URL from the class path of the given ClassLoader if it supports such operation.
     *
     * <p>
     * This method checks whether the provided ClassLoader is an instance of {@link SecureClassLoader} or a compatible subclass,
     * as only those are expected to support dynamic modification of the class path. If it is, the method delegates the removal
     * operation to the configured {@link URLClassPathHandle} instance ({@link #urlClassPathHandle}).
     * </p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Remove a URL from a URLClassLoader
     * URL urlToRemove = new URL("file:/path/to/removed.jar");
     * URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{urlToRemove});
     *
     * boolean removed = ClassLoaderUtils.removeClassPathURL(urlClassLoader, urlToRemove);
     * System.out.println("Was the URL removed? " + removed);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Attempt to remove a URL from a non-SecureClassLoader
     * ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
     * URL urlToRemove = new URL("file:/some/path.jar");
     *
     * boolean removed = ClassLoaderUtils.removeClassPathURL(systemClassLoader, urlToRemove);
     * System.out.println("Was the URL removed? " + removed); // Likely false
     * }</pre>
     *
     * @param classLoader the ClassLoader from which the URL should be removed; may be null
     * @param url         the URL to remove from the class path
     * @return true if the URL was successfully removed; false otherwise
     */
    public static boolean removeClassPathURL(@Nullable ClassLoader classLoader, URL url) {
        if (!(classLoader instanceof SecureClassLoader)) {
            return false;
        }
        return urlClassPathHandle.removeURL(classLoader, url);
    }

    /**
     * Attempts to find and return the first {@link URLClassLoader} in the hierarchy starting from the provided ClassLoader.
     *
     * <p>If the given ClassLoader is an instance of URLClassLoader, it is returned directly. Otherwise,
     * this method traverses up the parent hierarchy to locate a URLClassLoader. If none is found,
     * it returns null.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Find URLClassLoader from a known URLClassLoader instance
     * URLClassLoader urlClassLoader = (URLClassLoader) MyClass.class.getClassLoader();
     * URLClassLoader result = ClassLoaderUtils.findURLClassLoader(urlClassLoader);
     * System.out.println("Found URLClassLoader: " + result);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Attempt to find URLClassLoader from a non-URLClassLoader
     * ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
     * URLClassLoader result = ClassLoaderUtils.findURLClassLoader(systemClassLoader);
     * if (result != null) {
     *     System.out.println("Found URLClassLoader: " + result);
     * } else {
     *     System.out.println("No URLClassLoader found in the hierarchy.");
     * }
     * }</pre>
     *
     * @param classLoader the ClassLoader to start searching from; may be null
     * @return the first URLClassLoader found in the hierarchy, or null if none exists
     */
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
     * Creates a new instance of {@link URLClassLoader} using the URLs provided in an {@link Iterable}.
     *
     * <p>This method ensures that the resulting class loader has access to all the URLs specified.
     * If the iterable is empty or contains null elements, an exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Create a URLClassLoader from a list of URLs
     * List<URL> urls = Arrays.asList(new URL("file:/path/to/jar1.jar"), new URL("file:/path/to/jar2.jar"));
     * URLClassLoader classLoader = ClassLoaderUtils.newURLClassLoader(urls);
     * System.out.println("ClassLoader created successfully.");
     * }</pre>
     *
     * @param urls the URLs from which to create the class loader; must not be null and must not contain null elements
     * @return a non-null instance of {@link URLClassLoader}
     * @throws IllegalArgumentException if the 'urls' is null or contains null elements
     */
    @Nonnull
    public static URLClassLoader newURLClassLoader(@Nonnull Iterable<URL> urls) {
        return newURLClassLoader(urls, null);
    }


    /**
     * Creates a new instance of {@link URLClassLoader} using the URLs provided in an {@link Iterable}.
     *
     * <p>This method ensures that the resulting class loader has access to all the URLs specified.
     * If the iterable is empty or contains null elements, an exception will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Create a URLClassLoader from a list of URLs
     * List<URL> urls = Arrays.asList(new URL("file:/path/to/jar1.jar"), new URL("file:/path/to/jar2.jar"));
     * URLClassLoader classLoader = ClassLoaderUtils.newURLClassLoader(urls, parentClassLoader);
     * System.out.println("ClassLoader created successfully.");
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Handle empty URLs list gracefully with try-catch
     * try {
     *     URLClassLoader classLoader = ClassLoaderUtils.newURLClassLoader(Collections.emptyList(), null);
     * } catch (IllegalArgumentException e) {
     *     System.err.println("Failed to create URLClassLoader: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param urls        the URLs from which to create the class loader; must not be null and must not contain null elements
     * @param classLoader the parent class loader for delegation, may be null
     * @return a non-null instance of {@link URLClassLoader}
     * @throws IllegalArgumentException if the 'urls' is null or contains null elements
     */
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
     * Creates a new instance of {@link URLClassLoader} using the provided array of URLs and an optional parent ClassLoader.
     *
     * <p>This method ensures that the resulting class loader has access to all the URLs specified.
     * If the 'urls' array is null or contains null elements, an {@link IllegalArgumentException} will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Create a URLClassLoader with a custom parent ClassLoader
     * URL[] urls = { new URL("file:/path/to/library.jar") };
     * ClassLoader parentClassLoader = MyClass.class.getClassLoader();
     * URLClassLoader urlClassLoader = ClassLoaderUtils.newURLClassLoader(urls, parentClassLoader);
     * System.out.println("ClassLoader created successfully.");
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Create a URLClassLoader without a parent ClassLoader (uses system ClassLoader as default)
     * URL[] urls = { new URL("file:/path/to/another-library.jar") };
     * URLClassLoader urlClassLoader = ClassLoaderUtils.newURLClassLoader(urls, null);
     * System.out.println("ClassLoader created successfully without a parent.");
     * }</pre>
     *
     * <pre>{@code
     * // Example 3: Handle invalid input gracefully with try-catch
     * try {
     *     URLClassLoader classLoader = ClassLoaderUtils.newURLClassLoader(null, null);
     * } catch (IllegalArgumentException e) {
     *     System.err.println("Caught expected exception: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param urls   the URLs from which to create the class loader; must not be null and must not contain null elements
     * @param parent the parent class loader for delegation, may be null
     * @return a non-null instance of {@link URLClassLoader}
     * @throws IllegalArgumentException if the 'urls' is null or contains null elements
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
     * Resolves or creates a {@link URLClassLoader} instance from the specified ClassLoader.
     *
     * <p>If the provided ClassLoader is already an instance of URLClassLoader, it is returned directly.
     * If not, this method attempts to find a URLClassLoader in the parent hierarchy. If none is found,
     * a new URLClassLoader is created using all URLs from the class path associated with the given ClassLoader.</p>
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Resolve an existing URLClassLoader
     * ClassLoader classLoader = MyClass.class.getClassLoader();
     * URLClassLoader resolvedLoader = ClassLoaderUtils.resolveURLClassLoader(classLoader);
     * System.out.println("Resolved URLClassLoader: " + resolvedLoader);
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Create a new URLClassLoader when none exists in the hierarchy
     * ClassLoader customClassLoader = new MyCustomClassLoader();
     * URLClassLoader resolvedLoader = ClassLoaderUtils.resolveURLClassLoader(customClassLoader);
     * System.out.println("Created URLClassLoader with URLs: " + Arrays.toString(resolvedLoader.getURLs()));
     * }</pre>
     *
     * @param classLoader the ClassLoader to resolve into a URLClassLoader; may be null
     * @return a non-null URLClassLoader instance derived from or wrapping the provided ClassLoader
     * @throws IllegalArgumentException if any error occurs during creation of the URLClassLoader
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
     * ResourceType defines different types of resources that can be accessed through a ClassLoader.
     * Each resource type provides its own logic for determining if it supports a given resource name,
     * and for normalizing the name to match the format expected by the underlying system.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String className = "com.example.MyClass.class";
     * String normalizedClassName = ResourceType.CLASS.normalize(className);
     * System.out.println("Normalized class name: " + normalizedClassName);
     * }</pre>
     *
     * <pre>{@code
     * String packageName = "com.example.package";
     * boolean isPackage = ResourceType.PACKAGE.supports(packageName);
     * System.out.println("Is package: " + isPackage);
     * }</pre>
     *
     * <pre>{@code
     * String resourceName = "config.properties";
     * String resolvedResource = ResourceType.DEFAULT.resolve(resourceName);
     * System.out.println("Resolved resource: " + resolvedResource);
     * }</pre>
     */
    public enum ResourceType {

        /**
         * The default resource type. This type accepts any input and does not perform any transformation other than path normalization.
         *
         * <p><b>Supports:</b> All resource names.</p>
         * <p><b>Normalization:</b> Simply normalizes the path using standard rules (e.g., removing redundant slashes).</p>
         *
         * <h4>Example</h4>
         * <pre>{@code
         * String rawName = "raw/resource.txt";
         * String resolvedName = ResourceType.DEFAULT.resolve(rawName); // Returns "raw/resource.txt"
         * }</pre>
         */
        DEFAULT {
            @Override
            boolean supports(String name) {
                return true;
            }

            @Override
            public String normalize(String name) {
                return name;
            }
        },

        /**
         * Represents class file resources. This type expects resource names to end with ".class".
         *
         * <p><b>Supports:</b> Names ending in ".class".</p>
         * <p><b>Normalization:</b> Converts fully qualified class names into corresponding path formats suitable for class loading.</p>
         *
         * <h4>Example</h4>
         * <pre>{@code
         * String className = "com.example.MyClass.class";
         * String normalized = ResourceType.CLASS.normalize(className); // Returns "com/example/MyClass.class"
         * }</pre>
         */
        CLASS {
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
        },

        /**
         * Represents package resources. This type expects resource names that represent Java packages.
         *
         * <p><b>Supports:</b> Names that do not contain slashes and are not class files.</p>
         * <p><b>Normalization:</b> Converts dot-separated package names into slash-separated paths, ensuring they end with a slash.</p>
         *
         * <h4>Example</h4>
         * <pre>{@code
         * String packageName = "com.example.mypackage";
         * String normalized = ResourceType.PACKAGE.normalize(packageName); // Returns "com/example/mypackage/"
         * }</pre>
         */
        PACKAGE {
            @Override
            boolean supports(String name) {
                if (name == null) {
                    return false;
                }
                //TODO: use regexp to match more precise patterns
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
         * Resolves the resource name based on the current type's support and normalization logic.
         *
         * <p>This method ensures that only supported resource names are processed and that they are normalized according to the type's rules.
         * It also applies generic path normalization and removes leading slashes from the final result.</p>
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * String rawName = "  com/example/MyClass.class  ";
         * String resolved = ResourceType.CLASS.resolve(rawName);
         * System.out.println("Resolved name: " + resolved); // Outputs "com/example/MyClass.class"
         * }</pre>
         *
         * @param name the original resource name
         * @return the resolved and normalized resource name, or null if not supported
         */
        public String resolve(String name) {
            if (isBlank(name) || !supports(name)) {
                return null;
            }

            String normalizedName = normalize(name);

            normalizedName = normalizePath(normalizedName);

            // Remove the character "/" in the start of String if found
            while (normalizedName.startsWith(SLASH)) {
                normalizedName = normalizedName.substring(1);
            }

            return normalizedName;
        }

        /**
         * Determines whether this resource type supports the specified resource name.
         *
         * <p>Implementations should define the conditions under which a name is considered valid for the type.</p>
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * boolean isSupported = ResourceType.CLASS.supports("MyClass.class");
         * System.out.println("Is supported: " + isSupported);
         * }</pre>
         *
         * @param name the resource name to check
         * @return true if the name is supported; false otherwise
         */
        abstract boolean supports(String name);

        /**
         * Normalizes the resource name according to the conventions of this type.
         *
         * <p>Implementations should transform the name into a format suitable for use with the ClassLoader or resource lookup mechanism.</p>
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * String normalized = ResourceType.PACKAGE.normalize("com.example.package");
         * System.out.println("Normalized name: " + normalized);
         * }</pre>
         *
         * @param name the resource name to normalize
         * @return the normalized resource name
         */
        abstract String normalize(String name);
    }

    private ClassLoaderUtils() {
    }
}