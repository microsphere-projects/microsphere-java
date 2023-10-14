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
package io.github.microsphere.jdbc.util;

import java.sql.Statement;

/**
 * The enumerations for Data Definition {@link Statement} type
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public enum DataDefinitionStatementType {

    ALTER_DATABASE,

    ALTER_EVENT,

    ALTER_FUNCTION,

    ALTER_INSTANCE,

    ALTER_LOGFILE_GROUP,

    ALTER_PROCEDURE,

    ALTER_SERVER,

    ALTER_TABLE,

    ALTER_TABLESPACE,

    ALTER_VIEW,

    CREATE_DATABASE,

    CREATE_EVENT,

    CREATE_FUNCTION,

    CREATE_INDEX,

    CREATE_LOGFILE_GROUP,

    CREATE_PROCEDURE,

    CREATE_SERVER,

    CREATE_SPATIAL_REFERENCE_SYSTEM,

    CREATE_TABLE,

    CREATE_TABLESPACE,

    CREATE_TRIGGER,

    CREATE_VIEW,

    DROP_DATABASE,

    DROP_EVENT,

    DROP_FUNCTION,

    DROP_INDEX,

    DROP_LOGFILE_GROUP,

    DROP_PROCEDURE,

    DROP_SERVER,

    DROP_SPATIAL_REFERENCE_SYSTEM,

    DROP_TABLE,

    DROP_TABLESPACE,

    DROP_TRIGGER,

    DROP_VIEW,

    RENAME_TABLE,

    TRUNCATE_TABLE


}
