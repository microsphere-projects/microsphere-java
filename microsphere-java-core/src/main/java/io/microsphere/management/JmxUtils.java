/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.management;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 * The utilities class for JMX operations, providing convenient methods to interact with MBeans and MXBeans.
 * <p>
 * This class offers static utility methods to retrieve and manipulate JMX MBeans and attributes,
 * as well as access various platform MXBean components such as memory, thread, and garbage collection metrics.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanAttribute
 * @see Utils
 * @since 1.0.0
 */
public abstract class JmxUtils implements Utils {

    private static final Logger logger = getLogger(JmxUtils.class);

    public static final MBeanAttribute[] EMPTY_MBEAN_ATTRIBUTE_ARRAY = new MBeanAttribute[0];

    private static ClassLoadingMXBean classLoadingMXBean;

    private static MemoryMXBean memoryMXBean;

    private static ThreadMXBean threadMXBean;

    private static RuntimeMXBean runtimeMXBean;

    private static Optional<CompilationMXBean> compilationMXBean;

    private static OperatingSystemMXBean operatingSystemMXBean;

    private static List<MemoryPoolMXBean> memoryPoolMXBeans;

    private static List<MemoryManagerMXBean> memoryManagerMXBeans;

    private static List<GarbageCollectorMXBean> garbageCollectorMXBeans;

    /**
     * Returns the managed bean for the class loading system of the Java virtual machine.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassLoadingMXBean classLoadingMXBean = JmxUtils.getClassLoadingMXBean();
     * long loadedClassCount = classLoadingMXBean.getLoadedClassCount();
     * System.out.println("Loaded class count: " + loadedClassCount);
     * }</pre>
     *
     * @return a {@link ClassLoadingMXBean} object for the Java virtual machine.
     * @see ManagementFactory#getClassLoadingMXBean()
     */
    @Nonnull
    public static ClassLoadingMXBean getClassLoadingMXBean() {
        if (classLoadingMXBean == null) {
            classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        }
        return classLoadingMXBean;
    }

    /**
     * Returns the managed bean for the memory system of the Java virtual machine.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MemoryMXBean memoryMXBean = JmxUtils.getMemoryMXBean();
     * MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
     * System.out.println("Heap memory usage: " + heapMemoryUsage);
     * }</pre>
     *
     * @return a {@link MemoryMXBean} object for the Java virtual machine.
     * @see ManagementFactory#getMemoryMXBean()
     */
    @Nonnull
    public static MemoryMXBean getMemoryMXBean() {
        if (memoryMXBean == null) {
            memoryMXBean = ManagementFactory.getMemoryMXBean();
        }
        return memoryMXBean;
    }

    /**
     * Returns the managed bean for the thread system of the Java virtual machine.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ThreadMXBean threadMXBean = JmxUtils.getThreadMXBean();
     * long threadCount = threadMXBean.getThreadCount();
     * System.out.println("Current thread count: " + threadCount);
     * }</pre>
     *
     * @return a {@link ThreadMXBean} object for the Java virtual machine.
     * @see ManagementFactory#getThreadMXBean()
     */
    @Nonnull
    public static ThreadMXBean getThreadMXBean() {
        if (threadMXBean == null) {
            threadMXBean = ManagementFactory.getThreadMXBean();
        }
        return threadMXBean;
    }

    /**
     * Returns the managed bean for the runtime system of the Java virtual machine.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * RuntimeMXBean runtimeMXBean = JmxUtils.getRuntimeMXBean();
     * String jvmName = runtimeMXBean.getName();
     * System.out.println("JVM Name: " + jvmName);
     * }</pre>
     *
     * @return a {@link RuntimeMXBean} object for the Java virtual machine.
     * @see ManagementFactory#getRuntimeMXBean()
     */
    @Nonnull
    public static RuntimeMXBean getRuntimeMXBean() {
        if (runtimeMXBean == null) {
            runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        }
        return runtimeMXBean;
    }

