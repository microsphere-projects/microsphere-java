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
package io.microsphere.util;

import io.microsphere.annotation.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

/**
 * Compatible
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Compatible<T, R> {

    private final Version version;

    private final Function<Version, R> conditionalFunction;

    public Compatible(Version version, Function<Version, R> conditionalFunction) {
        this.version = version;
        this.conditionalFunction = conditionalFunction;
    }

    public static <T> Compatible<T, ?> of(Class<T> targetClass) {
        return new Compatible<>(Version.getVersion(targetClass), null);
    }

    public <R> Compatible<T, R> on(String operator, String comparedVersion,
                                   Function<Version, R> conditionalFunction) {
        return on(Version.Operator.of(operator), Version.of(comparedVersion), conditionalFunction);
    }

    public <R> Compatible<T, R> on(Version.Operator operator, Version comparedVersion,
                                   Function<Version, R> conditionalFunction) {
        if (TRUE.equals(operator.test(this.version, comparedVersion))) {
            return new Compatible<>(version, conditionalFunction);
        }
        return (Compatible<T, R>) this;
    }

    /**
     * @return
     */
    public Optional<R> call() {
        R result = null;
        if (conditionalFunction != null) {
            result = conditionalFunction.apply(version);
        }
        return ofNullable(result);
    }

    public void accept(Consumer<R> resultConsumer) {
        call().ifPresent(resultConsumer);
    }

    /**
     * @return
     */
    @Nullable
    public R get() {
        return call().orElse(null);
    }
}
