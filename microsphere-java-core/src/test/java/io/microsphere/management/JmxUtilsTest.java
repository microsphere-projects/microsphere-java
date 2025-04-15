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

import io.microsphere.AbstractTestCase;
import io.microsphere.junit.jupiter.api.extension.annotation.UtilsTestExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.PlatformManagedObject;
import java.util.Map;
import java.util.Optional;

import static io.microsphere.management.JmxUtils.EMPTY_MBEAN_ATTRIBUTE_ARRAY;
import static io.microsphere.management.JmxUtils.getAttribute;
import static io.microsphere.management.JmxUtils.getClassLoadingMXBean;
import static io.microsphere.management.JmxUtils.getCompilationMXBean;
import static io.microsphere.management.JmxUtils.getGarbageCollectorMXBeans;
import static io.microsphere.management.JmxUtils.getMBeanAttributes;
import static io.microsphere.management.JmxUtils.getMBeanAttributesMap;
import static io.microsphere.management.JmxUtils.getMemoryMXBean;
import static io.microsphere.management.JmxUtils.getMemoryManagerMXBeans;
import static io.microsphere.management.JmxUtils.getMemoryPoolMXBeans;
import static io.microsphere.management.JmxUtils.getOperatingSystemMXBean;
import static io.microsphere.management.JmxUtils.getRuntimeMXBean;
import static io.microsphere.management.JmxUtils.getThreadMXBean;
import static java.lang.management.ManagementFactory.CLASS_LOADING_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.COMPILATION_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE;
import static java.lang.management.ManagementFactory.MEMORY_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE;
import static java.lang.management.ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.RUNTIME_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Collections.emptyMap;
import static javax.management.ObjectName.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JmxUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@UtilsTestExtension
public class JmxUtilsTest extends AbstractTestCase {

    private static MBeanServer mBeanServer;

    private static ObjectName objectName;

    private static ObjectName notFoundObjectName;

    private static String notFoundAttributeName;

    @BeforeAll
    public static void init() throws Throwable {
        mBeanServer = getPlatformMBeanServer();
        objectName = getInstance("java.lang:type=ClassLoading");
        notFoundObjectName = getInstance("java.lang:type=NotFound");
        notFoundAttributeName = "NotFound";
    }

    @Test
    public void testGetClassLoadingMXBean() throws Throwable {
        assertPlatformMXBean(getClassLoadingMXBean(), CLASS_LOADING_MXBEAN_NAME);
        assertPlatformMXBean(getClassLoadingMXBean(), CLASS_LOADING_MXBEAN_NAME);
    }

    @Test
    public void testGetMemoryMXBean() throws Throwable {
        assertPlatformMXBean(getMemoryMXBean(), MEMORY_MXBEAN_NAME);
        assertPlatformMXBean(getMemoryMXBean(), MEMORY_MXBEAN_NAME);
    }

    @Test
    public void testGetThreadMXBean() throws Throwable {
        assertPlatformMXBean(getThreadMXBean(), THREAD_MXBEAN_NAME);
        assertPlatformMXBean(getThreadMXBean(), THREAD_MXBEAN_NAME);
    }

    @Test
    public void testGetRuntimeMXBean() throws Throwable {
        assertPlatformMXBean(getRuntimeMXBean(), RUNTIME_MXBEAN_NAME);
        assertPlatformMXBean(getRuntimeMXBean(), RUNTIME_MXBEAN_NAME);
    }

    @Test
    public void testGetCompilationMXBean() throws Throwable {
        assertPlatformMXBean(getCompilationMXBean(), COMPILATION_MXBEAN_NAME);
        assertPlatformMXBean(getCompilationMXBean(), COMPILATION_MXBEAN_NAME);
    }

    @Test
    public void testGetOperatingSystemMXBean() throws Throwable {
        assertPlatformMXBean(getOperatingSystemMXBean(), OPERATING_SYSTEM_MXBEAN_NAME);
        assertPlatformMXBean(getOperatingSystemMXBean(), OPERATING_SYSTEM_MXBEAN_NAME);
    }

