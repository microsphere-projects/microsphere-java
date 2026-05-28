package io.microsphere.util;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.last;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.io.IOUtils.readLines;
import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.DEFAULT;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getResources;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.ServiceLoader.load;

/**
 * Utility class for loading and managing service providers via {@link ServiceLoader}, with support for caching,
 * prioritization, and ClassLoader hierarchy traversal.
 *
 * <p>{@link ServiceLoaderUtils} provides methods to load all implementations of a service type, retrieve the first or last
 * implementation based on declaration order or priority, and return them as either a list or an array. It supports custom
 * class loaders and optional caching of loaded services.</p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>Load services using the context class loader or a specified class loader.</li>
 *     <li>Supports caching of loaded services (configurable).</li>
 *     <li>Sorts services by priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</li>
 *     <li>Returns read-only lists or arrays of service instances.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Loading All Services</h4>
 * <pre>{@code
 * List<MyService> services = ServiceLoaderUtils.loadServicesList(MyService.class);
 * for (MyService service : services) {
 *     service.execute();
 * }
 * }</pre>
 *
 * <h4>Loading First Service (Highest Priority)</h4>
 * <pre>{@code
 * MyService service = ServiceLoaderUtils.loadFirstService(MyService.class);
 * service.initialize();
 * }</pre>
 *
 * <h4>Loading Last Service (Lowest Priority)</h4>
 * <pre>{@code
 * MyService service = ServiceLoaderUtils.loadLastService(MyService.class);
 * service.shutdown();
 * }</pre>
 *
 * <h4>Loading With Custom ClassLoader</h4>
 * <pre>{@code
 * ClassLoader cl = MyClassLoader.getInstance();
 * MyService[] services = ServiceLoaderUtils.loadServices(MyService.class, cl);
 * }</pre>
 *
 * <h4>Loading Without Caching</h4>
 * <pre>{@code
 * List<MyService> services = ServiceLoaderUtils.loadServicesList(MyService.class, false);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServiceLoader
 * @since 1.0.0
 */
public abstract class ServiceLoaderUtils implements Utils {

    private static final Logger logger = getLogger(ServiceLoaderUtils.class);

    /**
     * The default value of the {@link #SERVICE_LOADER_CACHED} property : {@code "false"}
     */
    public static final String DEFAULT_SERVICE_LOADER_CACHED_PROPERTY_VALUE = "false";

    /**
     * The name of the {@link #SERVICE_LOADER_CACHED} property : {@code "microsphere.service-loader.cached"}
     */
    public static final String SERVICE_LOADER_CACHED_PROPERTY_NAME = MICROSPHERE_PROPERTY_NAME_PREFIX + "service-loader.cached";

    /**
     * The location of service provider configuration files : "META-INF/services/".
     *
     * @see ServiceLoader
     */
    public static final String SERVICES_PROVIDER_LOCATION = "META-INF/services/";

    /**
     * The locatings' pattern of configuration files of the single service provider : "META-INF/services/{}",
     * where "{}" is replaced with the service type name.
     *
     * @see ServiceLoader
     */
    public static final String SERVICE_PROVIDER_CONFIG_FILES_LOCATION_PATTERN = SERVICES_PROVIDER_LOCATION + "{}";

    /**
     * Whether to cache the loaded services
     */
    @ConfigurationProperty(
            name = SERVICE_LOADER_CACHED_PROPERTY_NAME,
            defaultValue = DEFAULT_SERVICE_LOADER_CACHED_PROPERTY_VALUE,
            description = "Whether to cache the loaded services",
            source = SYSTEM_PROPERTIES_SOURCE
    )
    public static final boolean SERVICE_LOADER_CACHED = parseBoolean(getProperty(SERVICE_LOADER_CACHED_PROPERTY_NAME, DEFAULT_SERVICE_LOADER_CACHED_PROPERTY_VALUE));

    private static final ConcurrentMap<Class<?>, List<?>> servicesCache = newConcurrentHashMap();

