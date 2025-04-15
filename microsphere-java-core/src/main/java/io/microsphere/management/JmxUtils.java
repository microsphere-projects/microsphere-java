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
 * The utilities class for JMX
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
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
     * @return a {@link ClassLoadingMXBean} object for
     * the Java virtual machine.
     * @see {@link ManagementFactory#getClassLoadingMXBean()}
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
     * @return a {@link MemoryMXBean} object for the Java virtual machine.
     * @see {@link ManagementFactory#getMemoryMXBean()}
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
     * @return a {@link ThreadMXBean} object for the Java virtual machine.
     * @see {@link ManagementFactory#getThreadMXBean()}
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
     * @return a {@link RuntimeMXBean} object for the Java virtual machine.
     * @see {@link ManagementFactory#getRuntimeMXBean()}
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
     * This method returns {@code null} if the Java virtual machine has no compilation system.
     *
     * @return an instance of {@link Optional} holding a {@link CompilationMXBean} object for the Java virtual machine
     * or {@code null if the Java virtual machine has no compilation system.
     * @see {@link ManagementFactory#getCompilationMXBean()}
     */
    @Nullable
    public static Optional<CompilationMXBean> getCompilationMXBean() {
        if (compilationMXBean == null) {
            compilationMXBean = ofNullable(ManagementFactory.getCompilationMXBean());
        }
        return compilationMXBean;
    }

    /**
     * Returns the managed bean for the operating system on which
     * the Java virtual machine is running.
     *
     * @return an {@link OperatingSystemMXBean} object for
     * the Java virtual machine.
     * @see {@link ManagementFactory#getOperatingSystemMXBean()}
     */
    @Nonnull
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        if (operatingSystemMXBean == null) {
            operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        }
        return operatingSystemMXBean;
    }

    /**
     * Returns a list of {@link MemoryPoolMXBean} objects in the Java virtual machine.
     * The Java virtual machine can have one or more memory pools.
     * It may add or remove memory pools during execution.
     *
     * @return a list of {@code MemoryPoolMXBean} objects.
     * @see {@link ManagementFactory#getMemoryPoolMXBeans()}
     */
    @Nonnull
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        if (memoryPoolMXBeans == null) {
            memoryPoolMXBeans = unmodifiableList(ManagementFactory.getMemoryPoolMXBeans());
        }
        return memoryPoolMXBeans;
    }

    /**
     * Returns a list of {@link MemoryManagerMXBean} objects in the Java virtual machine.
     * The Java virtual machine can have one or more memory managers.
     * It may add or remove memory managers during execution.
     *
     * @return a list of {@code MemoryManagerMXBean} objects.
     * @see {@link ManagementFactory#getMemoryManagerMXBeans()}
     */
    @Nonnull
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        if (memoryManagerMXBeans == null) {
            memoryManagerMXBeans = unmodifiableList(ManagementFactory.getMemoryManagerMXBeans());
        }
        return memoryManagerMXBeans;
    }

    /**
     * Returns a list of {@link GarbageCollectorMXBean} objects in the Java virtual machine.
     * The Java virtual machine may have one or more {@code GarbageCollectorMXBean} objects.
     * It may add or remove {@code GarbageCollectorMXBean} during execution.
     *
     * @return a list of {@code GarbageCollectorMXBean} objects.
     * @see {@link ManagementFactory#getGarbageCollectorMXBeans()}
     */
    @Nonnull
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        if (garbageCollectorMXBeans == null) {
            garbageCollectorMXBeans = unmodifiableList(ManagementFactory.getGarbageCollectorMXBeans());
        }
        return garbageCollectorMXBeans;
    }

    /**
     * Get the {@link Map} with the attribute name and {@link MBeanAttribute MBeanAttributes} from the specified named
     * MBean and its' registered {@link MBeanServer}
     *
     * @param mBeanServer {@link MBeanServer}
     * @param objectName  the name of MBean
     * @return non-null read-only {@link Map}
     */
    @Nonnull
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
     * Get the {@link MBeanAttribute MBeanAttributes} from the specified named MBean and its' registered {@link MBeanServer}
     * Note that the array of {@link MBeanAttribute MBeanAttributes} is the same order of {@link MBeanInfo#getAttributes()}
     *
     * @param mBeanServer {@link MBeanServer}
     * @param objectName  the name of MBean
     * @return non-null array
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

    public static Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        return doGetAttribute(mBeanServer, objectName, attributeInfo, attributeInfo.getName());
    }

    public static Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        return doGetAttribute(mBeanServer, objectName, null, attributeName);
    }

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

    public static MBeanInfo getMBeanInfo(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanInfo mBeanInfo = null;
        try {
            mBeanInfo = mBeanServer.getMBeanInfo(objectName);
        } catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
            handleException(e, mBeanServer, objectName);
        }
        return mBeanInfo;
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
