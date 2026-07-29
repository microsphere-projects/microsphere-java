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

import static io.microsphere.io.IOUtils.CHAR_BYTES_SIZE;

/**
 * Java {@code char} or {@link Character} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSerializer
 * @since 1.0.0
 */
public class CharacterSerializer extends AbstractSerializer<Character> {

    public static final CharacterSerializer CHARACTER_SERIALIZER = new CharacterSerializer();

    @Override
    protected int calcBytesLength() {
        return CHAR_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Character character) {
        char c = character.charValue();
        return new byte[]{
                (byte) (c >>> 8), // High byte
                (byte) c          // Low byte
        };
    }

    @Override
    protected Character doDeserialize(byte[] bytes) {
        char c = (char) ((bytes[0] << 8) | (bytes[1] & 0xFF));
        return c;
    }
}