    /**
     * Retrieves the implementation classes of all service providers for the specified service type using the context class loader.
     *
     * <p>This method searches for service provider configuration files located at
     * {@code META-INF/services/<serviceType>} using the context class loader associated with the service type.
     * It loads the classes defined in these configuration files and verifies their assignability
     * to the service type. This method uses the default fail-fast behavior ({@code true}).</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<Class<MyService>> classes = ServiceLoaderUtils.getServiceClasses(MyService.class);
     * for (Class<MyService> clazz : classes) {
     *     System.out.println("Found service implementation class: " + clazz.getName());
     * }
     * }</pre>
     *
     * @param <T>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @return an unmodifiable set of implementation classes of the service providers
     * @throws IllegalStateException if a class cannot be loaded or is not assignable to the service type
     */
    @Nonnull
    @Immutable
    public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType) {
        return getServiceClasses(serviceType, null, true);
    }

    /**
     * Retrieves the implementation classes of all service providers for the specified service type using the context class loader.
     *
     * <p>This method searches for service provider configuration files located at
     * {@code META-INF/services/<serviceType>} using the context class loader associated with the service type.
     * It loads the classes defined in these configuration files and verifies their assignability
     * to the service type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Fail fast if a class cannot be loaded or is not assignable
     * Set<Class<MyService>> classes = ServiceLoaderUtils.getServiceClasses(MyService.class, true);
     * for (Class<MyService> clazz : classes) {
     *     System.out.println("Found service implementation class: " + clazz.getName());
     * }
     *
     * // Lenient mode: skip invalid classes
     * Set<Class<MyService>> lenientClasses = ServiceLoaderUtils.getServiceClasses(MyService.class, false);
     * }</pre>
     *
     * @param <T>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param failFast    if {@code true}, throws an exception when a class cannot be loaded or is not assignable;
     *                    if {@code false}, skips invalid classes
     * @return an unmodifiable set of implementation classes of the service providers
     * @throws IllegalStateException if {@code failFast} is {@code true} and an error occurs during loading or assignment check
     */
    @Nonnull
    @Immutable
    public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, boolean failFast) {
        return getServiceClasses(serviceType, null, failFast);
    }

    /**
     * Retrieves the implementation classes of all service providers for the specified service type using the provided {@link ClassLoader}.
     *
     * <p>This method searches for service provider configuration files located at
     * {@code META-INF/services/<serviceType>} using the provided {@link ClassLoader}.
     * It loads the classes defined in these configuration files and verifies their assignability
     * to the service type. This method uses the default fail-fast behavior ({@code true}).</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * Set<Class<MyService>> classes = ServiceLoaderUtils.getServiceClasses(MyService.class, classLoader);
     * for (Class<MyService> clazz : classes) {
     *     System.out.println("Found service implementation class: " + clazz.getName());
     * }
     * }</pre>
     *
     * @param <T>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to locate and load the resources; may be {@code null}
     * @return an unmodifiable set of implementation classes of the service providers
     * @throws IllegalStateException if a class cannot be loaded or is not assignable to the service type
     */
    @Nonnull
    @Immutable
    public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, @Nullable ClassLoader classLoader) {
        return getServiceClasses(serviceType, classLoader, true);
    }

    /**
     * Retrieves the implementation classes of all service providers for the specified service type.
     *
     * <p>This method searches for service provider configuration files located at
     * {@code META-INF/services/<serviceType>} using the provided {@link ClassLoader}.
     * It loads the classes defined in these configuration files and verifies their assignability
     * to the service type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * // Fail fast if a class cannot be loaded or is not assignable
     * Set<Class<MyService>> classes = ServiceLoaderUtils.getServiceClasses(MyService.class, classLoader, true);
     * for (Class<MyService> clazz : classes) {
     *     System.out.println("Found service implementation class: " + clazz.getName());
     * }
     *
     * // Lenient mode: skip invalid classes
     * Set<Class<MyService>> lenientClasses = ServiceLoaderUtils.getServiceClasses(MyService.class, classLoader, false);
     * }</pre>
     *
     * @param <T>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to locate and load the resources; may be {@code null}
     * @param failFast    if {@code true}, throws an exception when a class cannot be loaded or is not assignable;
     *                    if {@code false}, skips invalid classes
     * @return an unmodifiable set of implementation classes of the service providers
     * @throws IllegalStateException if {@code failFast} is {@code true} and an error occurs during loading or assignment check
     */
    @Nonnull
    @Immutable
    public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, @Nullable ClassLoader classLoader, boolean failFast) {
        Set<String> serviceClassNames = doGetServiceClassNames(serviceType, classLoader);
        Set<Class<T>> serviceClasses = newFixedLinkedHashSet(serviceClassNames.size());
        for (String serviceClassName : serviceClassNames) {
            Class<?> serviceClass = loadClass(classLoader, serviceClassName);
            if (serviceClass == null) {
                if (failFast) {
                    String errorMessage = format("The service class[name : '{}'] can't be loaded by {}", serviceClassName, classLoader);
                    throw new IllegalStateException(errorMessage);
                }
            } else if (isAssignableFrom(serviceType, serviceClass)) {
                serviceClasses.add((Class<T>) serviceClass);
            } else if (failFast) {
                String errorMessage = format("The service class[name : '{}'] is not assignable to the service type {}", serviceClassName, serviceType);
                throw new IllegalStateException(errorMessage);
            }
        }
        return unmodifiableSet(serviceClasses);
    }

    /**
     * Retrieves the class names of all service providers for the specified service type using the context class loader.
     *
     * <p>This method searches for service provider configuration files located at
     * {@code META-INF/services/<serviceType>} using the context class loader associated with the service type.
     * It reads the fully qualified class names defined in these configuration files.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Set<String> classNames = ServiceLoaderUtils.getServiceClassNames(MyService.class);
     * for (String className : classNames) {
     *     System.out.println("Found service implementation: " + className);
     * }
     * }</pre>
     *
     * @param serviceType the class of the service type, cannot be null
     * @return an unmodifiable set of fully qualified class names of the service providers
     */
    @Nonnull
    @Immutable
    public static Set<String> getServiceClassNames(Class<?> serviceType) {
        return getServiceClassNames(serviceType, null);
    }

    /**
     * Retrieves the class names of all service providers for the specified service type.
     *
     * <p>This method searches for service provider configuration files located at
     * {@code META-INF/services/<serviceType>} using the provided {@link ClassLoader}.
     * It reads the fully qualified class names defined in these configuration files.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * Set<String> classNames = ServiceLoaderUtils.getServiceClassNames(MyService.class, classLoader);
     * for (String className : classNames) {
     *     System.out.println("Found service implementation: " + className);
     * }
     * }</pre>
     *
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to locate the resources; may be {@code null}
     * @return an unmodifiable set of fully qualified class names of the service providers
     */
    @Nonnull
    @Immutable
    public static Set<String> getServiceClassNames(Class<?> serviceType, @Nullable ClassLoader classLoader) {
        return unmodifiableSet(doGetServiceClassNames(serviceType, classLoader));
    }

    /**
     * Retrieves the URLs of the service provider configuration files for the specified service type.
     *
     * <p>This method searches for configuration files located at {@code META-INF/services/<serviceType>}
     * using the provided {@link ClassLoader}. It traverses the class loader hierarchy to find all
     * matching resources.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * Set<URL> resources = ServiceLoaderUtils.getServiceResources(MyService.class, classLoader);
     * for (URL url : resources) {
     *     System.out.println("Found service config at: " + url);
     * }
     * }</pre>
     *
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to locate the resources; may be {@code null}
     * @return a set of {@link URL}s pointing to the service provider configuration files
     * @throws IOException if an I/O error occurs while accessing the resources
     */
    @Nonnull
    @Immutable
    public static Set<URL> getServiceResoources(Class<?> serviceType, @Nullable ClassLoader classLoader) throws IOException {
        String resouceLocation = format(SERVICE_PROVIDER_CONFIG_FILES_LOCATION_PATTERN, serviceType.getName());
        return getResources(classLoader, DEFAULT, resouceLocation);
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the context class loader.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned list contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<MyService> services = ServiceLoaderUtils.loadServicesList(MyService.class);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @return a read-only list containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    @Immutable
    public static <S> List<S> loadServicesList(Class<S> serviceType) throws IllegalArgumentException {
        return loadServicesList(serviceType, getClassLoader(serviceType));
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader}.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned list contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * List<MyService> services = ServiceLoaderUtils.loadServicesList(MyService.class, classLoader);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @return a read-only list containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    @Immutable
    public static <S> List<S> loadServicesList(Class<S> serviceType, @Nullable ClassLoader classLoader) throws IllegalArgumentException {
        return loadServicesList(serviceType, classLoader, SERVICE_LOADER_CACHED);
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the context class loader,
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned list contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<MyService> services = ServiceLoaderUtils.loadServicesList(MyService.class, true);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return a read-only list containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    @Immutable
    public static <S> List<S> loadServicesList(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadServicesList(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader},
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned list contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * List<MyService> services = ServiceLoaderUtils.loadServicesList(MyService.class, classLoader, true);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return a read-only list containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    @Immutable
    public static <S> List<S> loadServicesList(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return unmodifiableList(loadServicesAsList(serviceType, classLoader, cached));
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the context class loader.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned array contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService[] services = ServiceLoaderUtils.loadServices(MyService.class);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @return an array containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S[] loadServices(Class<S> serviceType) throws IllegalArgumentException {
        return loadServices(serviceType, getClassLoader(serviceType));
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader}.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned array contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * MyService[] services = ServiceLoaderUtils.loadServices(MyService.class, classLoader);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @return an array containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S[] loadServices(Class<S> serviceType, @Nullable ClassLoader classLoader) throws IllegalArgumentException {
        return loadServices(serviceType, classLoader, SERVICE_LOADER_CACHED);
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the context class loader,
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method utilizes the context class loader associated with the provided service type to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned array contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService[] services = ServiceLoaderUtils.loadServices(MyService.class, true);
     * for (MyService service : services) {
     *     service.initialize();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return an array containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S[] loadServices(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadServices(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Loads all implementation instances of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader},
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method traverses the hierarchy of {@link ClassLoader} to load the service configuration file located at
     * {@code /META-INF/services/<serviceType>}. The returned array contains all discovered implementations in the order they were found,
     * sorted by their priority if they implement the {@link io.microsphere.lang.Prioritized} interface.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * MyService[] services = ServiceLoaderUtils.loadServices(MyService.class, classLoader, true);
     * for (MyService service : services) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return an array containing all implementation instances of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S[] loadServices(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return asArray(loadServicesAsList(serviceType, classLoader, cached), serviceType);
    }

    /**
     * Loads the first instance of the specified {@link ServiceLoader} service type using the context class loader.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader)}, and returns the first element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService service = ServiceLoaderUtils.loadFirstService(MyService.class);
     * if (service != null) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @return the first implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadFirstService(Class<S> serviceType) throws IllegalArgumentException {
        return loadFirstService(serviceType, getClassLoader(serviceType));
    }

    /**
     * Loads the first instance of the specified {@link ServiceLoader} service type using the context class loader,
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader, boolean)}, and returns the first element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     * <p>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService service = ServiceLoaderUtils.loadFirstService(MyService.class, true);
     * if (service != null) {
     *     service.initialize();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return the first implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadFirstService(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadFirstService(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Loads the first instance of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader}.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader)}, and returns the first element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * MyService service = ServiceLoaderUtils.loadFirstService(MyService.class, classLoader);
     * if (service != null) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @return the first implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadFirstService(Class<S> serviceType, @Nullable ClassLoader classLoader) throws IllegalArgumentException {
        return loadFirstService(serviceType, classLoader, SERVICE_LOADER_CACHED);
    }

    /**
     * Loads the first instance of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader},
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader, boolean)}, and returns the first element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * MyService service = ServiceLoaderUtils.loadFirstService(MyService.class, classLoader, true);
     * if (service != null) {
     *     service.initialize();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return the first implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadFirstService(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return loadService(serviceType, classLoader, cached, true);
    }

    /**
     * Loads the last instance of the specified {@link ServiceLoader} service type using the context class loader.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader)}, and returns the last element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService service = ServiceLoaderUtils.loadLastService(MyService.class);
     * if (service != null) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @return the last implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadLastService(Class<S> serviceType) throws IllegalArgumentException {
        return loadLastService(serviceType, getClassLoader(serviceType));
    }

    /**
     * Loads the last instance of the specified {@link ServiceLoader} service type using the context class loader,
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader, boolean)}, and returns the last element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService service = ServiceLoaderUtils.loadLastService(MyService.class, true);
     * if (service != null) {
     *     service.initialize();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return the last implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadLastService(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadLastService(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Loads the last instance of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader}.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader)}, and returns the last element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * MyService service = ServiceLoaderUtils.loadLastService(MyService.class, classLoader);
     * if (service != null) {
     *     service.execute();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @return the last implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadLastService(Class<S> serviceType, @Nullable ClassLoader classLoader) throws IllegalArgumentException {
        return loadLastService(serviceType, classLoader, SERVICE_LOADER_CACHED);
    }

    /**
     * Loads the last instance of the specified {@link ServiceLoader} service type using the provided {@link ClassLoader},
     * with an option to enable or disable caching of the loaded services.
     *
     * <p>This method retrieves the list of service implementations using
     * {@link #loadServicesList(Class, ClassLoader, boolean)}, and returns the last element from the list.
     * The order of service instances is determined by their declaration in the configuration files,
     * and services implementing the {@link io.microsphere.lang.Prioritized} interface are sorted by priority.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     * MyService service = ServiceLoaderUtils.loadLastService(MyService.class, classLoader, true);
     * if (service != null) {
     *     service.initialize();
     * }
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the class of the service type, cannot be null
     * @param classLoader the class loader used to load service implementations; must not be null
     * @param cached      flag indicating whether to cache the loaded services
     * @return the last implementation instance of the service type
     * @throws IllegalArgumentException if no implementation is defined for the service type in the configuration file
     */
    @Nonnull
    public static <S> S loadLastService(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return loadService(serviceType, classLoader, cached, false);
    }

    /**
     * Loads a single service implementation, either the first or last, based on the sorted order.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Load the first service (used internally by loadFirstService):
     *   MyService first = ServiceLoaderUtils.loadService(MyService.class, classLoader, false, true);
     *   // Load the last service (used internally by loadLastService):
     *   MyService last = ServiceLoaderUtils.loadService(MyService.class, classLoader, false, false);
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the interface or abstract class representing the service
     * @param classLoader the class loader to use; may be {@code null}
     * @param cached      whether to use cached service instances
     * @param first       {@code true} to return the first service, {@code false} for the last
     * @return the selected service implementation
     * @throws IllegalArgumentException if no implementation is found
     */
    static <S> S loadService(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached, boolean first) {
        List<S> serviceList = loadServicesAsList(serviceType, classLoader, cached);
        return first ? first(serviceList) : last(serviceList);
    }

    /**
     * Load all instances of service type
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @param cached      the list of services to be cached
     * @return Load all instances of service type
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    static <S> List<S> loadServicesAsList(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        final List<S> serviceList;
        if (cached) {
            serviceList = (List<S>) servicesCache.computeIfAbsent(serviceType, type -> loadServicesAsList(type, classLoader));
        } else {
            serviceList = loadServicesAsList(serviceType, classLoader);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Loaded the service[type : '{}' , cached : {}] list : {}", serviceType, cached, serviceList);
        }

        return serviceList;
    }

    /**
     * Loads all service implementations as a list using the specified class loader, without caching.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   List<MyService> services = ServiceLoaderUtils.loadServicesAsList(
     *       MyService.class, Thread.currentThread().getContextClassLoader());
     * }</pre>
     *
     * @param <S>         the service type
     * @param serviceType the interface or abstract class representing the service
     * @param classLoader the class loader to use; may be {@code null}
     * @return a sorted list of service implementations
     * @throws IllegalArgumentException if no implementation is defined
     */
    static <S> List<S> loadServicesAsList(Class<S> serviceType, @Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getClassLoader(serviceType);
        }

        ServiceLoader<S> serviceLoader = load(serviceType, classLoader);

        Iterator<S> iterator = serviceLoader.iterator();

        if (!iterator.hasNext()) {
            String className = serviceType.getName();
            String message = format("No Service interface[type : {}] implementation was defined in service loader configuration file[/META-INF/services/{}] under ClassLoader[{}]", className, className, classLoader);
            throw new IllegalArgumentException(message);
        }

        List<S> serviceList = newLinkedList(iterator);

        sort(serviceList, COMPARATOR);

        return serviceList;
    }

    static Set<String> doGetServiceClassNames(Class<?> serviceType, @Nullable ClassLoader classLoader) {
        Set<String> serviceClassNames = newLinkedHashSet();
        execute(() -> {
            Set<URL> serviceResoources = getServiceResoources(serviceType, classLoader);
            for (URL serviceResoource : serviceResoources) {
                try (InputStream inputStream = serviceResoource.openStream()) {
                    String[] classNames = readLines(inputStream);
                    serviceClassNames.addAll(ofList(classNames));
                }
            }
        });
        return serviceClassNames;
    }

    private ServiceLoaderUtils() {
    }
}