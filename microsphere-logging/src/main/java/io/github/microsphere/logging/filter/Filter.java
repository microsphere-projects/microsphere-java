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
package io.github.microsphere.logging.filter;

/**
 * Logging Filter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface Filter {

    /**
     * Filter to {@link Result}
     *
     * @param loggerName the logging name
     * @param level      the logging level
     * @param message    logging message or logging message pattern
     * @return {@link Result#ACCEPT} or {@link Result#NEUTRAL} or {@link Result#DENY}
     */
    Result filter(String loggerName, String level, String message);

    /**
     * The result of {@link Filter}
     */
    enum Result {

        ACCEPT,
        NEUTRAL,
        DENY;

        /**
         * Covert the specified {@link Enum enumeration} to {@link Result}
         *
         * @param enumeration {@link Enum enumeration}
         * @return the member of {@link Result}
         * @throws IllegalArgumentException if the specified enum type has
         *                                  no constant with the specified name, or the specified
         *                                  class object does not represent an enum type
         * @throws NullPointerException     if {@code enumType} or {@code name}
         *                                  is null
         */
        public static Result of(Enum enumeration) {
            return valueOf(enumeration.name());
        }

        /**
         * Covert the specified name to {@link Result}
         *
         * @param name the {@link String} representing the name of {@link Enum enumeration}
         * @return the member of {@link Result}
         * @throws IllegalArgumentException if the specified enum type has
         *                                  no constant with the specified name, or the specified
         *                                  class object does not represent an enum type
         * @throws NullPointerException     if {@code enumType} or {@code name}
         *                                  is null
         */
        public static Result of(String name) {
            return valueOf(name);
        }
    }
}
