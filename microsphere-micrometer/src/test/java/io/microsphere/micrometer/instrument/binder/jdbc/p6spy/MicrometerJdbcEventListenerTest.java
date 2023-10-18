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
package io.microsphere.micrometer.instrument.binder.jdbc.p6spy;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@link MicrometerJdbcEventListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MicrometerJdbcEventListener
 * @since 1.0.0
 */
public class MicrometerJdbcEventListenerTest {

    private MicrometerJdbcEventListener listener;

    @Before
    public void init() {
        MeterRegistry registry = new SimpleMeterRegistry();
        listener = new MicrometerJdbcEventListener(registry);
    }

    @Test
    public void testResolveStatementType() {
        String type = listener.resolveStatementType("SELECT * FROM users;");
        assertEquals("SELECT", type);

        type = listener.resolveStatementType("INSERT INTO users(id) VALUES (1);");
        assertEquals("INSERT", type);

        type = listener.resolveStatementType("UPDATE users SET ...");
        assertEquals("UPDATE", type);

        type = listener.resolveStatementType("DELETE FROM users");
        assertEquals("DELETE", type);
    }

    @Test
    public void testAddMetrics() {
        String sql = "SELECT * FROM users;";
        listener.addMetrics(sql, 1000000000, 0, null);
    }
}