    /**
     * Returns the managed bean for the compilation system of the Java virtual machine.
     * This method returns an empty {@link Optional} if the Java virtual machine has no compilation system.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Optional<CompilationMXBean> compilationMXBean = JmxUtils.getCompilationMXBean();
     * if (compilationMXBean.isPresent()) {
     *     CompilationMXBean bean = compilationMXBean.get();
     *     String compilerName = bean.getName();
     *     System.out.println("Compiler name: " + compilerName);
     * } else {
     *     System.out.println("No compilation MXBean available.");
     * }
     * }</pre>
     *
     * @return an instance of {@link Optional} containing a {@link CompilationMXBean} object for the Java virtual machine,
     * or an empty Optional if the Java virtual machine has no compilation system.
     * @see ManagementFactory#getCompilationMXBean()
     */
    @Nonnull
    public static Optional<CompilationMXBean> getCompilationMXBean() {
        if (compilationMXBean == null) {
            compilationMXBean = ofNullable(ManagementFactory.getCompilationMXBean());
        }
        return compilationMXBean;
    }

    /**
     * Returns the managed bean for the operating system on which the Java virtual machine is running.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * OperatingSystemMXBean osMXBean = JmxUtils.getOperatingSystemMXBean();
     * String osName = osMXBean.getName();
     * String version = osMXBean.getVersion();
     * int availableProcessors = osMXBean.getAvailableProcessors();
     * System.out.println("Operating System: " + osName);
     * System.out.println("Version: " + version);
     * System.out.println("Available Processors: " + availableProcessors);
     * }</pre>
     *
     * @return an {@link OperatingSystemMXBean} object for the Java virtual machine.
     * @see ManagementFactory#getOperatingSystemMXBean()
     */
    @Nonnull
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        if (operatingSystemMXBean == null) {
            operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        }
        return operatingSystemMXBean;
    }

    /**
     * Returns an unmodifiable list of {@link MemoryPoolMXBean} objects representing the memory pools in the Java virtual machine.
     * <p>
     * The Java virtual machine can have one or more memory pools, and this method provides access to them.
     * The returned list is unmodifiable and reflects the current state of the JVM's memory pools.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<MemoryPoolMXBean> memoryPools = JmxUtils.getMemoryPoolMXBeans();
     * for (MemoryPoolMXBean pool : memoryPools) {
     *     String poolName = pool.getName();
     *     MemoryUsage usage = pool.getUsage();
     *     System.out.println("Memory Pool: " + poolName + ", Usage: " + usage);
     * }
     * }</pre>
     *
     * @return a non-null, unmodifiable list of {@link MemoryPoolMXBean} objects.
     */
    @Nonnull
    @Immutable
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        if (memoryPoolMXBeans == null) {
            memoryPoolMXBeans = unmodifiableList(ManagementFactory.getMemoryPoolMXBeans());
        }
        return memoryPoolMXBeans;
    }

    /**
     * Returns a list of {@link MemoryManagerMXBean} objects in the Java virtual machine.
     * The Java virtual machine may have one or more memory managers, and this method provides access to them.
     * <p>
     * The returned list reflects the current state of the JVM's memory managers and may change over time as
     * memory managers are added or removed during execution.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<MemoryManagerMXBean> memoryManagers = JmxUtils.getMemoryManagerMXBeans();
     * for (MemoryManagerMXBean manager : memoryManagers) {
     *     String managerName = manager.getName();
     *     boolean isVerbose = manager.isVerbose();
     *     System.out.println("Memory Manager: " + managerName + ", Verbose: " + isVerbose);
     * }
     * }</pre>
     *
     * @return a non-null, unmodifiable list of {@link MemoryManagerMXBean} objects representing the memory managers
     * in the Java virtual machine.
     * @see ManagementFactory#getMemoryManagerMXBeans()
     */
    @Nonnull
    @Immutable
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        if (memoryManagerMXBeans == null) {
            memoryManagerMXBeans = unmodifiableList(ManagementFactory.getMemoryManagerMXBeans());
        }
        return memoryManagerMXBeans;
    }

    /**
     * Returns a list of {@link GarbageCollectorMXBean} objects in the Java virtual machine.
     * The Java virtual machine may have one or more garbage collectors, and this method provides access to them.
     * <p>
     * The returned list reflects the current state of the JVM's garbage collectors and may change over time as
     * garbage collectors are added or removed during execution.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<GarbageCollectorMXBean> garbageCollectors = JmxUtils.getGarbageCollectorMXBeans();
     * for (GarbageCollectorMXBean gc : garbageCollectors) {
     *     String gcName = gc.getName();
     *     long collectionCount = gc.getCollectionCount();
     *     System.out.println("Garbage Collector: " + gcName + ", Collection Count: " + collectionCount);
     * }
     * }</pre>
     *
     * @return a non-null, unmodifiable list of {@link GarbageCollectorMXBean} objects representing the garbage collectors
     * in the Java virtual machine.
     * @see ManagementFactory#getGarbageCollectorMXBeans()
     */
    @Nonnull
    @Immutable
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        if (garbageCollectorMXBeans == null) {
            garbageCollectorMXBeans = unmodifiableList(ManagementFactory.getGarbageCollectorMXBeans());
        }
        return garbageCollectorMXBeans;
    }

    /**
     * Retrieves a read-only map of MBean attributes for the specified MBean registered in the given MBeanServer.
     * The keys are attribute names, and the values are corresponding {@link MBeanAttribute} instances.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     * ObjectName objectName = new ObjectName("java.lang", "type", "Memory");
     *
     * Map<String, MBeanAttribute> attributesMap = JmxUtils.getMBeanAttributesMap(mBeanServer, objectName);
     *
     * for (Map.Entry<String, MBeanAttribute> entry : attributesMap.entrySet()) {
     *     String attributeName = entry.getKey();
     *     MBeanAttribute mBeanAttribute = entry.getValue();
     *     System.out.println("Attribute Name: " + attributeName);
     *     System.out.println("Attribute Info: " + mBeanAttribute.getAttributeInfo());
     *     System.out.println("Attribute Value: " + mBeanAttribute.getValue());
     * }
     * }</pre>
     *
     * @param mBeanServer the MBeanServer from which to retrieve the attributes
     * @param objectName  the name of the MBean whose attributes are to be retrieved
     * @return a non-null, unmodifiable map where the keys are attribute names and the values are MBeanAttribute instances
     */
    @Nonnull
    @Immutable
    public static Map<String, MBeanAttribute> getMBeanAttributesMap(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanAttribute[] mBeanAttributes = getMBeanAttributes(mBeanServer, objectName);
        int length = mBeanAttributes.length;
        if (length == 0) {
            return emptyMap();
        }
        Map<String, MBeanAttribute> mBeanAttributesMap = newHashMap(length);
        for (int i = 0; i < length; i++) {
            MBeanAttribute mBeanAttribute = mBeanAttributes[i];
            String attributeName = mBeanAttribute.getName();
            mBeanAttributesMap.put(attributeName, mBeanAttribute);
        }
        return unmodifiableMap(mBeanAttributesMap);
    }

    /**
     * Retrieves an array of {@link MBeanAttribute} objects representing the attributes of the specified MBean.
     * <p>
     * This method fetches all attributes from the MBean registered under the given {@link ObjectName} in the provided
     * {@link MBeanServer}. Each attribute is encapsulated in an {@link MBeanAttribute} instance, which includes both the
     * metadata ({@link MBeanAttributeInfo}) and the current value of the attribute.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     * ObjectName objectName = new ObjectName("java.lang", "type", "Memory");
     *
     * MBeanAttribute[] mBeanAttributes = JmxUtils.getMBeanAttributes(mBeanServer, objectName);
     *
     * for (MBeanAttribute attr : mBeanAttributes) {
     *     System.out.println("Attribute Name: " + attr.getName());
     *     System.out.println("Attribute Type: " + attr.getType());
     *     System.out.println("Attribute Value: " + attr.getValue());
     * }
     * }</pre>
     *
     * @param mBeanServer the MBeanServer from which to retrieve the MBean attributes
     * @param objectName  the name of the MBean whose attributes are to be retrieved
     * @return a non-null array of {@link MBeanAttribute} objects representing the attributes of the specified MBean
     */
    @Nonnull
    public static MBeanAttribute[] getMBeanAttributes(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanInfo mBeanInfo = getMBeanInfo(mBeanServer, objectName);
        if (mBeanInfo == null) {
            return EMPTY_MBEAN_ATTRIBUTE_ARRAY;
        }
        MBeanAttributeInfo[] attributeInfoList = mBeanInfo.getAttributes();
        int size = attributeInfoList.length;
        MBeanAttribute[] mBeanAttributes = new MBeanAttribute[size];
        for (int i = 0; i < size; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfoList[i];
            Object attributeValue = getAttribute(mBeanServer, objectName, attributeInfo);
            MBeanAttribute mBeanAttribute = new MBeanAttribute(mBeanInfo, attributeInfo, attributeValue);
            mBeanAttributes[i] = mBeanAttribute;
        }
        return mBeanAttributes;
    }

    /**
     * Retrieves the value of the specified MBean attribute from the given MBean registered in the MBeanServer.
     * <p>
     * This method uses the provided {@link MBeanAttributeInfo} to determine the name and other metadata of the attribute,
     * then fetches its current value from the MBean.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     * ObjectName objectName = new ObjectName("java.lang", "type", "Memory");
     *
     * MBeanAttributeInfo attributeInfo = JmxUtils.findMBeanAttributeInfo(mBeanServer, objectName, "HeapMemoryUsage");
     * if (attributeInfo != null) {
     *     Object heapMemoryUsage = JmxUtils.getAttribute(mBeanServer, objectName, attributeInfo);
     *     System.out.println("Heap Memory Usage: " + heapMemoryUsage);
     * }
     * }</pre>
     *
     * @param mBeanServer   the MBeanServer from which to retrieve the attribute value
     * @param objectName    the name of the MBean whose attribute is to be retrieved
     * @param attributeInfo the metadata of the attribute whose value is to be retrieved
     * @return the current value of the MBean attribute, or {@code null} if the attribute is not readable or an error occurs
     */
    @Nullable
    public static Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        return doGetAttribute(mBeanServer, objectName, attributeInfo, attributeInfo.getName());
    }

    /**
     * Retrieves the value of the specified MBean attribute from the given MBean registered in the MBeanServer.
     * <p>
     * This method fetches the current value of the attribute identified by the given name from the MBean registered under
     * the specified {@link ObjectName} in the provided {@link MBeanServer}. If the attribute is not readable or an error occurs,
     * {@code null} is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     * ObjectName objectName = new ObjectName("java.lang", "type", "Memory");
     * String attributeName = "HeapMemoryUsage";
     *
     * Object heapMemoryUsage = JmxUtils.getAttribute(mBeanServer, objectName, attributeName);
     * System.out.println("Heap Memory Usage: " + heapMemoryUsage);
     * }</pre>
     *
     * @param mBeanServer   the MBeanServer from which to retrieve the attribute value
     * @param objectName    the name of the MBean whose attribute is to be retrieved
     * @param attributeName the name of the attribute whose value is to be retrieved
     * @return the current value of the MBean attribute, or {@code null} if the attribute is not readable or an error occurs
     */
    @Nullable
    public static Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        return doGetAttribute(mBeanServer, objectName, null, attributeName);
    }

    /**
     * Retrieves the metadata ({@link MBeanAttributeInfo}) for a specific attribute of an MBean registered in the MBeanServer.
     * <p>
     * This method searches through the attributes of the specified MBean, identified by its {@link ObjectName},
     * to find an attribute with the given name. If found, it returns the corresponding {@link MBeanAttributeInfo};
     * otherwise, it returns {@code null} and logs a warning if the attribute is not found.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     * ObjectName objectName = new ObjectName("java.lang", "type", "Memory");
     * String attributeName = "HeapMemoryUsage";
     *
     * MBeanAttributeInfo attributeInfo = JmxUtils.findMBeanAttributeInfo(mBeanServer, objectName, attributeName);
     * if (attributeInfo != null) {
     *     System.out.println("Attribute Info: " + attributeInfo.getDescription());
     * }
     * }</pre>
     *
     * @param mBeanServer   the MBeanServer from which to retrieve the MBean attribute info
     * @param objectName    the name of the MBean whose attribute info is to be retrieved
     * @param attributeName the name of the attribute for which metadata is requested
     * @return the {@link MBeanAttributeInfo} for the specified attribute if found; {@code null} otherwise
     */
    @Nullable
    public static MBeanAttributeInfo findMBeanAttributeInfo(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        MBeanInfo mBeanInfo = getMBeanInfo(mBeanServer, objectName);
        if (mBeanInfo == null) {
            return null;
        }
        MBeanAttributeInfo targetAttributeInfo = null;
        MBeanAttributeInfo[] attributeInfoList = mBeanInfo.getAttributes();
        int size = attributeInfoList.length;
        for (int i = 0; i < size; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfoList[i];
            if (Objects.equals(attributeName, attributeInfo.getName())) {
                targetAttributeInfo = attributeInfo;
                break;
            }
        }
        if (targetAttributeInfo == null) {
            handleAttributeNotFoundException(new AttributeNotFoundException(), mBeanServer, objectName, mBeanInfo, attributeName);
        }

        return targetAttributeInfo;
    }

    /**
     * Retrieves the metadata ({@link MBeanInfo}) for the specified MBean registered in the MBeanServer.
     * <p>
     * This method fetches the {@link MBeanInfo} for the MBean identified by the given {@link ObjectName}.
     * The MBeanInfo contains detailed information about the MBean, including its attributes, operations,
     * constructors, and notifications.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     * ObjectName objectName = new ObjectName("java.lang", "type", "Memory");
     *
     * MBeanInfo mBeanInfo = JmxUtils.getMBeanInfo(mBeanServer, objectName);
     * if (mBeanInfo != null) {
     *     System.out.println("MBean Description: " + mBeanInfo.getDescription());
     *     System.out.println("MBean Class Name: " + mBeanInfo.getClassName());
     * }
     * }</pre>
     *
     * @param mBeanServer the MBeanServer from which to retrieve the MBeanInfo
     * @param objectName  the name of the MBean whose metadata is to be retrieved
     * @return the {@link MBeanInfo} for the specified MBean if found; {@code null} otherwise
     */
    public static MBeanInfo getMBeanInfo(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanInfo mBeanInfo = null;
        try {
            mBeanInfo = mBeanServer.getMBeanInfo(objectName);
        } catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
            handleException(e, mBeanServer, objectName);
        }
        return mBeanInfo;
    }

    protected static Object doGetAttribute(MBeanServer mBeanServer, ObjectName objectName, @Nullable MBeanAttributeInfo attributeInfo,
                                           String attributeName) {
        MBeanAttributeInfo mBeanAttributeInfo = attributeInfo;
        if (mBeanAttributeInfo == null) {
            mBeanAttributeInfo = findMBeanAttributeInfo(mBeanServer, objectName, attributeName);
        }
        if (mBeanAttributeInfo == null) {
            return null;
        }

        if (!mBeanAttributeInfo.isReadable()) {
            return null;
        }
        Object attributeValue = null;
        try {
            attributeValue = mBeanServer.getAttribute(objectName, attributeName);
        } catch (ReflectionException | InstanceNotFoundException e) {
            handleException(e, mBeanServer, objectName);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return attributeValue;
    }

    private static void handleException(Exception e, MBeanServer mBeanServer, ObjectName objectName) {
        if (logger.isWarnEnabled()) {
            logger.warn("the MBean[name : '{}'] can't be manipulated by the Reflection in the MBeanServer[default domain : '{}' , domains : {}]",
                    objectName.getCanonicalName(),
                    mBeanServer.getDefaultDomain(),
                    arrayToString(mBeanServer.getDomains()),
                    e
            );
        }
    }

    private static void handleAttributeNotFoundException(AttributeNotFoundException e, MBeanServer mBeanServer,
                                                         ObjectName objectName, MBeanInfo mBeanInfo, String attributeName) {

        if (logger.isWarnEnabled()) {
            logger.warn("The attribute[name : '{}' ] of MBean[name : '{}'] can't be found in the MBeanServer[default domain : '{}' , domains : {}]",
                    attributeName,
                    objectName.getCanonicalName(),
                    mBeanServer.getDefaultDomain(),
                    arrayToString(mBeanServer.getDomains()),
                    e
            );
        }
    }

    private JmxUtils() {
    }
}
