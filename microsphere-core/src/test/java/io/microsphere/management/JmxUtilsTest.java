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
import java.util.Map;

import static io.microsphere.management.JmxUtils.getMBeanAttributesMap;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static javax.management.ObjectName.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
