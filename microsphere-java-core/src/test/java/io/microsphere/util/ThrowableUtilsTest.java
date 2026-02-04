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


import io.microsphere.lang.function.ThrowableAction;
import org.junit.jupiter.api.Test;

import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.util.ThrowableUtils.getRootCause;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ThrowableUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ThrowableUtils
 * @since 1.0.0
 */
class ThrowableUtilsTest {

    @Test
    void testGetRootCause() {
        Throwable rootCause = new Throwable("Root Cause");
        Throwable throwable = new Throwable("Throwable", rootCause);
        assertSame(rootCause, getRootCause(throwable));

        ThrowableAction action = () -> {
            throw throwable;
        };

        execute(action, e -> {
            assertSame(rootCause, getRootCause(e));
        });

        assertThrows(NullPointerException.class, () -> {
            try {
                ThrowableAction a = () -> {
                    String s = null;
                    s.toString();
                };
                a.execute();
            } catch (Throwable e) {
                throw getRootCause(e);
            }
        });
    }
}