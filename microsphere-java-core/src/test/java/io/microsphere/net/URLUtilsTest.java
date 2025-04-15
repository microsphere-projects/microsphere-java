/**
 *
 */
package io.microsphere.net;

import io.microsphere.AbstractTestCase;
import io.microsphere.net.console.Handler;
import io.microsphere.util.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.constants.SymbolConstants.AND_CHAR;
import static io.microsphere.net.URLUtils.FILE_URL_PREFIX;
import static io.microsphere.net.URLUtils.attachURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.buildMatrixString;
import static io.microsphere.net.URLUtils.buildURI;
import static io.microsphere.net.URLUtils.clearURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.close;
import static io.microsphere.net.URLUtils.decode;
import static io.microsphere.net.URLUtils.encode;
import static io.microsphere.net.URLUtils.getMutableURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.getSubProtocol;
import static io.microsphere.net.URLUtils.getURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.isArchiveURL;
import static io.microsphere.net.URLUtils.isDirectoryURL;
import static io.microsphere.net.URLUtils.isJarURL;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.net.URLUtils.ofURL;
import static io.microsphere.net.URLUtils.registerURLStreamHandler;
import static io.microsphere.net.URLUtils.resolveArchiveEntryPath;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.net.URLUtils.resolveBasePath;
import static io.microsphere.net.URLUtils.resolveMatrixParameters;
import static io.microsphere.net.URLUtils.resolveParameters;
import static io.microsphere.net.URLUtils.resolvePath;
import static io.microsphere.net.URLUtils.resolveProtocol;
import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static io.microsphere.net.URLUtils.resolveSubProtocols;
import static io.microsphere.net.URLUtils.toExternalForm;
import static io.microsphere.net.console.HandlerTest.TEST_CONSOLE_URL;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
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
 * @see URLUtilsTest
 * @since 1.0.0
 */
public class URLUtilsTest extends AbstractTestCase {

    private static final String TEST_PATH = "/abc/def";

    private static final String UTF8_ENCODED_TEST_PATH = "%2Fabc%2Fdef";

    private static final String userDirURLString = FILE_URL_PREFIX + USER_DIR;

    private static final String TEST_HTTP_BASE = "http://localhost/";

    private static final String TEST_HTTP_WITH_PATH = TEST_HTTP_BASE + TEST_PATH;

    private static final String TEST_HTTP_WITH_PATH_HASH = TEST_HTTP_WITH_PATH + "#hash";

    private static final String TEST_HTTP_WITH_QUERY_STRING = TEST_HTTP_BASE + "?q=java&oq=java&sourceid=chrome&es_sm=122&ie=UTF-8";

    private static final String TEST_HTTP_WITH_MATRIX_STRING = TEST_HTTP_BASE + ";q=java;oq=java;sourceid=chrome;es_sm=122;ie=UTF-8";

    private static final String TEST_HTTP_WITH_SP_MATRIX = TEST_HTTP_BASE + ";_sp=text;_sp=properties";

    private static URL userDirURL;

    private static URL classPathURL;

    private static URL classFileURL;

    private static URL classArchiveEntryURL;

    @BeforeAll
    public static void beforeAll() throws Throwable {
        userDirURL = get(USER_DIR).toUri().toURL();
        classPathURL = getResource(getClassLoader(URLUtilsTest.class), "/");
        classFileURL = getClassResource(StringUtils.class);
        classArchiveEntryURL = getClassResource(Nonnull.class);
    }

    @AfterEach
    public void after() {
        clearURLStreamHandlerFactory();
    }
    
    @Test
    public void testOfURL() throws MalformedURLException {
        assertEquals(new URL(userDirURLString), ofURL(userDirURLString));
    }

    @Test
    public void testOfURLOnFailed() {
        assertThrows(IllegalArgumentException.class, () -> ofURL("unknown://localhost/"));
    }

    @Test
    public void testResolveArchiveEntryPath() {
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
        assertEquals(new File(USER_DIR), new File(resolveBasePath(userDirURL)));
    }

