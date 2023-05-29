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
package io.microsphere.micrometer.util;

import io.microsphere.util.BaseUtils;
import io.micrometer.core.instrument.util.NamedThreadFactory;

import java.util.concurrent.ExecutorService;

import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * The utilities class for Micrometer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MicrometerUtils extends BaseUtils {

    private static ExecutorService asyncExecutor = newSingleThreadExecutor(new NamedThreadFactory("Micrometer-Async-"));

    static {
        addShutdownHookCallback(asyncExecutor::shutdownNow);
    }

    /**
     * Execute the task asynchronously
     *
     * @param task {@link Runnable} task
     */
    public static void async(Runnable task) {
        if (task != null) {
            asyncExecutor.execute(task);
        }
    }
}
