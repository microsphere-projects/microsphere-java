package io.microsphere.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import static io.microsphere.collection.ListUtils.toList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.util.Collections.unmodifiableList;

/**
 * {@link ServiceLoader} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ServiceLoader
 * @since 1.0.0
 */
public abstract class ServiceLoaderUtils extends BaseUtils {

    private static final Map<ClassLoader, Map<Class<?>, ServiceLoader<?>>> serviceLoadersCache = new ConcurrentHashMap<>();

    static {
        // Clear cache on JVM shutdown
        ShutdownHookUtils.addShutdownHookCallback(serviceLoadersCache::clear);
    }

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
        return loadServicesList(serviceType, classLoader, false);
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
        return unmodifiableList(loadServicesList0(serviceType, classLoader, cached));
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
        return loadServices(serviceType, classLoader, false);
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
        return asArray(loadServicesList0(serviceType, classLoader, cached), serviceType);
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
        return loadFirstService(serviceType, classLoader, false);
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
        return loadLastService(serviceType, classLoader, false);
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

    public static <S> ServiceLoader<S> load(Class<S> serviceType, ClassLoader classLoader, boolean cached) {
        if (cached) {
            Map<Class<?>, ServiceLoader<?>> serviceLoadersMap =
                    serviceLoadersCache.computeIfAbsent(classLoader, cl -> newConcurrentHashMap());
            return (ServiceLoader<S>) serviceLoadersMap.computeIfAbsent(serviceType, type ->
                    ServiceLoader.load(serviceType, classLoader));
        }
        return ServiceLoader.load(serviceType, classLoader);
    }


    private static <S> S loadService(Class<S> serviceType, ClassLoader classLoader, boolean cached, boolean first) {
        List<S> serviceList = loadServicesList0(serviceType, classLoader, cached);
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
    private static <S> List<S> loadServicesList0(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException {
        if (classLoader == null) {
            classLoader = getDefaultClassLoader();
        }
        ServiceLoader<S> serviceLoader = load(serviceType, classLoader, cached);
        Iterator<S> iterator = serviceLoader.iterator();
        List<S> serviceList = toList(iterator);

        if (serviceList.isEmpty()) {
            String className = serviceType.getName();
            String message = String.format("No Service interface[type : %s] implementation was defined in service loader configuration file[/META-INF/services/%s] under ClassLoader[%s]", className, className, classLoader);
            IllegalArgumentException e = new IllegalArgumentException(message);
            throw e;
        }

        return serviceList;
    }

}
