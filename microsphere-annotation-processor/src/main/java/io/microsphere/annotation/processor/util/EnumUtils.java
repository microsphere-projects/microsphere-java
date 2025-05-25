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

package io.microsphere.annotation.processor.util;

import io.microsphere.util.Utils;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

import static io.microsphere.util.ArrayUtils.length;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.OTHER;
import static javax.lang.model.element.ElementKind.valueOf;

/**
 * The utility class for {@link Enum}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Enum
 * @since 1.0.0
 */
public interface EnumUtils extends Utils {

    /**
     * Converts the specified {@link ElementType} to an equivalent {@link ElementKind}.
     * <p>
     * If the provided {@code elementType} is {@code null}, this method returns {@link ElementKind#OTHER}.
     * </p>
     *
     * @param elementType the ElementType to convert, may be {@code null}
     * @return the corresponding ElementKind, never {@code null}
     */
    static ElementKind toElementKind(ElementType elementType) {
        if (elementType == null) {
            return OTHER;
        }
        switch (elementType) {
            case TYPE:
            case TYPE_USE:
                return CLASS;
            default:
                return valueOf(elementType.name());
        }
    }

    /**
     * Checks whether the specified {@link ElementKind} matches the specified {@link ElementType}.
     *
     * @param elementKind the ElementKind to check
     * @param elementType the ElementType to check
     * @return {@code true} if the ElementKind matches the ElementType, {@code false} otherwise
     */
    static boolean matches(ElementKind elementKind, ElementType elementType) {
        return elementKind == toElementKind(elementType);
    }

    /**
     * Checks whether the specified {@link ElementKind} matches any of the specified {@link ElementType}s.
     *
     * @param elementKind  the ElementKind to check
     * @param elementTypes the ElementTypes to check
     * @return {@code true} if the ElementKind matches any of the ElementTypes, {@code false} otherwise
     */
    static boolean matches(ElementKind elementKind, ElementType... elementTypes) {
        int length = length(elementTypes);
        if (length < 1) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (matches(elementKind, elementTypes[i])) {
                return true;
            }
        }
        return false;
    }
}
