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
package io.github.microsphere.micrometer.instrument.binder.system;

import com.sun.management.OperatingSystemMXBean;
import io.github.microsphere.micrometer.instrument.binder.AbstractMeterBinder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.util.Collections;

/**
 * System Memory Metrics based on JMX
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SystemMemoryMetrics extends AbstractMeterBinder {

    private static final String TARGET_CLASS_NAME = "com.sun.management.OperatingSystemMXBean";

    private static final boolean SUPPORTED = isSupported();

    private static boolean isSupported() {
        boolean supported = false;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            Class<?> mxbeanInterface = classLoader.loadClass(TARGET_CLASS_NAME);
            supported = ManagementFactory.getPlatformMXBean((Class<PlatformManagedObject>) mxbeanInterface) != null;
        } catch (ClassNotFoundException e) {
        }
        return supported;
    }

    public SystemMemoryMetrics() {
        this(Collections.emptyList());
    }

    public SystemMemoryMetrics(Iterable<Tag> tags) {
        super(tags);
    }

    @Override
    protected boolean supports(MeterRegistry registry) {
        return SUPPORTED;
    }

    @Override
    protected void doBindTo(MeterRegistry registry) throws Throwable {

        OperatingSystemMXBean operatingSystemBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        Gauge.builder("memory.swap.space.total", operatingSystemBean, OperatingSystemMXBean::getTotalSwapSpaceSize)
                .tags(tags)
                .description("Total swap space size")
                .baseUnit(BaseUnits.BYTES)
                .strongReference(true)
                .register(registry);

        Gauge.builder("memory.swap.space.free", operatingSystemBean, OperatingSystemMXBean::getFreeSwapSpaceSize)
                .tags(tags)
                .description("Free swap space size")
                .baseUnit(BaseUnits.BYTES)
                .strongReference(true)
                .register(registry);

        Gauge.builder("memory.committed.virtual", operatingSystemBean, OperatingSystemMXBean::getCommittedVirtualMemorySize)
                .tags(tags)
                .description("Committed virtual memory size")
                .baseUnit(BaseUnits.BYTES)
                .strongReference(true)
                .register(registry);

        Gauge.builder("memory.physical.total", operatingSystemBean, OperatingSystemMXBean::getTotalPhysicalMemorySize)
                .tags(tags)
                .description("Total physical memory size")
                .baseUnit(BaseUnits.BYTES)
                .strongReference(true)
                .register(registry);

        Gauge.builder("memory.physical.free", operatingSystemBean, OperatingSystemMXBean::getFreePhysicalMemorySize)
                .tags(tags)
                .description("Free physical memory size")
                .baseUnit(BaseUnits.BYTES)
                .strongReference(true)
                .register(registry);
    }
}
