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

package io.microsphere.util;

import io.microsphere.Loggable;
import io.microsphere.lang.Prioritized;

/**
 * The ShutdownHook Callback
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ShutdownHookUtils
 * @since 1.0.0
 */
class ShutdownHookCallback extends Loggable implements Runnable, Prioritized {

    private final int priority;

    ShutdownHookCallback(int priority) {
        this.priority = priority;
    }

    @Override
    public void run() {
        log("Run an instance of ShutdownHookCallback[priority : {}] : {}", priority, this);
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
