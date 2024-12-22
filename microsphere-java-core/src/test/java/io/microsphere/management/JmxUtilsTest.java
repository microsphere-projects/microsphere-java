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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.microsphere.management.JmxUtils.getMBeanAttributesMap;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static javax.management.ObjectName.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JmxUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JmxUtilsTest extends AbstractTestCase {

    private MBeanServer mBeanServer;

    private ObjectName objectName;

    @BeforeEach
    public void init() throws Throwable {
        this.mBeanServer = getPlatformMBeanServer();
        this.objectName = getInstance("java.lang:type=ClassLoading");
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
    public void testPlatformMXBeans() throws Throwable {
        assertNull(JmxUtils.classLoadingMXBean);
        assertNull(JmxUtils.memoryMXBean);
        assertNull(JmxUtils.threadMXBean);
        assertNull(JmxUtils.runtimeMXBean);
        assertNull(JmxUtils.compilationMXBean);
        assertNull(JmxUtils.operatingSystemMXBean);
        assertNull(JmxUtils.memoryPoolMXBeans);
        assertNull(JmxUtils.memoryManagerMXBeans);
        assertNull(JmxUtils.garbageCollectorMXBeans);

        assertPlatformMXBean(JmxUtils::getClassLoadingMXBean, ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
        assertPlatformMXBean(JmxUtils::getMemoryMXBean, ManagementFactory.MEMORY_MXBEAN_NAME);
        assertPlatformMXBean(JmxUtils::getThreadMXBean, ManagementFactory.THREAD_MXBEAN_NAME);
        assertPlatformMXBean(JmxUtils::getRuntimeMXBean, ManagementFactory.RUNTIME_MXBEAN_NAME);
        assertPlatformMXBean(JmxUtils.getCompilationMXBean(), ManagementFactory.COMPILATION_MXBEAN_NAME);
        assertPlatformMXBean(JmxUtils::getOperatingSystemMXBean, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        assertPlatformMXBeans(JmxUtils.getMemoryPoolMXBeans(), ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE);
        assertTrue(JmxUtils.getMemoryManagerMXBeans().size() > 0);
        assertPlatformMXBeans(JmxUtils.getGarbageCollectorMXBeans(), ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE);
    }

    private void assertPlatformMXBean(Optional<? extends PlatformManagedObject> platformMXBean, String name) throws Throwable {
        if (platformMXBean.isPresent()) {
            return;
        }
        assertPlatformMXBean(platformMXBean.get(), name);
    }

    private void assertPlatformMXBean(Supplier<PlatformManagedObject> platformMXBean, String name) throws Throwable {
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
