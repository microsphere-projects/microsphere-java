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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import io.micrometer.core.lang.Nullable;
import io.microsphere.management.MBeanAttribute;
import io.microsphere.micrometer.instrument.binder.AbstractMeterBinder;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.QueryExp;
import java.lang.management.ManagementFactory;
import java.util.Set;

import static io.microsphere.management.JmxUtils.getMBeanAttributes;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

/**
 * A {@link MeterBinder} for MBean(s)
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanServer
 * @see MBeanServerFactory
 * @see ManagementFactory#getPlatformMBeanServer()
 * @see MeterBinder
 * @since 1.0.0
 */
public class MBeanMetrics extends AbstractMeterBinder {

    private final MBeanServer mBeanServer;

    private final ObjectName objectNameToQuery;

    @Nullable
    private final QueryExp queryExp;

    private final MBeanAttributeMeterBinder[] attributeMeterBinders;

    private final int attributeMeterBindersCount;


    public MBeanMetrics(@NonNull ObjectName objectNameToQuery, MBeanAttributeMeterBinder... attributeMeterBinders) {
        this(getPlatformMBeanServer(), objectNameToQuery, attributeMeterBinders);
    }

    public MBeanMetrics(@Nullable MBeanServer mbeanserver, @NonNull ObjectName objectNameToQuery, MBeanAttributeMeterBinder... attributeMeterBinders) {
        this(mbeanserver, objectNameToQuery, null, attributeMeterBinders);
    }

    public MBeanMetrics(@NonNull MBeanServer mbeanserver, @NonNull ObjectName objectNameToQuery, @Nullable QueryExp queryExp,
                        MBeanAttributeMeterBinder... attributeMeterBinders) {
        this.mBeanServer = mbeanserver;
        this.objectNameToQuery = objectNameToQuery;
        this.queryExp = queryExp;
        this.attributeMeterBinders = attributeMeterBinders;
        this.attributeMeterBindersCount = attributeMeterBinders.length;
    }

    @Override
    protected boolean supports(MeterRegistry registry) {
        return attributeMeterBindersCount > 0;
    }

    @Override
    protected void doBindTo(MeterRegistry registry) throws Throwable {
        Set<ObjectName> objectNames = mBeanServer.queryNames(this.objectNameToQuery, this.queryExp);
        for (ObjectName objectName : objectNames) {
            MBeanAttribute[] mBeanAttributes = getMBeanAttributes(mBeanServer, objectName);
            int size = mBeanAttributes.length;
            for (int i = 0; i < size; i++) {
                MBeanAttribute mBeanAttribute = mBeanAttributes[i];
                doBindTo(objectName, mBeanAttribute, registry);
            }
        }
    }

    private void doBindTo(ObjectName mBeanName, MBeanAttribute mBeanAttribute, MeterRegistry registry) throws Throwable {
        MBeanInfo mBeanInfo = mBeanAttribute.getDeclaringMBeanInfo();
        MBeanAttributeInfo mBeanAttributeInfo = mBeanAttribute.getAttributeInfo();
        Object attributeValue = mBeanAttribute.getValue();
        for (int i = 0; i < attributeMeterBindersCount; i++) {
            MBeanAttributeMeterBinder binder = this.attributeMeterBinders[i];
            binder.bindTo(mBeanName, mBeanInfo, mBeanAttributeInfo, attributeValue, registry);
        }
    }
}

