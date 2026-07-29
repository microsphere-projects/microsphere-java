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

package io.microsphere.io.serializer;


import static io.microsphere.io.serializer.LongSerializer.LONG_SERIALIZER;
import static java.lang.Long.MAX_VALUE;

/**
 * {@link LongSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LongSerializer
 * @since 1.0.0
 */
class LongSerializerTest extends AbstractSerializerTest<Long> {

    @Override
    protected AbstractSerializer<Long> getSerializer() {
        return LONG_SERIALIZER;
    }

    @Override
    protected Long getValue() {
        return MAX_VALUE;
    }

}