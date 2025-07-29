package io.microsphere.util;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
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

    static <S> S loadService(Class<S> serviceType, @Nullable ClassLoader classLoader, boolean cached, boolean first) {
        List<S> serviceList = loadServicesAsList(serviceType, classLoader, cached);
        int index = first ? 0 : serviceList.size() - 1;
        return serviceList.get(index);
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

    private ServiceLoaderUtils() {
    }
}
