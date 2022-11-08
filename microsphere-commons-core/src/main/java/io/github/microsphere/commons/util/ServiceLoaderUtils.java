package io.github.microsphere.commons.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * {@link ServiceLoader} Utility
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ServiceLoader
 * @since 1.0.0
 */
public abstract class ServiceLoaderUtils {

    /**
     * Using the hierarchy of {@link ClassLoader}, each level of ClassLoader ( ClassLoader , its parent ClassLoader and higher)
     * will be able to load the configuration file META-INF/services <code>serviceInterfaceType<code> under its class path.
     * The configuration file of each service interface type can define multiple lists of implementation classes.
     * <p/>
     *
     * @param <T>                  service interface type
     * @param classLoader          {@link ClassLoader}
     * @param serviceInterfaceType service interface type
     * @return service interface type all implementation objects of {@link Collections#unmodifiableList(List) readonly list}
     * @throws IllegalArgumentException If it refers to the implementation class that does not define <code>serviceInterfaceType<code>
     *                                  in the configuration file /META-INF/services/<code>serviceInterfaceType</code>
     * @version 1.0.0
     * @since 1.0.0
     */
    public static <T> List<T> loadServicesList(ClassLoader classLoader, Class<T> serviceInterfaceType) throws IllegalArgumentException {
        return Collections.unmodifiableList(loadServicesList0(classLoader, serviceInterfaceType));
    }

    /**
     * Load all instances of service interface type
     *
     * @param <T>                  service interface type
     * @param classLoader          {@link ClassLoader}
     * @param serviceInterfaceType service interface type
     * @return Load all instances of service interface type
     * @throws IllegalArgumentException see {@link #loadServicesList(ClassLoader, Class)}
     * @version 1.0.0
     * @since 1.0.0
     */
    private static <T> List<T> loadServicesList0(ClassLoader classLoader, Class<T> serviceInterfaceType) throws IllegalArgumentException {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceInterfaceType, classLoader);
        Iterator<T> iterator = serviceLoader.iterator();
        List<T> serviceList = CollectionUtils.toList(iterator);

        if (serviceList.isEmpty()) {
            String className = serviceInterfaceType.getName();
            String message = String.format("No Service interface[type : %s] implementation was defined in service loader configuration file[/META-INF/services/%s] under ClassLoader[%s]", className, className, classLoader);
            IllegalArgumentException e = new IllegalArgumentException(message);
            throw e;
        }

        return serviceList;
    }

    /**
     * Load the first instance of {@link #loadServicesList(ClassLoader, Class) Service interface instances list}
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, each level of ClassLoader will be able to access the configuration files under its class path
     * /META-INF/services/<code>serviceInterfaceType</code>.
     * Then, override the first implementation class of the configuration file under the class path of ClassLoader,
     * thereby providing a mechanism for overriding the implementation class.
     *
     * @param <T>                  service interface type
     * @param serviceInterfaceType
     * @return If it exists, {@link #loadServicesList(ClassLoader, Class) loads the first in the list of implementation objects of service interface type}.
     * @throws IllegalArgumentException If the implementation class that does not define <code>serviceInterfaceType<code> is in the configuration file
     *                                  META-INF/services/<code>serviceInterfaceType<code>, IllegalArgumentException will be thrown
     * @version 1.0.0
     * @since 1.0.0
     */
    public static <T> T loadFirstService(ClassLoader classLoader, Class<T> serviceInterfaceType) throws IllegalArgumentException {
        List<T> serviceList = loadServicesList0(classLoader, serviceInterfaceType);
        return serviceList.get(0);
    }

    /**
     * Loads the last in the list of objects implementing the service interface type, if present.
     * <p/>
     * <p/>
     * Design Purpose : Using the hierarchy of {@link ClassLoader}, once the configuration file is loaded in the parent's ClassLoader at a higher level (here the highest-level ClassLoader is Bootstrap ClassLoader)
     * /META-INF/services/<code>serviceInterfaceType</code>
     * If the last implementation class is used, the lower-level Class Loader will not be able to override the previous definition。
     *
     * @param <T>                  service interface type
     * @param serviceInterfaceType
     * @return Loads the last in the list of objects implementing the service interface type, if present.
     * @throws IllegalArgumentException see {@link #loadServicesList(ClassLoader, Class)}
     * @version 1.0.0
     * @since 1.0.0
     */
    public static <T> T loadLastService(ClassLoader classLoader, Class<T> serviceInterfaceType) throws IllegalArgumentException {
        List<T> serviceList = loadServicesList0(classLoader, serviceInterfaceType);
        return serviceList.get(serviceList.size() - 1);
    }

}
