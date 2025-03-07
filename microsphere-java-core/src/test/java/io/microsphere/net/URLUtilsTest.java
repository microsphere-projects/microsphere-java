/**
 *
 */
package io.microsphere.net;

import io.microsphere.net.console.Handler;
import io.microsphere.util.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.net.URLUtils.attachURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.buildMatrixString;
import static io.microsphere.net.URLUtils.clearURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.decode;
import static io.microsphere.net.URLUtils.encode;
import static io.microsphere.net.URLUtils.getSubProtocol;
import static io.microsphere.net.URLUtils.getURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.isArchiveURL;
import static io.microsphere.net.URLUtils.isDirectoryURL;
import static io.microsphere.net.URLUtils.isJarURL;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.net.URLUtils.resolveArchiveEntryPath;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.net.URLUtils.resolveBasePath;
import static io.microsphere.net.URLUtils.resolveMatrixParameters;
import static io.microsphere.net.URLUtils.resolveProtocol;
import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static io.microsphere.net.URLUtils.resolveSubProtocols;
import static io.microsphere.net.URLUtils.toExternalForm;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.StringUtils.substringBeforeLast;
import static io.microsphere.util.SystemUtils.USER_DIR;
import static java.nio.file.Paths.get;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link URLUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see URLUtilsTest
 * @since 1.0.0
 */
public class URLUtilsTest {

    private static final String TEST_PATH = "/abc/def";

    private static final String UTF8_ENCODED_TEST_PATH = "%2Fabc%2Fdef";

    private static final String FILE_URL_PREFIX = "file://";

    private static URL userDirURL;

    private static URL classFileURL;

    private static URL classArchiveEntryURL;

    @BeforeAll
    public static void beforeAll() throws Throwable {
        ClassLoader classLoader = getDefaultClassLoader();
        userDirURL = new URL(FILE_URL_PREFIX + USER_DIR);
        classFileURL = getClassResource(classLoader, StringUtils.class);
        classArchiveEntryURL = getClassResource(classLoader, Nonnull.class);
    }

    @AfterEach
    public void after() {
        clearURLStreamHandlerFactory();
    }


    @Test
    public void testResolveArchiveEntryPath() {
        ClassLoader classLoader = getDefaultClassLoader();
        URL resourceURL = getClassResource(classLoader, Nonnull.class);
        String expectedPath = "javax/annotation/Nonnull.class";
        String relativePath = resolveArchiveEntryPath(resourceURL);
        assertEquals(expectedPath, relativePath);
    }

    @Test
    public void testResolveArchiveEntryPathOnFile() {
        assertNull(resolveArchiveEntryPath(userDirURL));
    }

    @Test
    public void testResolveBasePathOnFile() {
        assertEquals(get(USER_DIR), get(resolveBasePath(userDirURL)));
    }

    @Test
    public void testResolveBasePathOnArchiveEntry() throws MalformedURLException {
        String basePath = resolveBasePath(classArchiveEntryURL);
        assertNotNull(basePath);
        assertEquals(get(basePath), get(resolveBasePath(FILE_URL_PREFIX + basePath)));
    }

    @Test
    public void testResolveArchiveFile() {
        File archiveFile = resolveArchiveFile(classArchiveEntryURL);
        assertTrue(archiveFile.exists());
    }

    @Test
    public void testResolveArchiveFileOnClassFile() {
        File archiveFile = resolveArchiveFile(classFileURL);
        assertTrue(archiveFile.exists());
    }

