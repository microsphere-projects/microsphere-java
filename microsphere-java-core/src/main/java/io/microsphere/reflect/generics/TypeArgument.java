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
package io.microsphere.reflect.generics;

import java.lang.reflect.Type;
import java.util.Objects;

import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.Assert.assertTrue;

/**
 * {@link Type} Argument
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TypeArgument {

    private final Type type;

    private final int index;

    protected TypeArgument(Type type, int index) {
        assertNotNull(type, () -> "The 'type' must not be null");
        assertTrue(index > -1, () -> "The 'index' must not be positive");
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {
        return "TypeArgument{" +
                "type=" + type +
                ", index=" + index +
                '}';
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeArgument that = (TypeArgument) o;

        if (index != that.index) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + index;
        return result;
    }


    public static TypeArgument create(Type type, int index) {
        return new TypeArgument(type, index);
    }
}
