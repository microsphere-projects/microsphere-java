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
package io.microsphere.logging.filter;

/**
 * Abstract {@link Filter} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Filter
 * @since 1.0.0
 */
public abstract class AbstractFilter implements Filter {

    private Filter.Result onMatch = Filter.Result.NEUTRAL;

    private Filter.Result onMismatch = Filter.Result.NEUTRAL;

    @Override
    public final Result filter(String loggerName, String level, String message) {
        if (matches(loggerName, level, message)) {
            if (onMatch != null) {
                return onMatch;
            }
        }
        return onMismatch;
    }

    /**
     * Matches
     *
     * @param loggerName the logging name
     * @param level      the logging level
     * @param message    logging message or logging message pattern
     * @return <code>true</code> if matches, or <code>false</code>
     */
    protected abstract boolean matches(String loggerName, String level, String message);

    public void setOnMatch(Filter.Result onMatch) {
        this.onMatch = onMatch;
    }

    public void setOnMismatch(Filter.Result onMismatch) {
        this.onMismatch = onMismatch;
    }

    public Result getOnMatch() {
        return onMatch;
    }

    public Result getOnMismatch() {
        return onMismatch;
    }
}
