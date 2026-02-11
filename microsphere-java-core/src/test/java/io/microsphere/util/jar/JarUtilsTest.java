/**
 *
 */
package io.microsphere.util.jar;

import io.microsphere.AbstractTestCase;
import io.microsphere.filter.JarEntryFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.net.URLUtils.ofURL;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.PACKAGE;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.jar.JarUtils.MANIFEST_RESOURCE_PATH;
import static io.microsphere.util.jar.JarUtils.assertJarURLProtocol;
import static io.microsphere.util.jar.JarUtils.doExtract;
import static io.microsphere.util.jar.JarUtils.extract;
import static io.microsphere.util.jar.JarUtils.filter;
import static io.microsphere.util.jar.JarUtils.findJarEntry;
import static io.microsphere.util.jar.JarUtils.resolveJarAbsolutePath;
import static io.microsphere.util.jar.JarUtils.resolveRelativePath;
import static io.microsphere.util.jar.JarUtils.toJarFile;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link JarUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see JarUtilsTest
 * @since 1.0.0
 */
class JarUtilsTest extends AbstractTestCase {

    private File targetDirectory;

    private final ClassLoader classLoader = getClassLoader(getClass());

    private URL resourceURL;

    @BeforeEach
    void setUp() {
        this.targetDirectory = createRandomTempDirectory();
        this.resourceURL = getClassResource(this.classLoader, Nonnull.class);
    }

    @Test
    void testConstants() {
        assertSame("META-INF/MANIFEST.MF", MANIFEST_RESOURCE_PATH);
    }

    @Test
    void testAssertJarURLProtocol() {
        assertThrows(IllegalArgumentException.class, () -> assertJarURLProtocol(ofURL("http://localhost")));
        assertJarURLProtocol(ofURL("file://localhost"));
    }

    @Test
    void testResolveRelativePath() {
        String relativePath = resolveRelativePath(this.resourceURL);
        String expectedPath = "javax/annotation/Nonnull.class";
        assertEquals(expectedPath, relativePath);
    }

    @Test
    void testResolveJarAbsolutePath() {
        String jarAbsolutePath = resolveJarAbsolutePath(this.resourceURL);
        assertNotNull(jarAbsolutePath);
    }

    @Test
    void testFilter() throws IOException {
        JarFile jarFile = toJarFile(this.resourceURL);
        List<JarEntry> jarEntries = filter(jarFile, null);
        assertFalse(jarEntries.isEmpty());
    }

    @Test
    void testFilterOnNullJarFile() {
        assertSame(emptyList(), filter(null, null));
    }

    @Test
    void testToJarFile() throws IOException {
        JarFile jarFile = toJarFile(this.resourceURL);
        assertNotNull(jarFile);
    }

    @Test
    void testToJarFileOnNotFound() throws IOException {
        URL url = new URL("jar:file:/path/to/file.jar!/entry");
        JarFile jarFile = toJarFile(url);
        assertNull(jarFile);
    }

    @Test
    void testToJarFileOnInvalidProtocol() {
        assertThrows(IllegalArgumentException.class, () -> toJarFile(new URL("http://github.com")));
    }

    @Test
    void testToJarFileOnNPE() {
        assertThrows(NullPointerException.class, () -> toJarFile(null));
    }

    @Test
    void testFindJarEntry() throws Exception {
        URL resourceURL = getClassResource(this.classLoader, Nonnull.class);
        JarEntry jarEntry = findJarEntry(resourceURL);
        assertNotNull(jarEntry);
    }

    @Test
    void testExtract() throws IOException {
        String jarAbsolutePath = resolveJarAbsolutePath(this.resourceURL);
        extract(new File(jarAbsolutePath), this.targetDirectory);
    }

    @Test
    void testExtractWithURL() throws IOException {
        extract(this.resourceURL, this.targetDirectory, (JarEntryFilter) filteredObject -> !filteredObject.isDirectory());

        URL resourceURL = getResource(this.classLoader, PACKAGE, "javax.annotation");
        extract(resourceURL, this.targetDirectory, (JarEntryFilter) filteredObject -> !filteredObject.isDirectory());
    }

    @Test
    void testDoExtractOnNullJarFile() throws IOException {
        doExtract(null, null, this.targetDirectory);
    }

    @Test
    void testDoExtractWithoutJarEntry() throws IOException {
        JarFile jarFile = toJarFile(this.resourceURL);
        doExtract(jarFile, null, this.targetDirectory);
    }

    @Test
    void testDoExtractOnMissingMatch() throws IOException {
        URL resourceURL = getClassResource(this.classLoader, Test.class);
        JarFile jarFile = toJarFile(resourceURL);
        List<JarEntry> jarEntries = filter(jarFile, null);
        doExtract(toJarFile(this.resourceURL), jarEntries, this.targetDirectory);
    }
}
