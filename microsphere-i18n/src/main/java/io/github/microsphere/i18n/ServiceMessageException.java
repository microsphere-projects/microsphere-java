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
package io.github.microsphere.i18n;

import io.github.microsphere.i18n.util.MessageUtils;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Service Message Exception
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ServiceMessageException extends RuntimeException {

    private final String message;
    private final Object[] args;

    public ServiceMessageException(String message, Object... args) {
        this.message = message;
        this.args = args;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return MessageUtils.getLocalizedMessage(message, args);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ServiceMessageException.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("args=" + Arrays.toString(args))
                .add("localized message =" + getLocalizedMessage())
                .toString();
    }
}