    @Test
    public void testResolveQueryParameters() {
        String url = "https://www.google.com.hk/search?q=java&oq=java&sourceid=chrome&es_sm=122&ie=UTF-8";
        Map<String, List<String>> parametersMap = resolveQueryParameters(url);
        Map<String, List<String>> expectedParametersMap = new LinkedHashMap<>();
        expectedParametersMap.put("q", Arrays.asList("java"));
        expectedParametersMap.put("oq", Arrays.asList("java"));
        expectedParametersMap.put("sourceid", Arrays.asList("chrome"));
        expectedParametersMap.put("es_sm", Arrays.asList("122"));
        expectedParametersMap.put("ie", Arrays.asList("UTF-8"));

        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search";
        parametersMap = resolveQueryParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search?";
        parametersMap = resolveQueryParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);
    }

    @Test
    public void testEncodeAndDecode() {
        String path = "/abc/def";

        String encodedPath = encode(path);
        String decodedPath = decode(encodedPath);
        assertEquals(path, decodedPath);

        encodedPath = encode(path, "GBK");
        decodedPath = decode(encodedPath, "GBK");
        assertEquals(path, decodedPath);
    }

    @Test
    public void testResolveMatrixParameters() {
        String url = "https://www.google.com.hk/search;q=java;oq=java;sourceid=chrome;es_sm=122;ie=UTF-8";
        Map<String, List<String>> parametersMap = resolveMatrixParameters(url);
        Map<String, List<String>> expectedParametersMap = new LinkedHashMap<>();
        expectedParametersMap.put("q", Arrays.asList("java"));
        expectedParametersMap.put("oq", Arrays.asList("java"));
        expectedParametersMap.put("sourceid", Arrays.asList("chrome"));
        expectedParametersMap.put("es_sm", Arrays.asList("122"));
        expectedParametersMap.put("ie", Arrays.asList("UTF-8"));

        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search";
        parametersMap = resolveMatrixParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search;";
        parametersMap = resolveMatrixParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);
    }

    @Test
    public void testResolvePath() {
        String path = null;
        String expectedPath = null;
        String resolvedPath = null;

        resolvedPath = normalizePath(path);
        assertEquals(expectedPath, resolvedPath);

        path = "";
        expectedPath = "";
        resolvedPath = normalizePath(path);
        assertEquals(expectedPath, resolvedPath);

        path = "/abc/";
        expectedPath = "/abc/";
        resolvedPath = normalizePath(path);
        assertEquals(expectedPath, resolvedPath);

        path = "//abc///";
        expectedPath = "/abc/";
        resolvedPath = normalizePath(path);
        assertEquals(expectedPath, resolvedPath);


        path = "//\\abc///";
        expectedPath = "/abc/";
        resolvedPath = normalizePath(path);
        assertEquals(expectedPath, resolvedPath);
    }

    @Test
    public void testEncode() {
        assertEquals(UTF8_ENCODED_TEST_PATH, encode(TEST_PATH));
    }

    @Test
    public void testEncodeWithEncoding() {
        assertEquals(UTF8_ENCODED_TEST_PATH, encode(TEST_PATH, "UTF-8"));
    }

    @Test
    public void testEncodeOnInvalidEncoding() {
        assertThrows(IllegalArgumentException.class, () -> encode(TEST_PATH, "unknown-encoding"));
    }

    @Test
    public void testDecode() {
        assertEquals(TEST_PATH, decode(UTF8_ENCODED_TEST_PATH));
    }

    @Test
    public void testDecodeWithEncoding() {
        assertEquals(TEST_PATH, decode(UTF8_ENCODED_TEST_PATH, "UTF-8"));
    }

    @Test
    public void testDecodeOnInvalidEncoding() {
        assertThrows(IllegalArgumentException.class, () -> decode(UTF8_ENCODED_TEST_PATH, "unknown-encoding"));
    }

    @Test
    public void testIsDirectoryURL() throws Exception {
        ClassLoader classLoader = getDefaultClassLoader();
        URL resourceURL = getClassResource(classLoader, StringUtils.class);
        assertFalse(isDirectoryURL(classFileURL));

        String externalForm = null;
        externalForm = substringBeforeLast(resourceURL.toExternalForm(), StringUtils.class.getSimpleName() + ".class");
        resourceURL = new URL(externalForm);
        assertTrue(isDirectoryURL(resourceURL));

        resourceURL = getClassResource(classLoader, String.class);
        assertFalse(isDirectoryURL(resourceURL));

        resourceURL = getClassResource(classLoader, getClass());
        assertFalse(isDirectoryURL(resourceURL));

        externalForm = substringBeforeLast(resourceURL.toExternalForm(), getClass().getSimpleName() + ".class");
        resourceURL = new URL(externalForm);
        assertTrue(isDirectoryURL(resourceURL));
    }

    @Test
    public void testIsJarURLOnDirectory() {
        assertFalse(isJarURL(userDirURL));
    }

    @Test
    public void testIsJarURLOnClassFile() {
        assertFalse(isJarURL(classFileURL));
    }

    @Test
    public void testIsJarURLOnArchiveEntry() {
        assertTrue(isJarURL(classArchiveEntryURL));
    }

    @Test
    public void testIsJarURLOnArchiveFile() throws MalformedURLException {
        File archiveFile = resolveArchiveFile(classArchiveEntryURL);
        assertTrue(isJarURL(new URL(FILE_URL_PREFIX + archiveFile.getAbsolutePath())));
    }

    @Test
    public void testIsArchiveURLOnJar() throws MalformedURLException {
        assertTrue(isArchiveURL(classArchiveEntryURL));
    }

    @Test
    public void testIsArchiveURLOnFile() throws MalformedURLException {
        File archiveFile = resolveArchiveFile(classArchiveEntryURL);
        assertTrue(isArchiveURL(new URL(FILE_URL_PREFIX + archiveFile.getAbsolutePath())));
    }

    @Test
    public void testIsArchiveURLOnNotJar() throws MalformedURLException {
        assertFalse(isArchiveURL(classFileURL));
    }

