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
package io.microsphere.event;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static java.util.concurrent.ForkJoinPool.commonPool;

/**
 * Parallel {@link EventDispatcher} implementation.
 * <p>
 * The default constructor uses {@link ForkJoinPool#commonPool() JDK common thread pool}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ForkJoinPool#commonPool()
 * @since 1.0.0
 */
public class ParallelEventDispatcher extends AbstractEventDispatcher {

    public ParallelEventDispatcher() {
        this(commonPool());
    }

    public ParallelEventDispatcher(Executor executor) {
        super(executor);
    }
}
