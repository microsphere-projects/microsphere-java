/**
 *
 */
package io.microsphere.util.jar;

import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.ClassLoaderUtils;
import io.microsphere.util.ClassPathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.net.URLUtils.ofURL;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static io.microsphere.util.jar.JarUtils.assertJarURLProtocol;
import static io.microsphere.util.jar.JarUtils.findJarEntry;
import static io.microsphere.util.jar.JarUtils.resolveJarAbsolutePath;
import static io.microsphere.util.jar.JarUtils.resolveRelativePath;
import static io.microsphere.util.jar.JarUtils.toJarFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link JarUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see JarUtilsTest
 * @since 1.0.0
 */
public class JarUtilsTest {

    private final static File tempDirectory = new File(JAVA_IO_TMPDIR);
    private final static File targetDirectory = new File(tempDirectory, "jar-util-extract");
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @BeforeEach
    public void init() throws IOException {
        deleteDirectory(targetDirectory);
        targetDirectory.mkdirs();
    }

    @Test
    public void testAssertJarURLProtocol() {
        URL url = ofURL("http://localhost");
        assertThrows(IllegalArgumentException.class, () -> assertJarURLProtocol(url));
    }

    @Test
    public void testResolveRelativePath() {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, Nonnull.class);
        String relativePath = resolveRelativePath(resourceURL);
        String expectedPath = "javax/annotation/Nonnull.class";
        assertEquals(expectedPath, relativePath);
    }

    @Test
    public void testResolveJarAbsolutePath() throws Exception {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, Nonnull.class);
        String jarAbsolutePath = resolveJarAbsolutePath(resourceURL);
        assertNotNull(jarAbsolutePath);
    }

    @Test
    public void testToJarFile() throws Exception {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, Nonnull.class);
        JarFile jarFile = toJarFile(resourceURL);
        assertNotNull(jarFile);
    }

    public void testToJarFileOnException() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            URL url = new URL("http://www.google.com");
            JarFile jarFile = toJarFile(url);
        });
    }

    @Test
    public void testFindJarEntry() throws Exception {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, Nonnull.class);
        JarEntry jarEntry = findJarEntry(resourceURL);
        assertNotNull(jarEntry);
    }

    @Test
    public void testExtract() throws IOException {
        Set<String> classPaths = ClassPathUtils.getBootstrapClassPaths();
        for (String classPath : classPaths) {
            File jarFile = new File(classPath);
            if (jarFile.exists()) {
                JarUtils.extract(jarFile, targetDirectory);
                break;
            }
        }
    }

    @Test
    public void testExtractWithURL() throws IOException {
        URL resourceURL = ClassLoaderUtils.getResource(classLoader, ClassLoaderUtils.ResourceType.PACKAGE, "javax.annotation");
        JarUtils.extract(resourceURL, targetDirectory, new JarEntryFilter() {
            @Override
            public boolean accept(JarEntry filteredObject) {
                return !filteredObject.isDirectory();
            }
        });
    }
}
