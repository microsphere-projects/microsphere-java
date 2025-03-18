package io.microsphere.util;

import io.microsphere.performance.AbstractPerformanceTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.ClassLoaderUtils.findLoadedClassesInClassPath;

/**
 * {@link ClassLoaderUtils} Performance Test
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see ClassLoaderUtilsPerformanceTest
 * @since 1.0.0
 */
@Disabled
public class ClassLoaderUtilsPerformanceTest extends AbstractPerformanceTest {

    @Test
    public void testFind() {
        super.execute(() -> findLoadedClassesInClassPath(classLoader));
    }
}
