package io.github.microsphere.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import static io.github.microsphere.collection.CollectionUtils.first;
import static io.github.microsphere.collection.ListUtils.toList;
import static io.github.microsphere.util.ArrayUtils.asArray;
import static io.github.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.github.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.util.Collections.unmodifiableList;
import static java.util.ServiceLoader.load;

/**
 * {@link ServiceLoader} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ServiceLoader
 * @since 1.0.0
 */
public abstract class ServiceLoaderUtils extends BaseUtils {

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <T>         service type
     * @param serviceType service type
     * @return service type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <T> List<T> loadServicesList(Class<T> serviceType) throws IllegalArgumentException {
        return loadServicesList(serviceType, getClassLoader(serviceType));
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <T>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @return service type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <T> List<T> loadServicesList(Class<T> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        return unmodifiableList(loadServicesList0(serviceType, classLoader));
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <T>         service type
     * @param serviceType service type
     * @return service type all implementation objects
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <T> T[] loadServices(Class<T> serviceType) throws IllegalArgumentException {
        return loadServices(serviceType, getClassLoader(serviceType));
    }

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceType<code> under its class path.
     * The configuration file of each service type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <T>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @return service type all implementation objects
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceType</code>
     */
    public static <T> T[] loadServices(Class<T> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        List<T> servicesList = loadServicesList0(serviceType, classLoader);
        return asArray(servicesList, serviceType);
    }

    /**
     * Load all instances of service type
     *
     * @param <T>         service type
     * @param serviceType service type
     * @param classLoader {@link ClassLoader}
     * @return Load all instances of service type
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    private static <T> List<T> loadServicesList0(Class<T> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        if (classLoader == null) {
            classLoader = getDefaultClassLoader();
        }
        ServiceLoader<T> serviceLoader = load(serviceType, classLoader);
        Iterator<T> iterator = serviceLoader.iterator();
        List<T> serviceList = toList(iterator);

        if (serviceList.isEmpty()) {
            String className = serviceType.getName();
            String message = String.format("No Service interface[type : %s] implementation was defined in service loader configuration file[/META-INF/services/%s] under ClassLoader[%s]", className, className, classLoader);
            IllegalArgumentException e = new IllegalArgumentException(message);
            throw e;
        }

        return serviceList;
    }

    /**
     * Load the first instance of {@link #loadServicesList(Class, ClassLoader) Service interface instances list}
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * @param <T>         service type
     * @param serviceType
     * @return If it exists, {@link #loadServicesList(Class, ClassLoader) loads the first in the list of implementation objects of service type}.
     * @throws IllegalArgumentException If the implementation class that does not define <code>serviceType<code> is in the configuration file
     *                                  META-INF/services/<code>serviceType<code>, IllegalArgumentException will be thrown
     */
    public static <T> T loadFirstService(Class<T> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        return first(loadServicesList0(serviceType, classLoader));
    }

    /**
     * Loads the last in the list of objects implementing the service type, if present.
     * <p/>
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, once the configuration file is loaded in the parent's ClassLoader at a higher level (here the highest-level ClassLoader is Bootstrap ClassLoader)
     * /META-INF/services/<code>serviceType</code>
     * If the last implementation class is used, the lower-level Class Loader will not be able to override the previous definition。
     *
     * @param <T>         service type
     * @param serviceType
     * @return Loads the last in the list of objects implementing the service type, if present.
     * @throws IllegalArgumentException see {@link #loadServicesList(Class, ClassLoader)}
     */
    public static <T> T loadLastService(Class<T> serviceType, ClassLoader classLoader) throws IllegalArgumentException {
        List<T> serviceList = loadServicesList0(serviceType, classLoader);
        return serviceList.get(serviceList.size() - 1);
    }

}
