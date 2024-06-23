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

import io.microsphere.logging.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import static io.microsphere.logging.LoggerFactory.getLogger;

public abstract class AbstractEventListener<E extends Event> implements EventListener<E> {

    private static final Logger logger = getLogger(AbstractEventListener.class);

    private final AtomicInteger eventOccurs = new AtomicInteger(0);

    @Override
    public final void onEvent(E event) {
        eventOccurs.getAndIncrement();
        handleEvent(event);
    }

    protected abstract void handleEvent(E event);

    public int getEventOccurs() {
        return eventOccurs.get();
    }

    protected void println(String message) {
        logger.info("[{}] {}", Thread.currentThread().getName(), message);
    }
}