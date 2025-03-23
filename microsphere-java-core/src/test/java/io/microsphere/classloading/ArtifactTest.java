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

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static io.microsphere.classloading.Artifact.UNKNOWN;
import static io.microsphere.classloading.Artifact.create;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Artifact} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Artifact
 * @since 1.0.0
 */
public class ArtifactTest extends AbstractTestCase {

    private static final String ARTIFACT_ID = "microsphere-core";

    private static final String VERSION = "1.0.0";

    private static final URL LOCATION = getClassResource(TEST_CLASS_LOADER, ArtifactTest.class);

    private Artifact artifact;

    @BeforeEach
    public void init() {
        this.artifact = create(ARTIFACT_ID, VERSION, LOCATION);
    }

    @Test
    public void testCreateOnArtifactId() {
        Artifact artifact = create(ARTIFACT_ID);
        assertEquals(ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(UNKNOWN, artifact.getVersion());
        assertNull(artifact.getLocation());
    }

    @Test
    public void testCreateOnArtifactIdAndVersion() {
        Artifact artifact = create(ARTIFACT_ID, VERSION);
        assertEquals(ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(VERSION, artifact.getVersion());
        assertNull(artifact.getLocation());
    }

    @Test
    public void testCreateOnArtifactIdAndVersionAndLocation() {
        Artifact artifact = create(ARTIFACT_ID, VERSION, LOCATION);
        assertEquals(ARTIFACT_ID, artifact.getArtifactId());
        assertEquals(VERSION, artifact.getVersion());
        assertEquals(LOCATION, artifact.getLocation());
    }

    @Test
    public void testEquals() {
        assertTrue(artifact.equals(create(ARTIFACT_ID, VERSION, LOCATION)));

        assertFalse(artifact.equals(null));
        assertFalse(artifact.equals(create(ARTIFACT_ID)));
        assertFalse(artifact.equals(create(ARTIFACT_ID, VERSION)));
        assertFalse(artifact.equals(create(ARTIFACT_ID, "-")));
        assertFalse(artifact.equals(create("-")));
        assertFalse(artifact.equals(create("-", "-")));
    }

    @Test
    public void testHashCode() {
        assertEquals(artifact.hashCode(), create(ARTIFACT_ID, VERSION, LOCATION).hashCode());
    }

    @Test
    public void testToString() {
        assertNotNull(artifact.toString());
    }

    @Test
    public void testMatches() {
        assertTrue(create("*", "*").matches(artifact));
        assertTrue(create("*", "*").matches(artifact));
        assertTrue(create(ARTIFACT_ID, "*").matches(artifact));
        assertTrue(create(ARTIFACT_ID, VERSION).matches(artifact));
        assertFalse(create(VERSION).matches(artifact));
        assertFalse(create(ARTIFACT_ID, "-").matches(artifact));
    }
}
