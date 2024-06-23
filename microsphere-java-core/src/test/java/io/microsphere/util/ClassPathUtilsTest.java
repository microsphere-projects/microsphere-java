/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.Set;

import static io.microsphere.util.ClassLoaderUtils.isLoadedClass;
import static io.microsphere.util.ClassPathUtils.getBootstrapClassPaths;
import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static io.microsphere.util.ClassPathUtils.getRuntimeClassLocation;
import static io.microsphere.util.ClassUtils.getAllClassNamesInClassPaths;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ClassPathUtils} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassPathUtilsTest
 * @since 1.0.0
 */
public class ClassPathUtilsTest extends AbstractTestCase {

    @Test
    public void testGetBootstrapClassPaths() {
        Set<String> bootstrapClassPaths = getBootstrapClassPaths();
        assertNotNull(bootstrapClassPaths);
        RuntimeMXBean runtimeMXBean = getRuntimeMXBean();
        assertEquals(runtimeMXBean.isBootClassPathSupported(), !bootstrapClassPaths.isEmpty());
        info(bootstrapClassPaths);
    }

    @Test
    public void testGetClassPaths() {
        Set<String> classPaths = getClassPaths();
        assertNotNull(classPaths);
        assertFalse(classPaths.isEmpty());
        info(classPaths);
    }

    @Test
    public void testGetRuntimeClassLocation() {
        URL location = null;
        location = getRuntimeClassLocation(String.class);
        assertNotNull(location);
        info(location);

        location = getRuntimeClassLocation(getClass());
        assertNotNull(location);
        info(location);

        //Primitive type
        location = getRuntimeClassLocation(int.class);
        assertNull(location);

        //Array type
        location = getRuntimeClassLocation(int[].class);
        assertNull(location);


        Set<String> classNames = getAllClassNamesInClassPaths();
        for (String className : classNames) {
            if (!isLoadedClass(classLoader, className)) {
                location = getRuntimeClassLocation(className);
                assertNull(location);
            }
        }

    }
}
