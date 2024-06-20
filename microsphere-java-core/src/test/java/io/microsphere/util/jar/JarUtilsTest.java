/**
 *
 */
package io.microsphere.util.jar;

import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.ClassLoaderUtils;
import io.microsphere.util.ClassPathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.util.SystemUtils.JAVA_HOME;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link JarUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
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
    public void testResolveRelativePath() {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        String relativePath = JarUtils.resolveRelativePath(resourceURL);
        String expectedPath = "java/lang/String.class";
        assertEquals(expectedPath, relativePath);
    }

    @Test
    public void testResolveJarAbsolutePath() throws Exception {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        String jarAbsolutePath = JarUtils.resolveJarAbsolutePath(resourceURL);
        File rtJarFile = new File(JAVA_HOME, "/lib/rt.jar");
        assertNotNull(jarAbsolutePath);
        assertEquals(rtJarFile.getAbsolutePath(), jarAbsolutePath);
    }


    @Test
    public void testToJarFile() throws Exception {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        JarFile jarFile = JarUtils.toJarFile(resourceURL);
        JarFile rtJarFile = new JarFile(new File(JAVA_HOME, "/lib/rt.jar"));
        assertNotNull(jarFile);
        assertEquals(rtJarFile.getName(), jarFile.getName());
    }

    public void testToJarFileOnException() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            URL url = new URL("http://www.google.com");
            JarFile jarFile = JarUtils.toJarFile(url);
        });
    }

    @Test
    public void testFindJarEntry() throws Exception {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        JarEntry jarEntry = JarUtils.findJarEntry(resourceURL);
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
        URL resourceURL = ClassLoaderUtils.getResource(classLoader, ClassLoaderUtils.ResourceType.PACKAGE, "io.microsphere");
        JarUtils.extract(resourceURL, targetDirectory, new JarEntryFilter() {
            @Override
            public boolean accept(JarEntry filteredObject) {
                return !filteredObject.isDirectory();
            }
        });
    }
}
