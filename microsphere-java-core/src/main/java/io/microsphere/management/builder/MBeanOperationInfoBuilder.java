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
import javax.management.MBeanOperationInfo;
import java.lang.reflect.Method;

import static io.microsphere.management.builder.MBeanOperationInfoBuilder.Impact.UNKNOWN;
import static io.microsphere.util.ClassUtils.getTypeName;

/**
 * {@link MBeanOperationInfo} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanOperationInfo
 * @see MBeanFeatureInfo
 * @see MBeanFeatureInfoBuilder
 * @since 1.0.0
 */
public class MBeanOperationInfoBuilder extends MBeanExecutableInfoBuilder<MBeanOperationInfoBuilder> {

    /**
     * The method's return value.
     */
    @Nonnull
    private String type;

    /**
     * The impact of the method, one of {@link MBeanOperationInfo#INFO}, {@link MBeanOperationInfo#ACTION},
     * {@link MBeanOperationInfo#ACTION_INFO} and {@link MBeanOperationInfo#UNKNOWN}.
     */
    private int impact;

    MBeanOperationInfoBuilder() {
        super();
        impact(UNKNOWN);
    }

    public MBeanOperationInfoBuilder impact(Impact impact) {
        this.impact = impact.getValue();
        return this;
    }

    public MBeanOperationInfo build() {
        return new MBeanOperationInfo(this.name, this.description, toSignature(), this.type, this.impact, this.descriptor);
    }

    public static MBeanOperationInfoBuilder operation(Class<?> type) {
        return operation(getTypeName(type));
    }

    public static MBeanOperationInfoBuilder operation(String type) {
        MBeanOperationInfoBuilder builder = new MBeanOperationInfoBuilder();
        builder.type = type;
        return builder;
    }

    public static MBeanOperationInfoBuilder operation(Method method) {
        Class<?> returnType = method.getReturnType();
        return operation(returnType).from(method);
    }

    public enum Impact {

        /**
         * Indicates that the operation is read-like:
         * it returns information but does not change any state.
         */
        INFO(MBeanOperationInfo.INFO),

        /**
         * Indicates that the operation is write-like: it has an effect but does
         * not return any information from the MBean.
         */
        ACTION(MBeanOperationInfo.ACTION),

        /**
         * Indicates that the operation is both read-like and write-like:
         * it has an effect, and it also returns information from the MBean.
         */
        ACTION_INFO(MBeanOperationInfo.ACTION_INFO),

        /**
         * Indicates that the impact of the operation is unknown or cannot be
         * expressed using one of the other values.
         */
        UNKNOWN(MBeanOperationInfo.UNKNOWN);

        private final int value;

        Impact(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}