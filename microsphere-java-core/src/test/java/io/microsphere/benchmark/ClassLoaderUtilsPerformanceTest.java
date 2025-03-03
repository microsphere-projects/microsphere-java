package io.microsphere.benchmark;

import io.microsphere.performance.AbstractPerformanceTest;
import io.microsphere.util.ClassLoaderUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.ClassLoaderUtils.findLoadedClassesInClassPath;

/**
 * {@link ClassLoaderUtils} Performance Test
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
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
