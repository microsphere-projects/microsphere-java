/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

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

import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.util.ClassLoaderUtils.findAllClassPathURLs;
import static io.microsphere.util.ClassLoaderUtils.findLoadedClass;
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
import static io.microsphere.util.ClassLoaderUtils.removeClassPathURL;
import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_12;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private static final boolean isLessThanJava12 = CURRENT_JAVA_VERSION.isLessThan(JAVA_VERSION_12);

    @Test
    public void testFields() throws Exception {

        Set<Field> allFields = findAllDeclaredFields(ClassLoader.class);

        Set<ClassLoader> classLoaders = getInheritableClassLoaders(classLoader);
        for (ClassLoader classLoader : classLoaders) {
            log("ClassLoader : {}", classLoader);
            for (Field field : allFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    String message = String.format("Field name : %s , value : %s", field.getName(), field.get(classLoader));
                    log(message);
                }
            }
        }

    }

    @Test
    public void testGetClassResource() {
        URL classResourceURL = getClassResource(classLoader, ClassLoaderUtilsTest.class);
        assertNotNull(classResourceURL);
        log(classResourceURL);

        classResourceURL = getClassResource(classLoader, String.class.getName());
        assertNotNull(classResourceURL);
        log(classResourceURL);
    }

    @Test
    public void testGetResource() {
        URL resourceURL = getResource(classLoader, ClassLoaderUtilsTest.class.getName() + CLASS_EXTENSION);
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
    public void testGetResources() throws IOException {
        Set<URL> resourceURLs = getResources(classLoader, ClassLoaderUtilsTest.class.getName() + CLASS_EXTENSION);
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);

        resourceURLs = getResources(classLoader, "///java/lang/CharSequence.class");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);

        resourceURLs = getResources(classLoader, "//META-INF/services/io.microsphere.event.EventListener");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        log(resourceURLs);
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
    public void testIsLoadedClass() {
        assertTrue(isLoadedClass(classLoader, String.class));
        assertTrue(isLoadedClass(classLoader, Double.class));
        assertTrue(isLoadedClass(classLoader, Double.class.getName()));
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
    public void testGetCount() {
        long count = getTotalLoadedClassCount();
        assertTrue(count > 0);

        count = getLoadedClassCount();
        assertTrue(count > 0);

        count = getUnloadedClassCount();
        assertTrue(count > -1);
    }

    @Test
    public void testFindLoadedClassesInClassPaths() {
        Set<Class<?>> allLoadedClasses = findLoadedClassesInClassPaths(classLoader, ClassPathUtils.getClassPaths());
        assertFalse(allLoadedClasses.isEmpty());
    }

    @Test
    public void testGetClassLoader() {
        Class<?> currentClass = getClass();
        ClassLoader classLoader = currentClass.getClassLoader();
        // Caller Class -> ClassLoaderUtilsTest.class -> classLoader
        assertSame(classLoader, getClassLoader(null));
        // ClassLoaderUtilsTest.class -> classLoader
        assertSame(classLoader, getClassLoader(currentClass));
        // String.class -> Bootstrap ClassLoader(null)
        assertSame(getDefaultClassLoader(), getClassLoader(String.class));
    }

    @Test
    public void testGetDefaultClassLoader() {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        assertEquals(classLoader, getDefaultClassLoader());

        currentThread.setContextClassLoader(null);
        assertEquals(ClassLoaderUtils.class.getClassLoader(), getDefaultClassLoader());

        currentThread.setContextClassLoader(getSystemClassLoader().getParent());

        // recovery
        currentThread.setContextClassLoader(classLoader);
    }

    @Test
    public void testGetCallerClassLoader() {
        ClassLoader classLoader = getCallerClassLoader();
        assertNotNull(classLoader);
    }

    @Test
    public void testRemoveClassPathURL() {
        assertFalse(removeClassPathURL(new TestClassLoader(), null));

        ClassLoader classLoader = getDefaultClassLoader();
        Set<URL> urls = findAllClassPathURLs(classLoader);
        for (URL url : urls) {
            String path = url.getPath();
            if (path.contains("jmh-generator-annprocess")) {
                assertTrue(removeClassPathURL(classLoader, url));
            }
        }
        Set<URL> urls2 = findAllClassPathURLs(classLoader);
        assertEquals(urls.size(), urls2.size());
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
