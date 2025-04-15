/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.Comparator;
import java.util.EventListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.collection.MapUtils.toFixedMap;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.management.JmxUtils.getClassLoadingMXBean;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.util.ArrayUtils.EMPTY_URL_ARRAY;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.doLoadClass;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.ClassLoaderUtils.findLoadedClass;
import static io.microsphere.util.ClassLoaderUtils.findLoadedClasses;
import static io.microsphere.util.ClassLoaderUtils.findLoadedClassesInClassPath;
import static io.microsphere.util.ClassLoaderUtils.findLoadedClassesInClassPaths;
import static io.microsphere.util.ClassLoaderUtils.findURLClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getAllLoadedClasses;
import static io.microsphere.util.ClassLoaderUtils.getAllLoadedClassesMap;
import static io.microsphere.util.ClassLoaderUtils.getCallerClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.getInheritableClassLoaders;
import static io.microsphere.util.ClassLoaderUtils.getLoadedClassCount;
import static io.microsphere.util.ClassLoaderUtils.getLoadedClasses;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.ClassLoaderUtils.getResources;
import static io.microsphere.util.ClassLoaderUtils.getTotalLoadedClassCount;
import static io.microsphere.util.ClassLoaderUtils.getUnloadedClassCount;
import static io.microsphere.util.ClassLoaderUtils.invokeFindLoadedClassMethod;
import static io.microsphere.util.ClassLoaderUtils.isLoadedClass;
import static io.microsphere.util.ClassLoaderUtils.isPresent;
import static io.microsphere.util.ClassLoaderUtils.isVerbose;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ClassLoaderUtils.logOnFindLoadedClassInvocationFailed;
import static io.microsphere.util.ClassLoaderUtils.newURLClassLoader;
import static io.microsphere.util.ClassLoaderUtils.removeClassPathURL;
import static io.microsphere.util.ClassLoaderUtils.resolveURLClassLoader;
import static io.microsphere.util.ClassLoaderUtils.setVerbose;
import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_12;
import static io.microsphere.util.VersionUtils.testCurrentJavaVersion;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.currentThread;
import static java.net.URLClassLoader.newInstance;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ClassLoaderUtils} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassLoaderUtils
 * @since 1.0.0
 */
public class ClassLoaderUtilsTest extends AbstractTestCase {

    private static final boolean isLessThanJava12 = testCurrentJavaVersion("<", JAVA_VERSION_12);

    private static final boolean verbose = isVerbose();

    private static final Class<?> currentClass = ClassLoaderUtilsTest.class;

    private static final List<Class<?>> testClasses = ofList(currentClass, Nonnull.class, CharSequence.class, EventListener.class);

    private static final Map<Class<?>, String> testClassNamesMap = toFixedMap(testClasses, type -> ofEntry(type, type.getName()));

    private static final String[] testResources = ofArray(
            currentClass.getName() + CLASS_EXTENSION,
            "javax.annotation.Nonnull.class",
            "/java/lang/CharSequence.class",
            "///META-INF//services/io.microsphere.event.EventListener"
    );

    @AfterAll
    public static void afterAll() {
        setVerbose(verbose);
    }

