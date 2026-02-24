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

package io.microsphere.management.builder;


import org.junit.jupiter.api.Test;

import javax.management.MBeanNotificationInfo;

import static io.microsphere.management.builder.MBeanNotificationInfoBuilder.getClassNames;
import static io.microsphere.management.builder.MBeanNotificationInfoBuilder.notification;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MBeanNotificationInfoBuilder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MBeanNotificationInfoBuilder
 * @since 1.0.0
 */
class MBeanNotificationInfoBuilderTest extends AbstractMBeanFeatureInfoBuilderTest<MBeanNotificationInfoBuilder> {

    @Test
    void testBuild() {
        this.builder.name(TEST_NAME);
        this.builder.description(TEST_DESCRIPTION);
        this.builder.descriptor(TEST_DESCRIPTOR);

        MBeanNotificationInfo info = this.builder.build();

        assertEquals(TEST_NAME, info.getName());
        assertEquals(TEST_DESCRIPTION, info.getDescription());
        assertEquals(TEST_DESCRIPTOR, info.getDescriptor());
        assertTrue(isEmpty(info.getNotifTypes()));
    }

    @Test
    void testNotification() {
        Class<?>[] types = ofArray(String.class, Integer.class, long.class);
        MBeanNotificationInfoBuilder builder = notification(types);
        MBeanNotificationInfo info = builder.build();
        assertNull(info.getName());
        assertNull(info.getDescription());
        assertNotNull(info.getDescriptor());

        assertArrayEquals(getClassNames(types), info.getNotifTypes());

        builder = notification(EMPTY_CLASS_ARRAY);
        info = builder.build();
        assertNull(info.getName());
        assertNull(info.getDescription());
        assertNotNull(info.getDescriptor());
        assertTrue(isEmpty(info.getNotifTypes()));
    }
}