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

import org.junit.jupiter.api.Test;

import static io.microsphere.util.Compatible.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Compatible} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CompatibleTest {

    private Compatible<Test, String> compatible = of(Test.class)
            .on("<", "4.13", version -> "<")
            .on("=", "4.13", version -> "=")
            .on(">", "4.2", version -> ">")
            .on(">=", "4.13.2", version -> ">=");

    @Test
    public void testGet() {
        assertEquals(">=", compatible.get());
    }

    @Test
    public void testAccept() {
        compatible.accept(r -> assertEquals(">=", r));
    }
}