//
//    @Test
//    public void testIsArchiveURLOnWar() throws MalformedURLException {
//        testIsArchiveURLOn(WAR_PROTOCOL);
//    }
//
//    @Test
//    public void testIsArchiveURLOnEar() throws MalformedURLException {
//        testIsArchiveURLOn(EAR_PROTOCOL);
//    }
//
//    private void testIsArchiveURLOn(String protocol) throws MalformedURLException {
//        String url = classArchiveEntryURL.toString().replace(JAR_PROTOCOL, protocol);
//        assertTrue(isArchiveURL(new URL(url)));
//    }

    @Test
    public void testBuildMatrixString() {
        String matrixString = buildMatrixString("n", "1");
        assertEquals(";n=1", matrixString);

        matrixString = buildMatrixString("n", "1", "2");
        assertEquals(";n=1;n=2", matrixString);

        matrixString = buildMatrixString("n", "1", "2", "3");
        assertEquals(";n=1;n=2;n=3", matrixString);
    }

    @Test
    public void testToExternalFormOnDirectory() {
        testToExternalForm(userDirURL);
    }

    @Test
    public void testToExternalFormOnClassFile() {
        testToExternalForm(classFileURL);
    }

    @Test
    public void testToExternalFormOnClassArchiveEntry() {
        testToExternalForm(classArchiveEntryURL);
    }

    @Test
    public void testToExternalFormWithMatrix() throws MalformedURLException {
        String url = "https://acme.com/;q=java";
        testToExternalForm(url);
    }

    @Test
    public void testToExternalFormWithQueryString() throws MalformedURLException {
        String url = "https://acme.com?q=java&key=1";
        testToExternalForm(url);
    }

    @Test
    public void testToExternalFormWithPath() throws MalformedURLException {
        String url = "https://acme.com/abc/def";
        testToExternalForm(url);
    }

    @Test
    public void testToExternalFormWithRef() throws MalformedURLException {
        String url = "https://acme.com/abc/def#hash";
        testToExternalForm(url);
    }

    private void testToExternalForm(String urlString) throws MalformedURLException {
        testToExternalForm(new URL(urlString));
    }

    private void testToExternalForm(URL url) {
        assertEquals(url.toExternalForm(), toExternalForm(url));
    }

    @Test
    public void testGetSubProtocol() {
        String url = "https://acme.com/;_sp=classpath";
        assertEquals("classpath", getSubProtocol(url));
    }

    @Test
    public void testGetSubProtocolWithSubProtocol() {
        String url = "https://acme.com/";
        assertNull(getSubProtocol(url));
    }

    @Test
    public void testResolveSubProtocolsFromMatrix() throws MalformedURLException {
        URL url = new URL("https://localhost/;_sp=text;_sp=properties");
        List<String> subProtocols = resolveSubProtocols(url);
        assertEquals(2, subProtocols.size());
        assertEquals("text", subProtocols.get(0));
        assertEquals("properties", subProtocols.get(1));
    }

    @Test
    public void testResolveSubProtocolsFromProtocol() throws MalformedURLException {
        new Handler();
        URL url = new URL("console:text:properties://localhost");
        List<String> subProtocols = resolveSubProtocols(url);
        assertEquals(2, subProtocols.size());
        assertEquals("text", subProtocols.get(0));
        assertEquals("properties", subProtocols.get(1));
    }

    @Test
    public void testResolveProtocol() {
        assertNull(resolveProtocol(null));
        assertNull(resolveProtocol(""));
        assertNull(resolveProtocol(" "));
        assertNull(resolveProtocol(":"));
        assertNull(resolveProtocol(" :"));
        assertNull(resolveProtocol(" a:"));
        assertNull(resolveProtocol("a :"));
        assertEquals("ftp", resolveProtocol("ftp://..."));
        assertEquals("http", resolveProtocol("http://..."));
    }

    @Test
    public void testAttachURLStreamHandlerFactory() {
        URLStreamHandlerFactory factory = new StandardURLStreamHandlerFactory();
        attachURLStreamHandlerFactory(factory);
        assertSame(factory, getURLStreamHandlerFactory());


        attachURLStreamHandlerFactory(factory);
        CompositeURLStreamHandlerFactory compositeFactory = (CompositeURLStreamHandlerFactory) getURLStreamHandlerFactory();
        assertNotSame(factory, compositeFactory);
        assertEquals(1, compositeFactory.getFactories().size());
        assertSame(factory, compositeFactory.getFactories().get(0));
        assertEquals(CompositeURLStreamHandlerFactory.class, compositeFactory.getClass());
    }


}