    @Test
    public void testFields() throws Exception {

        Set<Field> allFields = findAllDeclaredFields(ClassLoader.class);

        Set<ClassLoader> classLoaders = getInheritableClassLoaders(classLoader);
        for (ClassLoader classLoader : classLoaders) {
            log("ClassLoader : {}", classLoader);
            for (Field field : allFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    log("Field name : {} , value : {}", field.getName(), field.get(classLoader));
                }
            }
        }

    }

    @Test
    public void testGetLoadedClassCount() {
        long count = getLoadedClassCount();
        assertTrue(count > 0);
    }

    @Test
    public void testGetUnloadedClassCount() {
        long count = getUnloadedClassCount();
        assertTrue(count > -1);
    }

    @Test
    public void testGetTotalLoadedClassCount() {
        long count = getTotalLoadedClassCount();
        assertTrue(count > 0);
    }

    @Test
    public void testIsVerbose() {
        boolean verbose = isVerbose();
        assertFalse(verbose);
    }

    @Test
    public void testSetVerbose() {
        boolean verbose = isVerbose();
        setVerbose(true);
        assertTrue(isVerbose());
        setVerbose(false);
        assertFalse(isVerbose());
        setVerbose(verbose);
    }

    @Test
    public void testClassLoadingMXBean() {
        ClassLoadingMXBean classLoadingMXBean = getClassLoadingMXBean();
        assertEquals(classLoadingMXBean.getTotalLoadedClassCount(), getTotalLoadedClassCount());
        assertEquals(classLoadingMXBean.getLoadedClassCount(), getLoadedClassCount());
        assertEquals(classLoadingMXBean.getUnloadedClassCount(), getUnloadedClassCount());
        assertEquals(classLoadingMXBean.isVerbose(), isVerbose());

        setVerbose(true);
        assertTrue(isVerbose());
    }

    @Test
    public void testGetDefaultClassLoader() {
        Thread currentThread = currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        try {
            assertEquals(classLoader, getDefaultClassLoader());
            currentThread.setContextClassLoader(null);
            assertEquals(ClassLoaderUtils.class.getClassLoader(), getDefaultClassLoader());
            currentThread.setContextClassLoader(getSystemClassLoader().getParent());
        } finally {
            // recovery
            currentThread.setContextClassLoader(classLoader);
        }
    }

    @Test
    public void testGetClassLoader() {
        ClassLoader classLoader = currentClass.getClassLoader();
        // ClassLoaderUtilsTest -> classLoader
        assertSame(classLoader, getClassLoader(currentClass));
        // String.class -> Bootstrap ClassLoader(null)
        assertSame(classLoader, getClassLoader(String.class));
    }

    @Test
    public void testGetClassLoaderOnNull() {
        ClassLoader classLoader = currentClass.getClassLoader();
        assertSame(classLoader, getClassLoader(null));
    }

    @Test
    public void testGetCallerClassLoader() {
        assertSame(currentClass.getClassLoader(), getCallerClassLoader());
    }

    @Test
    public void testFindLoadedClasses() {
        Set<Class<?>> classes = findLoadedClasses(classLoader, currentClass.getName());
        assertEquals(1, classes.size());
        assertTrue(classes.contains(currentClass));

        classes = findLoadedClasses(classLoader, ofList(currentClass.getName(), currentClass.getName(), currentClass.getName()));
        assertEquals(1, classes.size());
        assertTrue(classes.contains(currentClass));
    }

    @Test
    public void testFindLoadedClassesOnNullClassLoader() {
        Set<Class<?>> classes = findLoadedClasses(null, currentClass.getName());
        assertEquals(1, classes.size());
        assertTrue(classes.contains(currentClass));
    }

    @Test
    public void testFindLoadedClassesOnNullClassNames() {
        Set<Class<?>> classes = findLoadedClasses(classLoader, TEST_NULL_STRING_ARRAY);
        assertSame(emptySet(), classes);

        classes = findLoadedClasses(classLoader, TEST_NULL_ITERABLE);
        assertSame(emptySet(), classes);
    }

    @Test
    public void testFindLoadedClassesOnEmptyClassNames() {
        Set<Class<?>> classes = findLoadedClasses(classLoader, TEST_EMPTY_LIST);
        assertSame(emptySet(), classes);

        classes = findLoadedClasses(classLoader, TEST_EMPTY_SET);
        assertSame(emptySet(), classes);

        classes = findLoadedClasses(classLoader, TEST_EMPTY_COLLECTION);
        assertSame(emptySet(), classes);
    }

    @Test
    public void testIsLoadedClassOnClass() {
        assertIsLoadedClass(classLoader, false);
    }

    @Test
    public void testIsLoadedClassOnClassName() {
        assertIsLoadedClass(classLoader, true);
    }

    @Test
    public void testIsLoadedClassOnNull() {
        assertIsLoadedClass(null, true);
        assertIsLoadedClass(null, false);
    }

    private void assertIsLoadedClass(ClassLoader classLoader, boolean testClassName) {
        testClassNamesMap.forEach((type, name) -> assertTrue(testClassName ? isLoadedClass(classLoader, name) : isLoadedClass(classLoader, type)));
    }

    @Test
    public void testFindLoadedClass() {
        Class<?> type = null;
        if (isLessThanJava12) {
            for (Class<?> class_ : getAllLoadedClasses(classLoader)) {
                type = findLoadedClass(classLoader, class_.getName());
                assertEquals(class_, type);
            }
        }

        type = findLoadedClass(classLoader, String.class.getName());
        assertEquals(String.class, type);

        type = findLoadedClass(classLoader, Double.class.getName());
        assertEquals(Double.class, type);

        type = findLoadedClass(classLoader, "java/lang/String.class");
        assertNull(type);
    }

    @Test
    public void testLoadClass() {
        assertLoadClass(classLoader);
    }

    @Test
    public void testLoadClassOnNullClassLoader() {
        assertLoadClass(null);
    }

    @Test
    public void testLoadClassOnNullClassName() {
        assertNull(loadClass(this.classLoader, null));

        assertNull(loadClass(this.classLoader, null, true));
    }

    @Test
    public void testLoadClassOnBlankClassName() {
        assertNull(loadClass(this.classLoader, ""));
        assertNull(loadClass(this.classLoader, " "));

        assertNull(loadClass(this.classLoader, "", true));
        assertNull(loadClass(this.classLoader, " ", true));
    }

    @Test
    public void testDoLoadClassOnNull() {
        assertNull(doLoadClass(null, ClassLoaderUtilsTest.class.getName()));
        assertNull(doLoadClass(null, Nonnull.class.getName()));
        assertNull(doLoadClass(null, String.class.getName()));
        assertNull(doLoadClass(null, null));
    }

    @Test
    public void testLoadClassOnNotFound() {
        assertNull(loadClass(classLoader, "io.microsphere.not.found.class"));
    }

    @Test
    public void testLoadClassOnNull() {
        assertLoadClass(null);
    }

    @Test
    public void testLoadClassWithCached() {
        assertLoadClass(classLoader, false);
        assertLoadClass(classLoader, true);
    }

    private void assertLoadClass(ClassLoader classLoader) {
        testClassNamesMap.forEach((type, name) -> assertSame(type, loadClass(classLoader, name)));
    }

    private void assertLoadClass(ClassLoader classLoader, boolean cached) {
        testClassNamesMap.forEach((type, name) -> assertSame(type, loadClass(classLoader, name, cached)));
    }

    @Test
    public void testGetResources() throws IOException {
        assertGetResources(classLoader);
    }

    @Test
    public void testGetResourcesOnNullClassLoader() throws IOException {
        assertGetResources(null);
    }

    private void assertGetResources(ClassLoader classLoader) throws IOException {
        for (String testResource : testResources) {
            Set<URL> resourceURLs = getResources(classLoader, testResource);
            assertNotNull(resourceURLs);
            assertEquals(1, resourceURLs.size());
            log(resourceURLs);
        }
    }

    @Test
    public void testGetResource() {
        assertGetResource(classLoader);
    }

    @Test
    public void testGetResourceOnNullClassLoader() {
        assertGetResource();
        assertGetResource(null);
    }

    private void assertGetResource() {
        for (String testResource : testResources) {
            URL resourceURL = getResource(testResource);
            assertNotNull(resourceURL);
            log(resourceURL);
        }
    }

    private void assertGetResource(ClassLoader classLoader) {
        for (String testResource : testResources) {
            URL resourceURL = getResource(classLoader, testResource);
            assertNotNull(resourceURL);
            log(resourceURL);
        }
    }

    @Test
    public void testGetClassResource() {
        assertGetClassResource(this.classLoader);
    }

    @Test
    public void testGetClassResourceOnNullClassLoader() {
        assertGetClassResource(null);
    }

    private void assertGetClassResource(ClassLoader classLoader) {
        testClasses.forEach(type -> assertNotNull(getClassResource(classLoader, type)));
    }

    @Test
    public void testGetInheritableClassLoaders() {
        Set<ClassLoader> classLoaders = getInheritableClassLoaders(classLoader);
        assertNotNull(classLoaders);
        assertTrue(classLoaders.size() > 1);
        log(classLoaders);
    }

    @Test
    public void testGetAllLoadedClassesMap() {
        if (isLessThanJava12) {
            Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = getAllLoadedClassesMap(classLoader);
            assertNotNull(allLoadedClassesMap);
            assertFalse(allLoadedClassesMap.isEmpty());
        }
    }

    @Test
    public void testGetAllLoadedClasses() {
        if (isLessThanJava12) {
            Set<Class<?>> classesSet = getAllLoadedClasses(classLoader);
            assertNotNull(classesSet);
            assertFalse(classesSet.isEmpty());

            classesSet = getAllLoadedClasses(getSystemClassLoader());
            assertNotNull(classesSet);
            assertFalse(classesSet.isEmpty());
            log(classesSet);
        }
    }

    @Test
    public void testGetLoadedClasses() {
        if (isLessThanJava12) {
            Set<Class<?>> classesSet = getLoadedClasses(classLoader);
            assertNotNull(classesSet);
            assertFalse(classesSet.isEmpty());

            classesSet = getLoadedClasses(getSystemClassLoader());
            assertNotNull(classesSet);
            assertFalse(classesSet.isEmpty());
            log(classesSet);
        }
    }

    @Test
    public void testFindLoadedClassesInClassPath() {
        Double d = null;
        Set<Class<?>> allLoadedClasses = findLoadedClassesInClassPath(classLoader);

        Set<Class<?>> classesSet = getAllLoadedClasses(classLoader);

        Set<Class<?>> remainingClasses = new LinkedHashSet<>(allLoadedClasses);

        remainingClasses.addAll(classesSet);

        Set<Class<?>> sortedClasses = new TreeSet(new ClassComparator());
        sortedClasses.addAll(remainingClasses);

        log(sortedClasses);

        int loadedClassesSize = allLoadedClasses.size() + classesSet.size();

        int loadedClassCount = getLoadedClassCount();

        log(loadedClassesSize);
        log(loadedClassCount);
    }

    @Test
    public void testFindLoadedClassesInClassPaths() {
        Set<Class<?>> allLoadedClasses = findLoadedClassesInClassPaths(classLoader, getClassPaths());
        assertFalse(allLoadedClasses.isEmpty());
    }

    @Test
    public void testRemoveClassPathURL() {
        assertFalse(removeClassPathURL(new TestClassLoader(), null));
        Set<URL> urls = findAllClassPathURLs(classLoader);
        for (URL url : urls) {
            String path = url.getPath();
            if (path.contains("jmh-generator-annprocess")) {
                assertTrue(removeClassPathURL(classLoader, url));
            }
        }
    }

    @Test
    public void testIsPresent() {
        assertFalse(isPresent(null));
        assertFalse(isPresent(""));
        assertFalse(isPresent(" "));
        assertFalse(isPresent(null, null));
        assertFalse(isPresent("", null));
        assertFalse(isPresent(" ", null));
        assertFalse(isPresent("NotFound"));
        assertFalse(isPresent("NotFound", getDefaultClassLoader()));
        assertTrue(isPresent("java.lang.String"));
        assertTrue(isPresent("java.lang.String", null));
        assertTrue(isPresent("java.lang.String", getSystemClassLoader()));
        assertTrue(isPresent("java.lang.String", ClassLoaderUtils.class.getClassLoader()));
    }

    @Test
    public void testFindURLClassLoader() {
        URLClassLoader parent = newInstance(EMPTY_URL_ARRAY);
        assertFindURLClassLoader(parent, parent);

        URLClassLoader classLoader = newInstance(EMPTY_URL_ARRAY, getDefaultClassLoader());
        assertFindURLClassLoader(classLoader, classLoader);

        SecureClassLoader secureClassLoader = new SecureClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return super.loadClass(name, resolve);
            }
        };
        findURLClassLoader(secureClassLoader);
    }

    @Test
    public void testNewURLClassLoaderWithURLIterable() {
        Set<URL> urls = findAllClassPathURLs(this.classLoader);
        URLClassLoader urlClassLoader = newURLClassLoader(urls, classLoader);
        assertArrayEquals(asArray(urls, URL.class), urlClassLoader.getURLs());
    }

    @Test
    public void testNewURLClassLoaderWithURLIterableOnEmpty() {
        URLClassLoader urlClassLoader = newURLClassLoader(emptyList(), classLoader);
        assertArrayEquals(EMPTY_URL_ARRAY, urlClassLoader.getURLs());

        urlClassLoader = newURLClassLoader(emptyList(), null);
        assertArrayEquals(EMPTY_URL_ARRAY, urlClassLoader.getURLs());
    }

    @Test
    public void testNewURLClassLoaderWithURLIterableOnNull() {
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader((Iterable<URL>) null, classLoader));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader((Iterable<URL>) null, null));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader((Iterable<URL>) null));

        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader(ofList((URL) null), classLoader));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader(ofList((URL) null), null));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader(ofList((URL) null)));
    }

    @Test
    public void testNewURLClassLoaderWithURLArray() {
        Set<URL> urls = findAllClassPathURLs(this.classLoader);
        URL[] urlsArray = asArray(urls, URL.class);

        URLClassLoader urlClassLoader = newURLClassLoader(urlsArray, classLoader);
        assertArrayEquals(urlsArray, urlClassLoader.getURLs());

        urlClassLoader = newURLClassLoader(urlsArray, classLoader, true);
        assertArrayEquals(urlsArray, urlClassLoader.getURLs());

        urlClassLoader = newURLClassLoader(urlsArray);
        assertArrayEquals(urlsArray, urlClassLoader.getURLs());

        newURLClassLoader(urlsArray, true);
        assertArrayEquals(urlsArray, urlClassLoader.getURLs());
    }

    @Test
    public void testNewURLClassLoaderWithURLArrayOnEmpty() {
        URLClassLoader urlClassLoader = newURLClassLoader(EMPTY_URL_ARRAY, classLoader);
        assertArrayEquals(EMPTY_URL_ARRAY, urlClassLoader.getURLs());

        urlClassLoader = newURLClassLoader(EMPTY_URL_ARRAY, classLoader, true);
        assertArrayEquals(EMPTY_URL_ARRAY, urlClassLoader.getURLs());

        urlClassLoader = newURLClassLoader(EMPTY_URL_ARRAY);
        assertArrayEquals(EMPTY_URL_ARRAY, urlClassLoader.getURLs());

        urlClassLoader = newURLClassLoader(EMPTY_URL_ARRAY, true);
        assertArrayEquals(EMPTY_URL_ARRAY, urlClassLoader.getURLs());
    }

    @Test
    public void testNewURLClassLoaderWithURLArrayOnNull() {
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader((URL[]) null, classLoader));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader((URL[]) null, null));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader((URL[]) null));

        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader(ofArray((URL) null), classLoader));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader(ofArray((URL) null), null));
        assertThrows(IllegalArgumentException.class, () -> newURLClassLoader(ofArray((URL) null)));
    }

    @Test
    public void testResolveURLClassLoader() {
        URLClassLoader urlClassLoader = resolveURLClassLoader(this.classLoader);
        assertNotNull(urlClassLoader.getURLs());
        assertSame(urlClassLoader, resolveURLClassLoader(urlClassLoader));
    }

    @Test
    public void testResolveURLClassLoaderOnNull() {
        Set<URL> urls = findAllClassPathURLs(getDefaultClassLoader());
        URLClassLoader urlClassLoader = resolveURLClassLoader(null);
        assertArrayEquals(asArray(urls, URL.class), urlClassLoader.getURLs());
    }

    @Test
    public void testInvokeFindLoadedClassMethod() {
        assertInvokeFindLoadedClassMethod(ClassLoaderUtilsTest.class);
        assertInvokeFindLoadedClassMethod(Nonnull.class);
        assertInvokeFindLoadedClassMethod(String.class);
    }

    @Test
    public void testInvokeFindLoadedClassMethodOnFailed() {
        assertNull(invokeFindLoadedClassMethod(null, ClassLoaderUtilsTest.class.getName()));
    }

    @Test
    public void testLogOnFindLoadedClassInvocationFailed() {
        logOnFindLoadedClassInvocationFailed(this.classLoader, "NotFound", new Exception("For testing"));
        logOnFindLoadedClassInvocationFailed(null, "NotFound", new Exception("For testing"));
        logOnFindLoadedClassInvocationFailed(null, null, new Exception("For testing"));
        logOnFindLoadedClassInvocationFailed(null, null, null);
    }

    private void assertInvokeFindLoadedClassMethod(Class clazz) {
        assertSame(clazz, invokeFindLoadedClassMethod(this.classLoader, clazz.getName()));
    }

    private void assertFindURLClassLoader(ClassLoader classLoader, ClassLoader expectedClassLoader) {
        URLClassLoader urlClassLoader = findURLClassLoader(classLoader);
        assertSame(expectedClassLoader, urlClassLoader);
    }

    private static class TestClassLoader extends ClassLoader {
    }


    private static class ClassComparator implements Comparator<Class<?>> {

        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            String cn1 = o1.getName();
            String cn2 = o2.getName();
            return cn1.compareTo(cn2);
        }
    }

}
