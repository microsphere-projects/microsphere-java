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
package io.github.microsphere.logging.log4j2.filter;

import io.github.microsphere.logging.filter.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.AbstractFilter;

import static org.apache.logging.log4j.core.util.Watcher.ELEMENT_TYPE;

/**
 * {@link Filter} Adapter for Log4j2
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Plugin(name = "Log4j2FilterAdapter", category = Node.CATEGORY, elementType = ELEMENT_TYPE, printObject = true)
public class Log4j2FilterAdapter extends AbstractFilter {

    private Filter delegate;

    @Override
    public Result filter(LogEvent event) {
        String loggerName = event.getLoggerName();
        String level = event.getLevel().name();
        String message = event.getMessage().getFormattedMessage();
        // delegate maybe some Filter or CompositeFilter
        Filter.Result result = delegate.filter(loggerName, level, message);
        switch (result) {
            case ACCEPT:
                return Result.ACCEPT;
            case DENY:
                return Result.DENY;
            default:
                return Result.NEUTRAL;
        }
    }

    public void setDelegate(Filter delegate) {
        this.delegate = delegate;
    }

}
