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

import io.microsphere.annotation.Nonnull;

import javax.management.MBeanFeatureInfo;
import javax.management.MBeanParameterInfo;

import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * {@link MBeanParameterInfo} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanParameterInfo
 * @see MBeanFeatureInfo
 * @see MBeanFeatureInfoBuilder
 * @since 1.0.0
 */
public class MBeanParameterInfoBuilder extends MBeanFeatureInfoBuilder<MBeanParameterInfoBuilder> {

    /**
     * The type or class name of the data.
     */
    @Nonnull
    private String type;

    MBeanParameterInfoBuilder() {
        super();
    }

    public MBeanParameterInfo build() {
        return new MBeanParameterInfo(this.name, this.type, this.description, this.descriptor);
    }

    public static MBeanParameterInfoBuilder parameter(@Nonnull Class<?> type) {
        return parameter(getTypeName(type));
    }

    public static MBeanParameterInfoBuilder parameter(@Nonnull String type) {
        MBeanParameterInfoBuilder builder = new MBeanParameterInfoBuilder();
        builder.type = type;
        return builder;
    }
}
