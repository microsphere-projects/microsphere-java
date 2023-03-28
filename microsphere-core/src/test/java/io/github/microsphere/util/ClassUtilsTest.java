/**
 *
 */
package io.github.microsphere.util;

import io.github.microsphere.AbstractTestCase;
import io.github.microsphere.reflect.ReflectionUtils;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import static io.github.microsphere.util.ClassUtils.getClassName;
import static org.junit.Assert.assertEquals;

/**
 * {@link ClassUtils} {@link TestCase}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassUtilsTest
 * @since 1.0.0
 */
public class ClassUtilsTest extends AbstractTestCase {

    @Test
    public void testGetClassNamesInClassPath() {
        Set<String> classPaths = ClassPathUtils.getClassPaths();
        for (String classPath : classPaths) {
            Set<String> classNames = ClassUtils.getClassNamesInClassPath(classPath, true);
            Assert.assertNotNull(classNames);
        }
    }

    @Test
    public void testGetClassNamesInPackage() {
        Set<String> packageNames = ClassUtils.getAllPackageNamesInClassPaths();
        for (String packageName : packageNames) {
            Set<String> classNames = ClassUtils.getClassNamesInPackage(packageName);
            Assert.assertFalse(classNames.isEmpty());
            Assert.assertNotNull(classNames);
            echo(packageName);
            echo("\t" + classNames);
        }
    }


    @Test
    public void testGetAllPackageNamesInClassPaths() {
        Set<String> packageNames = ClassUtils.getAllPackageNamesInClassPaths();
        Assert.assertNotNull(packageNames);
        echo(packageNames);
    }

    @Test
    public void testFindClassPath() {
        String classPath = ClassUtils.findClassPath(ReflectionUtils.class);
        Assert.assertNotNull(classPath);

        classPath = ClassUtils.findClassPath(String.class);
        Assert.assertNotNull(classPath);
    }

    @Test
    public void testGetAllClassNamesMapInClassPath() {
        Map<String, Set<String>> allClassNamesMapInClassPath = ClassUtils.getClassPathToClassNamesMap();
        Assert.assertFalse(allClassNamesMapInClassPath.isEmpty());
    }

    @Test
    public void testGetAllClassNamesInClassPath() {
        Set<String> allClassNames = ClassUtils.getAllClassNamesInClassPaths();
        Assert.assertFalse(allClassNames.isEmpty());
    }

    @Test
    public void testGetCodeSourceLocation() throws IOException {
        URL codeSourceLocation = null;
        Assert.assertNull(codeSourceLocation);

        codeSourceLocation = ClassUtils.getCodeSourceLocation(ClassUtilsTest.class);
        echo("codeSourceLocation : " + codeSourceLocation);
        Assert.assertNotNull(codeSourceLocation);

        codeSourceLocation = ClassUtils.getCodeSourceLocation(String.class);
        echo("codeSourceLocation : " + codeSourceLocation);
        Assert.assertNotNull(codeSourceLocation);


    }

    @Test
    public void testClassName() {
        // There are five kinds of classes (or interfaces):
        // a) Top level classes
        assertEquals("java.lang.String", getClassName(String.class));

        // b) Nested classes (static member classes)
        assertEquals("java.util.Map$Entry", getClassName(Map.Entry.class));

        // c) Inner classes (non-static member classes)
        assertEquals("java.lang.Thread$State", getClassName(Thread.State.class));

        // d) Local classes (named classes declared within a method)
        class LocalClass {
        }
        assertEquals("io.github.microsphere.util.ClassUtilsTest$1LocalClass", getClassName(LocalClass.class));

        // e) Anonymous classes
        Serializable instance = new Serializable() {
        };
        assertEquals("io.github.microsphere.util.ClassUtilsTest$1", getClassName(instance.getClass()));
    }

}
