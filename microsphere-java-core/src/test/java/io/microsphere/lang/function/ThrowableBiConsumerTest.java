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

package io.microsphere.lang.function;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

/**
 * {@link ThrowableBiConsumer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ThrowableBiConsumer
 * @since 1.0.0
 */
class ThrowableBiConsumerTest extends AbstractTestCase {

    @Test
    void testAccept() throws Throwable {
        ThrowableBiConsumer<String, Integer> consumer = (s, i) -> {
            logger.info("The String is : {}, the Integer is : {}", s, i);
        };
        consumer.accept("Mercy", 1);
    }

    @Test
    void testAndThen() throws Throwable {
        ThrowableBiConsumer<String, Integer> consumer = (s, i) -> {
            logger.info("The String is : {}, the Integer is : {}", s, i);
        };
        consumer.andThen((s, i) -> {
            logger.info("andThen -> The String is : {}, the Integer is : {}", s, i);
        }).accept("Mercy", 1);
    }
}
