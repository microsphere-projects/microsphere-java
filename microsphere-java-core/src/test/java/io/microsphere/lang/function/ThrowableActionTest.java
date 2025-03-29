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
package io.microsphere.lang.function;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import static io.microsphere.lang.function.ThrowableAction.execute;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ThrowableAction} Test
 *
 * @since 1.0.0
 */
public class ThrowableActionTest extends AbstractTestCase {

    private final ThrowableAction action = () -> {
        logger.trace("ThrowableAction#execute()");
    };

    private final ThrowableAction exceptionalAction = () -> {
        throw new Exception("Test");
    };

    @Test
    public void testExecute() throws Throwable {
        action.execute();
        assertThrows(Exception.class, exceptionalAction::execute);
    }

    @Test
    public void testExecuteWithThrowableAction() {
        execute(action);
        assertThrows(RuntimeException.class, () -> execute(exceptionalAction));
    }

    @Test
    public void testExecuteWithThrowableActionOnNull() {
        assertThrows(NullPointerException.class, () -> execute(null));
    }

    @Test
    public void testExecuteWithThrowableActionAndConsumer() {
        execute(action, e -> {
        });

        assertThrows(RuntimeException.class, () -> execute(exceptionalAction, e -> {
            throw new RuntimeException(e);
        }));
    }

    @Test
    public void testExecuteWithThrowableActionAndConsumerOnNull() {
        assertThrows(IllegalArgumentException.class, () -> execute(null, e -> {
        }));
    }
}
