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

import static io.microsphere.classloading.Artifact.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Artifact} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Artifact
 * @since 1.0.0
 */
public class ArtifactTest {

    @Test
    public void test() {
        Artifact artifact = create("microsphere-core", "1.0.0");

        assertEquals(create("microsphere-core", "1.0.0"), artifact);

        assertEquals("microsphere-core", artifact.getArtifactId());
        assertEquals("1.0.0", artifact.getVersion());
        assertNull(artifact.getLocation());

        assertTrue(create("*", "*").matches(artifact));
        assertTrue(create("microsphere-core", "*").matches(artifact));
        assertTrue(create("*", "1.0.0").matches(artifact));

        assertFalse(create("-", "-").matches(artifact));
        assertFalse(create("-", "*").matches(artifact));
        assertFalse(create("microsphere-core", "-").matches(artifact));
    }
}
