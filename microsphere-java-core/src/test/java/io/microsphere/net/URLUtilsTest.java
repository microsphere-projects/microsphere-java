/**
 *
 */
package io.microsphere.net;

import io.microsphere.util.ClassLoaderUtils;
import io.microsphere.util.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.net.URLUtils.attachURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.buildMatrixString;
import static io.microsphere.net.URLUtils.decode;
import static io.microsphere.net.URLUtils.encode;
import static io.microsphere.net.URLUtils.getURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.net.URLUtils.resolveArchiveEntryPath;
import static io.microsphere.net.URLUtils.resolveMatrixParameters;
import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static io.microsphere.util.StringUtils.substringBeforeLast;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @AfterEach
    public void after() {
        URLUtils.clearURLStreamHandlerFactory();
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
    public void testResolveRelativePath() throws MalformedURLException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, Nonnull.class);
        String expectedPath = "javax/annotation/Nonnull.class";
        String relativePath = resolveArchiveEntryPath(resourceURL);
        assertEquals(expectedPath, relativePath);
    }

    @Test
    public void testResolveArchiveFile() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        File archiveFile = URLUtils.resolveArchiveFile(resourceURL);
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
    public void testBuildMatrixString() {
        String matrixString = buildMatrixString("n", "1");
        assertEquals(";n=1", matrixString);

        matrixString = buildMatrixString("n", "1", "2");
        assertEquals(";n=1;n=2", matrixString);

        matrixString = buildMatrixString("n", "1", "2", "3");
        assertEquals(";n=1;n=2;n=3", matrixString);
    }

    @Test
    public void testIsDirectoryURL() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, StringUtils.class);
        assertFalse(URLUtils.isDirectoryURL(resourceURL));

        String externalForm = null;
        externalForm = substringBeforeLast(resourceURL.toExternalForm(), StringUtils.class.getSimpleName() + ".class");
        resourceURL = new URL(externalForm);
        assertTrue(URLUtils.isDirectoryURL(resourceURL));

        resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        assertFalse(URLUtils.isDirectoryURL(resourceURL));

        resourceURL = ClassLoaderUtils.getClassResource(classLoader, getClass());
        assertFalse(URLUtils.isDirectoryURL(resourceURL));

        externalForm = substringBeforeLast(resourceURL.toExternalForm(), getClass().getSimpleName() + ".class");
        resourceURL = new URL(externalForm);
        assertTrue(URLUtils.isDirectoryURL(resourceURL));
    }

    @Test
    public void testAttachURLStreamHandlerFactory() {
        URLStreamHandlerFactory factory = new StandardURLStreamHandlerFactory();
        attachURLStreamHandlerFactory(factory);
        assertSame(factory, getURLStreamHandlerFactory());

        URLStreamHandler handler = factory.createURLStreamHandler("http");
        assertEquals("sun.net.www.protocol.http.Handler", handler.getClass().getName());

        attachURLStreamHandlerFactory(factory);
        CompositeURLStreamHandlerFactory compositeFactory = (CompositeURLStreamHandlerFactory) getURLStreamHandlerFactory();
        assertNotSame(factory, compositeFactory);
        assertEquals(1, compositeFactory.getFactories().size());
        assertSame(factory, compositeFactory.getFactories().get(0));
        assertEquals(CompositeURLStreamHandlerFactory.class, compositeFactory.getClass());

    }

}
