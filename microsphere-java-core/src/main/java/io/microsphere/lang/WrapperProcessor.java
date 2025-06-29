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
package io.microsphere.lang;

/**
 * A processor interface for handling {@link Wrapper} instances.
 *
 * <p>Implementations of this interface can perform operations on a {@link Wrapper}
 * object, potentially modifying or enhancing the wrapper instance during processing.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class LoggingWrapperProcessor implements WrapperProcessor<MyWrapper> {
 *     public MyWrapper process(MyWrapper wrapper) {
 *         System.out.println("Processing wrapper: " + wrapper);
 *         return wrapper; // Return the same or modified instance
 *     }
 * }
 * }</pre>
 *
 * @param <W> the type of the wrapper that extends {@link Wrapper}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface WrapperProcessor<W extends Wrapper> {

    /**
     * Processes the {@link W Wrapper} bean
     *
     * @param wrapper {@link W Wrapper} instance
     * @return The processed {@link W Wrapper} instance that may be wrapped again
     */
    W process(W wrapper);

}

