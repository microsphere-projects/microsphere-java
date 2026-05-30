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
package io.microsphere.process;

import io.microsphere.lang.Prioritized;

/**
 * A strategy interface for resolving the current process ID.
 *
 * <p>Implementations of this interface are responsible for determining the identifier
 * of the currently running process. Different implementations may be used depending on
 * the runtime environment or operating system.
 *
 * <p>{@link ProcessIdResolver} extends {@link Prioritized}, allowing implementations to define
 * a priority value to influence the order in which they are considered when multiple resolvers
 * are available.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyProcessIdResolver implements ProcessIdResolver {
 *     public int getPriority() {
 *         return ProcessIdResolver.NORMAL_PRIORITY;
 *     }
 *
 *     public boolean supports() {
 *         return true; // or conditionally based on environment
 *     }
 *
 *     public Long current() {
 *         return 12345L; // simulate process ID
 *     }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ModernProcessIdResolver
 * @see ClassicProcessIdResolver
 * @see VirtualMachineProcessIdResolver
 * @since 1.0.0
 */
public interface ProcessIdResolver extends Prioritized {

    /**
     * The unknown process id
     */
    long UNKNOWN_PROCESS_ID = -1L;

    /**
     * Whether supports to resolve the process id or not?
     *
     * @return <code>true</code> if supports, otherwise <code>false</code>
     */
    boolean supports();

    /**
     * Resolve the current process id
     *
     * @return <code>>null</code> if can't be resolved
     */
    Long current();
}
