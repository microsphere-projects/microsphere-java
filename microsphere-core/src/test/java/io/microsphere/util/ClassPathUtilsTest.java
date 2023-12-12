/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Set;

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
        Set<String> bootstrapClassPaths = ClassPathUtils.getBootstrapClassPaths();
        assertNotNull(bootstrapClassPaths);
        assertFalse(bootstrapClassPaths.isEmpty());
        info(bootstrapClassPaths);
    }


    @Test
    public void testGetClassPaths() {
        Set<String> classPaths = ClassPathUtils.getClassPaths();
        assertNotNull(classPaths);
        assertFalse(classPaths.isEmpty());
        info(classPaths);
    }

    @Test
    public void getRuntimeClassLocation() {
        URL location = null;
        location = ClassPathUtils.getRuntimeClassLocation(String.class);
        assertNotNull(location);
        info(location);

        location = ClassPathUtils.getRuntimeClassLocation(getClass());
        assertNotNull(location);
        info(location);

        //Primitive type
        location = ClassPathUtils.getRuntimeClassLocation(int.class);
        assertNull(location);

        //Array type
        location = ClassPathUtils.getRuntimeClassLocation(int[].class);
        assertNull(location);


        Set<String> classNames = ClassUtils.getAllClassNamesInClassPaths();
        for (String className : classNames) {
            if (!ClassLoaderUtils.isLoadedClass(classLoader, className)) {
                location = ClassPathUtils.getRuntimeClassLocation(className);
                assertNull(location);
            }
        }

    }
}
