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
package io.microsphere.micrometer.instrument.binder.jmx;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

/**
 * The Meter Binder for MBean Attribute
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Attribute
 * @see MBeanAttributeInfo
 * @see MBeanInfo
 * @see ObjectName
 * @see MeterRegistry
 * @see Meter
 * @since 1.0.0
 */
public interface MBeanAttributeMeterBinder {

    /**
     * Bind to {@link Meter}
     *
     * @param mBeanName          the {@link ObjectName} of MBean
     * @param mBeanInfo          the {@link MBeanInfo}
     * @param mBeanAttributeInfo the {@link MBeanAttributeInfo}
     * @param attributeValue     the value of {@link Attribute}
     * @param registry           {@link MeterRegistry} to be bound
     * @throws Throwable if any error caused
     */
    void bindTo(ObjectName mBeanName, MBeanInfo mBeanInfo, MBeanAttributeInfo mBeanAttributeInfo,
                Object attributeValue, MeterRegistry registry) throws Throwable;

}
