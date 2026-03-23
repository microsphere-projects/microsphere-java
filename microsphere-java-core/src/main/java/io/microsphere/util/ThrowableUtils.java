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

/**
 * The uitlities class for {@link Throwable}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Throwable
 * @since 1.0.0
 */
public abstract class ThrowableUtils implements Utils {

    /**
     * Retrieves the root cause of the given {@link Throwable} by traversing the cause chain
     * until a throwable with no cause is found.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Exception root = new IllegalArgumentException("root");
     *   Exception wrapped = new RuntimeException("wrapped", root);
     *   Throwable result = ThrowableUtils.getRootCause(wrapped);
     *   System.out.println(result.getMessage()); // "root"
     * }</pre>
     *
     * @param throwable the throwable whose root cause is to be determined; must not be {@code null}
     * @return the root cause of the throwable chain
     * @since 1.0.0
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }


    private ThrowableUtils() {
    }
}
