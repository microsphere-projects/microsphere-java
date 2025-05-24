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

import static javax.lang.model.element.ElementKind.CLASS;
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
     * Convert {@link ElementType} to {@link ElementKind}
     *
     * @param elementType {@link ElementType}
     * @return {@link ElementKind}
     */
    static ElementKind toElementKind(ElementType elementType) {
        switch (elementType) {
            case TYPE:
            case TYPE_USE:
                return CLASS;
            default:
                return valueOf(elementType.name());
        }
    }
}
