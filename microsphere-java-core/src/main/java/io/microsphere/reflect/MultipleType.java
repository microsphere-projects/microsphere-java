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
package io.microsphere.reflect;

import java.lang.reflect.Type;
import java.util.Arrays;

import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.combineArray;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Objects.hash;

/**
 * Multiple {@link Type}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MultipleType {

    private final Type[] types;

    private MultipleType(Type... types) {
        this.types = types;
    }

    @Override
    public int hashCode() {
        return hash(types);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipleType that = (MultipleType) o;
        return Arrays.equals(types, that.types);
    }

    @Override
    public String toString() {
        return "MultipleType : " + arrayToString(types);
    }

    public static MultipleType of(Type one, Type two) {
        return new MultipleType(one, two);
    }

    public static MultipleType of(Type one, Type two, Type... others) {
        Type[] oneAndTwo = ofArray(one, two);
        Type[] types = combineArray(oneAndTwo, others);
        return new MultipleType(types);
    }
}
