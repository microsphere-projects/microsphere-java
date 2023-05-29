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

import java.util.List;

/**
 * Composite {@link LoggingNameFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CompositeFilter implements Filter {

    private List<Filter> filters;

    private Operator operator = Operator.OR; // Default "OR"


    @Override
    public Result filter(String loggerName, String level, String message) {
        // TODO
        Result result = Result.NEUTRAL;
        for (Filter filter : filters) {
            if (Operator.AND.equals(operator)) {
                Result innerResult = filter.filter(loggerName, level, message);
                if (Result.ACCEPT.equals(innerResult)) {
                    result = Result.ACCEPT;
                }
            }
        }
        return result;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    enum Operator {

        AND,
        OR,
        XOR,

    }
}
