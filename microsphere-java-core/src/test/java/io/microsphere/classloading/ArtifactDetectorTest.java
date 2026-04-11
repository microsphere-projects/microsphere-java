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


import io.microsphere.LoggingTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.List;

import static io.microsphere.AbstractTestCase.TEST_NULL_SET;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.net.URLUtils.ofURL;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ArtifactDetector} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ArtifactDetector
 * @since 1.0.0
 */
class ArtifactDetectorTest extends LoggingTest {

    @Test
    void testDetect() {
        ArtifactDetector instance = new ArtifactDetector();
        List<Artifact> artifacts = instance.detect();
        assertNotNull(artifacts);
    }

    @Test
    void testDetectOnClassInResource() {
        ArtifactDetector instance = new ArtifactDetector();
        assertThrows(NullPointerException.class, () -> instance.detect((Class) null));

        Artifact artifact = instance.detect(ArtifactDetector.class);
        assertNull(artifact);

        artifact = instance.detect(Nonnull.class);
        assertNotNull(artifact);
        assertEquals("jsr305", artifact.getArtifactId());
        assertEquals("3.0.2", artifact.getVersion());
    }

    @Test
    void testDetectOnResourceURL() {
        ArtifactDetector instance = new ArtifactDetector();
        assertNull(instance.detect((URL) null));
        URL url = ofURL("file:///not-found");
        Artifact artifact = instance.detect(url);
        assertNull(artifact);
    }

    @Test
    void testDetectOnNullSet() {
        ArtifactDetector instance = new ArtifactDetector(getDefaultClassLoader());
        assertTrue(instance.detect(TEST_NULL_SET).isEmpty());
    }

    @Test
    void testDetectOnEmptySet() {
        ArtifactDetector instance = new ArtifactDetector(null);
        assertTrue(instance.detect(emptySet()).isEmpty());
        assertTrue(instance.detect(ofSet(ofURL("file:///not-found"))).isEmpty());
    }

    @Test
    void testDetectWithIncludedJdkLibraries() {
        ArtifactDetector instance = new ArtifactDetector();
        List<Artifact> artifacts = instance.detect(true);
        assertNotNull(artifacts);
    }

    @Test
    void testDetectWithExcludedJdkLibraries() {
        ArtifactDetector instance = new ArtifactDetector();
        List<Artifact> artifacts = instance.detect(false);
        assertNotNull(artifacts);
    }
}
