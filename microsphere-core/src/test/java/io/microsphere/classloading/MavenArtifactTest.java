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
package io.microsphere.classloading;

import org.junit.jupiter.api.Test;

import static io.microsphere.classloading.MavenArtifact.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * {@link MavenArtifact} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MavenArtifact
 * @since 1.0.0
 */
public class MavenArtifactTest {

    @Test
    public void test() {
        MavenArtifact artifact = create("io.github.microsphere-projects", "microsphere-core", "1.0.0");

        assertTrue(create("io.github.microsphere-projects", "microsphere-core", "1.0.0").equals(artifact));

        assertEquals("io.github.microsphere-projects", artifact.getGroupId());
        assertEquals("microsphere-core", artifact.getArtifactId());
        assertEquals("1.0.0", artifact.getVersion());
        assertNull(artifact.getLocation());

        assertTrue(create("*", "*", "*").matches(artifact));
        assertTrue(create("io.github.microsphere-projects", "*", "*").matches(artifact));
        assertTrue(create("io.github.microsphere-projects", "microsphere-core", "*").matches(artifact));
        assertTrue(create("io.github.microsphere-projects", "microsphere-core", "1.0.0").matches(artifact));

        assertFalse(create("-", "microsphere-core", "1.0.0").matches(artifact));
        assertFalse(create("io.github.microsphere-projects", "-", "1.0.0").matches(artifact));
        assertFalse(create("io.github.microsphere-projects", "microsphere-core", "-").matches(artifact));

    }
}
