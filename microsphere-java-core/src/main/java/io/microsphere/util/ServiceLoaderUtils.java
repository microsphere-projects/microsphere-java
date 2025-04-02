package io.microsphere.util;

import io.microsphere.logging.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.lang.Prioritized.COMPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static java.lang.Boolean.getBoolean;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.ServiceLoader.load;

/**
 * {@link ServiceLoader} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ServiceLoader
 * @since 1.0.0
 */
public abstract class ServiceLoaderUtils {

    private static final Logger logger = getLogger(ServiceLoaderUtils.class);

    static final boolean serviceLoaderCached = getBoolean("microsphere.service-loader.cached");

    private static final ConcurrentMap<Class<?>, List<?>> servicesCache = newConcurrentHashMap();

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @return service type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> List<S> loadServicesList(Class<S> serviceType) throws IllegalArgumentException {
        return loadServicesList(serviceType, getClassLoader(serviceType));
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @return service type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> List<S> loadServicesList(Class<S> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        return loadServicesList(serviceType, classLoader, serviceLoaderCached);
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param cached      the list of services to be cached
     * @return service type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> List<S> loadServicesList(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadServicesList(serviceType, getClassLoader(serviceType), cached);
    }


    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @param cached      the list of services to be cached
     * @return service type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> List<S> loadServicesList(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return unmodifiableList(loadServicesAsList(serviceType, classLoader, cached));
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @return service type all implementation objects
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> S[] loadServices(Class<S> serviceType) throws IllegalArgumentException {
        return loadServices(serviceType, getClassLoader(serviceType));
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @return service type all implementation objects
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> S[] loadServices(Class<S> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        return loadServices(serviceType, classLoader, serviceLoaderCached);
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param cached      the list of services to be cached
     * @return service type all implementation objects
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> S[] loadServices(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadServices(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @param cached      the list of services to be cached
     * @return service type all implementation objects
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <S> S[] loadServices(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return asArray(loadServicesAsList(serviceType, classLoader, cached), serviceType);
    }

    /**
     * Load the first instance of {@link #loadServicesList(Class) Service interface instances list}
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * @param <S>         service type
     * @param serviceType service type
     * @return If it exists, {@link #loadServicesList(Class, ClassLoader) loads the first in the list of implementation objects of service type}.
     * @throws IllegalArgumentException If the implementation class that does not define <code>serviceType<code> is in the configuration file
     *                                  META-INF/services/<code>serviceType<code>, IllegalArgumentException will be thrown
     */
    public static <S> S loadFirstService(Class<S> serviceType) throws IllegalArgumentException {
        return loadFirstService(serviceType, getClassLoader(serviceType));
    }

    /**
     * Load the first instance of {@link #loadServicesList(Class) Service interface instances list}
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param cached      the list of services to be cached
     * @return If it exists, {@link #loadServicesList(Class, ClassLoader) loads the first in the list of implementation objects of service type}.
     * @throws IllegalArgumentException If the implementation class that does not define <code>serviceType<code> is in the configuration file
     *                                  META-INF/services/<code>serviceType<code>, IllegalArgumentException will be thrown
     */
    public static <S> S loadFirstService(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadFirstService(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Load the first instance of {@link #loadServicesList(Class, ClassLoader) Service interface instances list}
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * @param <S>         service type
     * @param serviceType service type
     * @return If it exists, {@link #loadServicesList(Class, ClassLoader) loads the first in the list of implementation objects of service type}.
     * @throws IllegalArgumentException If the implementation class that does not define <code>serviceType<code> is in the configuration file
     *                                  META-INF/services/<code>serviceType<code>, IllegalArgumentException will be thrown
     */
    public static <S> S loadFirstService(Class<S> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        return loadFirstService(serviceType, classLoader, serviceLoaderCached);
    }

    /**
     * Load the first instance of {@link #loadServicesList(Class, ClassLoader) Service interface instances list}
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param cached      the list of services to be cached
     * @return If it exists, {@link #loadServicesList(Class, ClassLoader) loads the first in the list of implementation objects of service type}.
     * @throws IllegalArgumentException If the implementation class that does not define <code>serviceType<code> is in the configuration file
     *                                  META-INF/services/<code>serviceType<code>, IllegalArgumentException will be thrown
     */
    public static <S> S loadFirstService(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return loadService(serviceType, classLoader, cached, true);
    }

    /**
     * Loads the last in the list of objects implementing the service type, if present.
     * <p/>
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, once the configuration file is loaded in the parent's ClassLoader at a higher level (here the highest-level ClassLoader is Bootstrap ClassLoader)
     * /META-INF/services/<code>serviceType</code>
     * If the last implementation class is used, the lower-level Class Loader will not be able to override the previous definition。
     *
     * @param <S>         service type
     * @param serviceType service type
     * @return Loads the last in the list of objects implementing the service type, if present.
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    public static <S> S loadLastService(Class<S> serviceType) throws IllegalArgumentException {
        return loadLastService(serviceType, getClassLoader(serviceType));
    }

    /**
     * Loads the last in the list of objects implementing the service type, if present.
     * <p/>
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, once the configuration file is loaded in the parent's ClassLoader at a higher level (here the highest-level ClassLoader is Bootstrap ClassLoader)
     * /META-INF/services/<code>serviceType</code>
     * If the last implementation class is used, the lower-level Class Loader will not be able to override the previous definition。
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param cached      the list of services to be cached
     * @return Loads the last in the list of objects implementing the service type, if present.
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    public static <S> S loadLastService(Class<S> serviceType, boolean cached) throws IllegalArgumentException {
        return loadLastService(serviceType, getClassLoader(serviceType), cached);
    }

    /**
     * Loads the last in the list of objects implementing the service type, if present.
     * <p/>
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, once the configuration file is loaded in the parent's ClassLoader at a higher level (here the highest-level ClassLoader is Bootstrap ClassLoader)
     * /META-INF/services/<code>serviceType</code>
     * If the last implementation class is used, the lower-level Class Loader will not be able to override the previous definition。
     *
     * @param <S>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @return Loads the last in the list of objects implementing the service type, if present.
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    public static <S> S loadLastService(Class<S> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        return loadLastService(serviceType, classLoader, serviceLoaderCached);
    }

    /**
     * Loads the last in the list of objects implementing the service type, if present.
     * <p/>
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, once the configuration file is loaded in the parent's ClassLoader at a higher level (here the highest-level ClassLoader is Bootstrap ClassLoader)
     * /META-INF/services/<code>serviceType</code>
     * If the last implementation class is used, the lower-level Class Loader will not be able to override the previous definition。
     *
     * @param <S>         service type
     * @param serviceType service type
     * @return Loads the last in the list of objects implementing the service type, if present.
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    public static <S> S loadLastService(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        return loadService(serviceType, classLoader, cached, false);
    }

    private static <S> S loadService(Class<S> serviceType, ClassLoader classLoader, boolean cached, boolean first) {
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
    static <S> List<S> loadServicesAsList(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
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

    static <S> List<S> loadServicesAsList(Class<S> serviceType, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getClassLoader(serviceType);
        }

        ServiceLoader<S> serviceLoader = load(serviceType, classLoader);

        Iterator<S> iterator = serviceLoader.iterator();

        if (!iterator.hasNext()) {
            String className = serviceType.getName();
            String message = format("No Service interface[type : %s] implementation was defined in service loader configuration file[/META-INF/services/%s] under ClassLoader[%s]", className, className, classLoader);
            throw new IllegalArgumentException(message);
        }

        List<S> serviceList = newLinkedList(iterator);

        sort(serviceList, COMPARATOR);

        return serviceList;
    }

}