    @Test
    public void testResolveBasePathOnArchiveEntry() throws MalformedURLException {
        String basePath = resolveBasePath(classArchiveEntryURL);
        assertNotNull(basePath);
        assertTrue(new File(basePath).exists());
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
        String url = TEST_HTTP_WITH_QUERY_STRING;
        Map<String, List<String>> parametersMap = resolveQueryParameters(url);
        Map<String, List<String>> expectedParametersMap = new LinkedHashMap<>();
        expectedParametersMap.put("q", ofList("java"));
        expectedParametersMap.put("oq", ofList("java"));
        expectedParametersMap.put("sourceid", ofList("chrome"));
        expectedParametersMap.put("es_sm", ofList("122"));
        expectedParametersMap.put("ie", ofList("UTF-8"));

        assertEquals(expectedParametersMap, parametersMap);

        url = TEST_HTTP_BASE;
        parametersMap = resolveQueryParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);

        url = TEST_HTTP_BASE;
        parametersMap = resolveQueryParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);
    }

    @Test
    public void testResolveMatrixParameters() {
        String url = TEST_HTTP_WITH_MATRIX_STRING;
        Map<String, List<String>> parametersMap = resolveMatrixParameters(url);
        Map<String, List<String>> expectedParametersMap = new LinkedHashMap<>();
        expectedParametersMap.put("q", ofList("java"));
        expectedParametersMap.put("oq", ofList("java"));
        expectedParametersMap.put("sourceid", ofList("chrome"));
        expectedParametersMap.put("es_sm", ofList("122"));
        expectedParametersMap.put("ie", ofList("UTF-8"));

        assertEquals(expectedParametersMap, parametersMap);

        url = TEST_HTTP_WITH_QUERY_STRING;
        parametersMap = resolveMatrixParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);

        url = TEST_HTTP_BASE;
        parametersMap = resolveMatrixParameters(url);
        expectedParametersMap = emptyMap();
        assertEquals(expectedParametersMap, parametersMap);
    }

    @Test
    public void testNormalizePath() {
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
        URL resourceURL = getClassResource(classLoader, StringUtils.class);
        assertFalse(isDirectoryURL(classFileURL));

        String externalForm = null;
        externalForm = substringBeforeLast(resourceURL.toExternalForm(), StringUtils.class.getSimpleName() + ".class");
        resourceURL = ofURL(externalForm);
        assertTrue(isDirectoryURL(resourceURL));

        resourceURL = getClassResource(classLoader, String.class);
        assertFalse(isDirectoryURL(resourceURL));

        resourceURL = getClassResource(classLoader, getClass());
        assertFalse(isDirectoryURL(resourceURL));

        externalForm = substringBeforeLast(resourceURL.toExternalForm(), getClass().getSimpleName() + ".class");
        resourceURL = ofURL(externalForm);
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
        assertTrue(isJarURL(ofURL(FILE_URL_PREFIX + archiveFile.getAbsolutePath())));
    }

    @Test
    public void testIsArchiveURLOnJar() throws MalformedURLException {
        assertTrue(isArchiveURL(classArchiveEntryURL));
    }

    @Test
    public void testIsArchiveURLOnFile() throws MalformedURLException {
        File archiveFile = resolveArchiveFile(classArchiveEntryURL);
        assertTrue(isArchiveURL(ofURL(FILE_URL_PREFIX + archiveFile.getAbsolutePath())));
    }

    @Test
    public void testIsArchiveURLOnNotJar() throws MalformedURLException {
        assertFalse(isArchiveURL(classFileURL));
    }

    @Test
    public void testBuildURI() {
        String path = buildURI("a", "b", "c");
        assertEquals("/a/b/c", path);

        path = buildURI("a/", "b\\\\", "//c");
        assertEquals("/a/b/c", path);
    }

    @Test
    public void testBuildURIOnEmpty() {
        String path = buildURI();
        assertEquals("/", path);

        path = buildURI(null);
        assertEquals("/", path);
    }

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
    public void testBuildMatrixStringOnEmptyMap() {
        assertNull(buildMatrixString(null));
        assertNull(buildMatrixString(emptyMap()));
        assertNull(buildMatrixString(newHashMap()));
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
    public void testToExternalFormWithMatrixString() throws MalformedURLException {
        testToExternalForm(TEST_HTTP_WITH_MATRIX_STRING);
    }

    @Test
    public void testToExternalFormWithQueryString() throws MalformedURLException {
        testToExternalForm(TEST_HTTP_WITH_QUERY_STRING);
    }

    @Test
    public void testToExternalFormWithPath() throws MalformedURLException {
        testToExternalForm(TEST_HTTP_WITH_PATH);
    }

    @Test
    public void testToExternalFormWithRef() throws MalformedURLException {
        testToExternalForm(TEST_HTTP_WITH_PATH_HASH);
    }

    private void testToExternalForm(String urlString) throws MalformedURLException {
        testToExternalForm(ofURL(urlString));
    }

    private void testToExternalForm(URL url) {
        assertEquals(url.toExternalForm(), toExternalForm(url));
    }

    @Test
    public void testGetSubProtocol() {
        assertEquals("text", getSubProtocol(TEST_HTTP_WITH_SP_MATRIX));
    }

    @Test
    public void testGetSubProtocolWithSubProtocol() {
        assertNull(getSubProtocol(TEST_HTTP_BASE));
    }

    @Test
    public void testResolveSubProtocolsFromMatrixString() throws MalformedURLException {
        URL url = ofURL(TEST_HTTP_WITH_SP_MATRIX);
        List<String> subProtocols = resolveSubProtocols(url);
        assertEquals(2, subProtocols.size());
        assertEquals("text", subProtocols.get(0));
        assertEquals("properties", subProtocols.get(1));
    }

    @Test
    public void testResolveSubProtocolsFromProtocol() throws MalformedURLException {
        new Handler();
        URL url = ofURL("console:text:properties://localhost");
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
    public void testResolvePath() {
        assertResolvePath(userDirURL);
        assertResolvePath(classPathURL);
        assertResolvePath(classFileURL);
    }

    @Test
    public void testResolvePathWithMatrixString() {
        assertEquals(new File(USER_DIR), new File(resolvePath(userDirURL)));
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

    @Test
    public void testGetURLStreamHandlerFactory() {
        assertNull(getURLStreamHandlerFactory());
    }

    @Test
    public void testRegisterURLStreamHandler() throws IOException {
        Handler handler = new Handler();
        registerURLStreamHandler(handler);
        URL url = ofURL(TEST_CONSOLE_URL);
        assertSame(System.in, url.openStream());
    }

    @Test
    public void testClose() throws IOException {
        URL url = ofURL("http://localhost");
        URLConnection urlConnection = url.openConnection();
        close(urlConnection);
    }

    @Test
    public void testCloseOnNonHttp() throws IOException {
        URL url = ofURL("ftp://localhost");
        URLConnection urlConnection = url.openConnection();
        close(urlConnection);
    }

    @Test
    public void testCloseOnNull() {
        close(null);
    }

    @Test
    public void testGetMutableURLStreamHandlerFactory() {
        MutableURLStreamHandlerFactory factory = getMutableURLStreamHandlerFactory();
        assertNull(factory);
    }

    @Test
    public void testGetMutableURLStreamHandlerFactoryFromAttached() {
        MutableURLStreamHandlerFactory factory = new MutableURLStreamHandlerFactory();
        attachURLStreamHandlerFactory(factory);
        assertSame(factory, getMutableURLStreamHandlerFactory());
    }

    @Test
    public void testGetMutableURLStreamHandlerFactoryOnCreateIfAbsent() {
        MutableURLStreamHandlerFactory factory = getMutableURLStreamHandlerFactory(true);
        assertNotNull(factory);
    }

    @Test
    public void testClearURLStreamHandlerFactory() {
        testGetMutableURLStreamHandlerFactoryFromAttached();
        clearURLStreamHandlerFactory();
        testGetMutableURLStreamHandlerFactory();
    }

    @Test
    public void testResolveParametersOnNull() {
        assertSame(emptyMap(), resolveParameters(null, AND_CHAR));
    }

    @Test
    public void testResolveParametersOnEmptyString() {
        assertSame(emptyMap(), resolveParameters(EMPTY_STRING, AND_CHAR));
    }


    private void assertResolvePath(URL url) {
        assertResolvePath(url, true);
    }

    private void assertResolvePath(URL url, boolean includeArchiveEntryPath) {
        String path = URLUtils.resolvePath(url, includeArchiveEntryPath);
        assertEquals(new File(path), new File(url.getPath()));
    }

}
