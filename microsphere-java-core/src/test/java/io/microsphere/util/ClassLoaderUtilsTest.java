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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
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
import static io.microsphere.util.ClassLoaderUtils.isLoadedClass;
import static io.microsphere.util.ClassLoaderUtils.isPresent;
import static io.microsphere.util.ClassLoaderUtils.isVerbose;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ClassLoaderUtils.removeClassPathURL;
import static io.microsphere.util.ClassLoaderUtils.setVerbose;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_12;
import static io.microsphere.util.VersionUtils.testCurrentJavaVersion;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @javax.annotation.Nonnull
    private static final Class<?> currentClass = ClassLoaderUtilsTest.class;

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
    public void testGetClassResource() {
        URL classResourceURL = getClassResource(classLoader, currentClass);
        assertNotNull(classResourceURL);
        log(classResourceURL);

        classResourceURL = getClassResource(classLoader, String.class.getName());
        assertNotNull(classResourceURL);
        log(classResourceURL);
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
        assertTrue(isLoadedClass(classLoader, currentClass));
        assertTrue(isLoadedClass(classLoader, javax.annotation.Nonnull.class));
        assertTrue(isLoadedClass(classLoader, String.class));
    }

    @Test
    public void testIsLoadedClassOnClassName() {
        assertTrue(isLoadedClass(classLoader, "java.lang.String"));
        assertTrue(isLoadedClass(classLoader, "javax.annotation.Nonnull"));
        assertTrue(isLoadedClass(classLoader, currentClass.getName()));
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
    }

    @Test
    public void testLoadClass() {
        assertLoadClass(currentClass);
        assertLoadClass(Nonnull.class);
        assertLoadClass(String.class);
    }

    @Test
    public void testLoadClassOnNotFound() {
        assertNull(loadClass(classLoader, "io.microsphere.not.found.class"));
    }

    @Test
    public void testLoadClassOnNull() {
        assertLoadClass(null, currentClass);
        assertLoadClass(null, Nonnull.class);
        assertLoadClass(null, String.class);
    }

    @Test
    public void testLoadClassWithCached() {
        assertLoadClass(classLoader, currentClass, true);
        assertLoadClass(classLoader, Nonnull.class, false);
        assertLoadClass(classLoader, String.class, true);
    }

    private void assertLoadClass(Class<?> type) {
        assertLoadClass(classLoader, type);
    }

    private void assertLoadClass(ClassLoader classLoader, Class<?> type) {
        assertLoadClass(classLoader, type, false);
    }

    private void assertLoadClass(ClassLoader classLoader, Class<?> type, boolean cached) {
        assertSame(type, loadClass(classLoader, type.getName(), cached));
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
        Set<URL> resourceURLs = getResources(classLoader, currentClass.getName() + CLASS_EXTENSION);
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);

        resourceURLs = getResources(classLoader, "javax.annotation.Nonnull.class");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);

        resourceURLs = getResources(classLoader, "/java/lang/CharSequence.class");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);

        resourceURLs = getResources(classLoader, "///META-INF//services/io.microsphere.event.EventListener");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);
    }

    @Test
    public void testGetResource() {
        URL resourceURL = getResource(classLoader, currentClass.getName() + CLASS_EXTENSION);
        assertNotNull(resourceURL);
        log(resourceURL);

        resourceURL = getResource(classLoader, "///java/lang/CharSequence.class");
        assertNotNull(resourceURL);
        log(resourceURL);

        resourceURL = getResource(classLoader, "//META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(resourceURL);
        log(resourceURL);
    }

    @Test
    public void testClassLoadingMXBean() {
        ClassLoadingMXBean classLoadingMXBean = ClassLoaderUtils.classLoadingMXBean;
        assertEquals(classLoadingMXBean.getTotalLoadedClassCount(), getTotalLoadedClassCount());
        assertEquals(classLoadingMXBean.getLoadedClassCount(), getLoadedClassCount());
        assertEquals(classLoadingMXBean.getUnloadedClassCount(), getUnloadedClassCount());
        assertEquals(classLoadingMXBean.isVerbose(), isVerbose());

        ClassLoaderUtils.setVerbose(true);
        assertTrue(isVerbose());
    }

    @Test
    public void testGetInheritableClassLoaders() {
        Set<ClassLoader> classLoaders = getInheritableClassLoaders(classLoader);
        assertNotNull(classLoaders);
        assertTrue(classLoaders.size() > 1);
        log(classLoaders);
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
    public void testGetAllLoadedClassesMap() {
        if (isLessThanJava12) {
            Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = getAllLoadedClassesMap(classLoader);
            assertNotNull(allLoadedClassesMap);
            assertFalse(allLoadedClassesMap.isEmpty());
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
        Set<Class<?>> allLoadedClasses = findLoadedClassesInClassPaths(classLoader, ClassPathUtils.getClassPaths());
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
        URLClassLoader parent = URLClassLoader.newInstance(new URL[0]);
        assertFindURLClassLoader(parent, parent);

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[0], getDefaultClassLoader());
        assertFindURLClassLoader(classLoader, classLoader);

        SecureClassLoader secureClassLoader = new SecureClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return super.loadClass(name, resolve);
            }
        };
        findURLClassLoader(secureClassLoader);
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
