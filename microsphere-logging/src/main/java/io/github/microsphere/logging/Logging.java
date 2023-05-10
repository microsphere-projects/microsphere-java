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
package io.github.microsphere.logging;

import java.util.List;
import java.util.logging.LoggingMXBean;

/**
 * The management interface for the logging facility.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingMXBean
 * @since 1.0.0
 */
interface Logging {

    /**
     * Returns the list of currently registered logger names.
     *
     * @return A list of <tt>String</tt> each of which is a
     * currently registered <tt>Logger</tt> name.
     */
    List<String> getLoggerNames();

    /**
     * Gets the name of the log level associated with the specified logger.
     * If the specified logger does not exist, <tt>null</tt>
     * is returned.
     *
     * <p>
     * If the <tt>Level</tt> of the specified logger is <tt>null</tt>,
     * which means that this logger's effective level is inherited
     * from its parent, an empty string will be returned.
     *
     * @param loggerName The name of the <tt>Logger</tt> to be retrieved.
     * @return The name of the log level of the specified logger; or
     * an empty string if the log level of the specified logger
     * is <tt>null</tt>.  If the specified logger does not
     * exist, <tt>null</tt> is returned.
     */
    String getLoggerLevel(String loggerName);

    /**
     * Sets the specified logger to the specified new level.
     * If the <tt>levelName</tt> is not <tt>null</tt>, the level
     * of the specified logger is set to the parsed <tt>Level</tt>
     * matching the <tt>levelName</tt>.
     * If the <tt>levelName</tt> is <tt>null</tt>, the level
     * of the specified logger is set to <tt>null</tt> and
     * the effective level of the logger is inherited from
     * its nearest ancestor with a specific (non-null) level value.
     *
     * @param loggerName The name of the <tt>Logger</tt> to be set.
     *                   Must be non-null.
     * @param levelName  The name of the level to set on the specified logger,
     *                   or <tt>null</tt> if setting the level to inherit
     *                   from its nearest ancestor.
     * @throws IllegalArgumentException if the specified logger
     *                                  does not exist, or <tt>levelName</tt> is not a valid level name.
     */
    void setLoggerLevel(String loggerName, String levelName);

    /**
     * Returns the name of the parent for the specified logger.
     * If the specified logger does not exist, <tt>null</tt> is returned.
     * If the specified logger is the root <tt>Logger</tt> in the namespace,
     * the result will be an empty string.
     *
     * @param loggerName The name of a <tt>Logger</tt>.
     * @return the name of the nearest existing parent logger;
     * an empty string if the specified logger is the root logger.
     * If the specified logger does not exist, <tt>null</tt>
     * is returned.
     */
    String getParentLoggerName(String loggerName);
}
