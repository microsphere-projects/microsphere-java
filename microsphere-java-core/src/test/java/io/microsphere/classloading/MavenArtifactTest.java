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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static io.microsphere.classloading.Artifact.UNKNOWN;
import static io.microsphere.classloading.Artifact.WILDCARD;
import static io.microsphere.classloading.MavenArtifact.create;
import static io.microsphere.constants.SymbolConstants.HYPHEN;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * {@link MavenArtifact} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MavenArtifact
 * @since 1.0.0
 */
class MavenArtifactTest {

    private static final String GROUP_ID = "io.github.microsphere-projects";

    private static final String ARTIFACT_ID = "microsphere-core";

    private static final String VERSION = "1.0.0";

    private static final URL LOCATION = getClassResource(MavenArtifactTest.class);

    private MavenArtifact artifact;

    @BeforeEach
    void init() {
        this.artifact = create(GROUP_ID, ARTIFACT_ID, VERSION, LOCATION);
    }

    @Test
    void testCreateOnGroupIdAndArtifactId() {
        MavenArtifact artifact = create(GROUP_ID, ARTIFACT_ID);
        assertEquals(GROUP_ID, artifact.getGroupId());
        assertEquals(ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(UNKNOWN, artifact.getVersion());
        assertNull(artifact.getLocation());
    }

    @Test
    void testCreateOnGroupIdAndArtifactIdAndVersion() {
        MavenArtifact artifact = create(GROUP_ID, ARTIFACT_ID, VERSION);
        assertEquals(GROUP_ID, artifact.getGroupId());
        assertEquals(ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(VERSION, artifact.getVersion());
        assertNull(artifact.getLocation());
    }

    @Test
    void testCreateOnGroupIdAndArtifactIdAndVersionAndLocation() {
        MavenArtifact artifact = create(GROUP_ID, ARTIFACT_ID, VERSION, LOCATION);
        assertEquals(GROUP_ID, artifact.getGroupId());
        assertEquals(ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(VERSION, artifact.getVersion());
        assertEquals(LOCATION, artifact.getLocation());
    }

    @Test
    void testEquals() {
        assertTrue(artifact.equals(create(GROUP_ID, ARTIFACT_ID, VERSION, LOCATION)));

        assertFalse(artifact.equals(null));
        assertFalse(artifact.equals(create(GROUP_ID)));
        assertFalse(artifact.equals(create(GROUP_ID, ARTIFACT_ID)));
        assertFalse(artifact.equals(create(GROUP_ID, ARTIFACT_ID, VERSION)));
        assertFalse(artifact.equals(create(GROUP_ID, ARTIFACT_ID, HYPHEN)));
        assertFalse(artifact.equals(create(GROUP_ID, HYPHEN, HYPHEN)));
        assertFalse(artifact.equals(create(HYPHEN, HYPHEN, HYPHEN)));
    }

    @Test
    void testHashCode() {
        assertEquals(artifact.hashCode(), create(GROUP_ID, ARTIFACT_ID, VERSION, LOCATION).hashCode());
    }

    @Test
    void testToString() {
        assertNotNull(artifact.toString());
    }

    @Test
    void testMatches() {
        assertTrue(create(WILDCARD, WILDCARD, WILDCARD).matches(artifact));
        assertTrue(create(GROUP_ID, WILDCARD, WILDCARD).matches(artifact));
        assertTrue(create(GROUP_ID, ARTIFACT_ID, WILDCARD).matches(artifact));
        assertTrue(create(GROUP_ID, ARTIFACT_ID, VERSION).matches(artifact));
        assertFalse(create(HYPHEN, ARTIFACT_ID, VERSION).matches(artifact));
        assertFalse(create(GROUP_ID, HYPHEN, VERSION).matches(artifact));
        assertFalse(create(GROUP_ID, ARTIFACT_ID, HYPHEN).matches(artifact));
    }
}