    @Test
    public void testGetMemoryPoolMXBeans() throws Throwable {
        assertPlatformMXBeans(getMemoryPoolMXBeans(), MEMORY_POOL_MXBEAN_DOMAIN_TYPE);
        assertPlatformMXBeans(getMemoryPoolMXBeans(), MEMORY_POOL_MXBEAN_DOMAIN_TYPE);
    }

    @Test
    public void testGetMemoryManagerMXBeans() throws Throwable {
        assertTrue(getMemoryManagerMXBeans().size() > 0);
        assertTrue(getMemoryManagerMXBeans().size() > 0);
    }

    @Test
    public void testGetGarbageCollectorMXBeans() throws Throwable {
        assertPlatformMXBeans(getGarbageCollectorMXBeans(), GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE);
        assertPlatformMXBeans(getGarbageCollectorMXBeans(), GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE);
    }

    @Test
    public void testGetMBeanAttributes() {
        MBeanAttribute[] mBeanAttributes = getMBeanAttributes(mBeanServer, objectName);
        assertEquals(5, mBeanAttributes.length);
    }

    @Test
    public void testGetMBeanAttributesNotFound() {
        MBeanAttribute[] mBeanAttributes = getMBeanAttributes(mBeanServer, notFoundObjectName);
        assertSame(EMPTY_MBEAN_ATTRIBUTE_ARRAY, mBeanAttributes);
    }

    @Test
    public void testGetMBeanAttributesMap() {
        Map<String, MBeanAttribute> mBeanAttributesMap = getMBeanAttributesMap(mBeanServer, objectName);
        assertEquals(5, mBeanAttributesMap.size());
        assertTrue(mBeanAttributesMap.containsKey("Verbose"));
        assertTrue(mBeanAttributesMap.containsKey("TotalLoadedClassCount"));
        assertTrue(mBeanAttributesMap.containsKey("LoadedClassCount"));
        assertTrue(mBeanAttributesMap.containsKey("UnloadedClassCount"));
        assertTrue(mBeanAttributesMap.containsKey("ObjectName"));
    }

    @Test
    public void testGetMBeanAttributesMapOnMBeanNotFound() throws Throwable {
        Map<String, MBeanAttribute> mBeanAttributesMap = getMBeanAttributesMap(mBeanServer, notFoundObjectName);
        assertSame(emptyMap(), mBeanAttributesMap);
    }

    @Test
    public void testGetAttribute() {
        Object value = getAttribute(mBeanServer, objectName, "Verbose");
        assertNotNull(value);
        value = getAttribute(mBeanServer, objectName, "ObjectName");
        assertEquals(objectName, value);
    }

    @Test
    public void testGetAttributeOnMBeanNotFound() {
        Object value = getAttribute(mBeanServer, notFoundObjectName, "Verbose");
        assertNull(value);
    }

    @Test
    public void testGetAttributeOnAttributeNotFound() {
        Object value = getAttribute(mBeanServer, objectName, notFoundAttributeName);
        assertNull(value);
    }


    private void assertPlatformMXBean(Optional<? extends PlatformManagedObject> platformMXBean, String name) throws Throwable {
        if (platformMXBean.isPresent()) {
            return;
        }
        assertPlatformMXBean(platformMXBean.get(), name);
    }

    private void assertPlatformMXBeans(Iterable<? extends PlatformManagedObject> platformManagedObjects, String name) throws Throwable {
        for (PlatformManagedObject platformManagedObject : platformManagedObjects) {
            assertPlatformMXBean(platformManagedObject, name);
        }
    }

    private void assertPlatformMXBean(PlatformManagedObject platformManagedObject, String name) throws Throwable {
        ObjectName objectName = getInstance(name);
        ObjectName actualObjectName = platformManagedObject.getObjectName();
        assertEquals(objectName.getDomain(), actualObjectName.getDomain());
        assertEquals(objectName.getKeyProperty("type"), actualObjectName.getKeyProperty("type"));
    }
}
