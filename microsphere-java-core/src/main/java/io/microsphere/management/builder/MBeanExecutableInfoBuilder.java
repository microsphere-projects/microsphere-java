
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

import io.microsphere.management.JmxUtils;

import javax.management.Descriptor;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.lang.reflect.Executable;
import java.util.List;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.management.JmxUtils.descriptorForElement;
import static io.microsphere.management.builder.MBeanParameterInfoBuilder.parameter;
import static java.util.Collections.addAll;

/**
 * MBean {@link Executable} Info Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanConstructorInfo
 * @see MBeanOperationInfo
 * @see MBeanFeatureInfo
 * @see Executable
 * @see MBeanFeatureInfoBuilder
 * @since 1.0.0
 */
public abstract class MBeanExecutableInfoBuilder<B extends MBeanExecutableInfoBuilder> extends MBeanFeatureInfoBuilder<B> {

    protected static final MBeanParameterInfo[] NO_PARAMS = new MBeanParameterInfo[0];

    /**
     * The signature of the method, that is, the class names
     * of the arguments.
     */
    private final List<MBeanParameterInfo> parameters = newLinkedList();

    MBeanExecutableInfoBuilder() {
        super();
    }

    public B signature(MBeanParameterInfo... signature) {
        this.parameters.clear();
        addAll(this.parameters, signature);
        return (B) this;
    }

    public B param(Class<?> type, Consumer<MBeanParameterInfoBuilder> parameterBuilderConsumer) {
        MBeanParameterInfoBuilder builder = parameter(type);
        parameterBuilderConsumer.accept(builder);
        MBeanParameterInfo build = builder.build();
        this.parameters.add(build);
        return (B) this;
    }

    public B from(Executable executable) {
        String name = executable.getName();
        Descriptor descriptor = descriptorForElement(executable);
        MBeanParameterInfo[] signature = JmxUtils.signature(executable.getParameters());
        return (B) name(name)
                .signature(signature)
                .descriptor(descriptor)
                .description(executable.toString());
    }

    protected MBeanParameterInfo[] toSignature() {
        return this.parameters.toArray(NO_PARAMS);
    }
}