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

package io.microsphere.management.builder;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.ClassUtils;

import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.util.Objects;

import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.util.stream.Stream.of;

/**
 * {@link MBeanNotificationInfo} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanFeatureInfoBuilder
 * @see MBeanNotificationInfo
 * @see MBeanFeatureInfo
 * @since 1.0.0
 */
public class MBeanNotificationInfoBuilder extends MBeanFeatureInfoBuilder<MBeanNotificationInfoBuilder> {

    @Nullable
    String[] types;

    MBeanNotificationInfoBuilder() {
        super();
    }

    public MBeanNotificationInfoBuilder types(@Nullable Class<?>... types) {
        return types(getClassNames(types));
    }

    public MBeanNotificationInfoBuilder types(@Nullable String... types) {
        this.types = types;
        return this;
    }

    public MBeanNotificationInfo build() {
        return new MBeanNotificationInfo(this.types, this.name, this.description, this.descriptor);
    }

    public static MBeanNotificationInfoBuilder notification() {
        return new MBeanNotificationInfoBuilder();
    }

    public static MBeanNotificationInfoBuilder notification(@Nullable Class<?>... types) {
        return notification().types(types);
    }

    public static MBeanNotificationInfoBuilder notification(@Nullable String... types) {
        return notification().types(types);
    }

    static String[] getClassNames(Class<?>[] types) {
        if (isEmpty(types)) {
            return EMPTY_STRING_ARRAY;
        }
        return of(types)
                .filter(Objects::nonNull)
                .map(ClassUtils::getTypeName)
                .toArray(String[]::new);
    }
}
