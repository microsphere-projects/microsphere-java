package io.microsphere.performance;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Disabled;

/**
 * {@link AbstractPerformanceTest}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractPerformanceTest
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractPerformanceTest extends AbstractTestCase {

    protected <T> void execute(PerformanceAction<T> action) {
        long startTime = System.currentTimeMillis();
        T returnValue = action.execute();
        long costTime = System.currentTimeMillis() - startTime;
        log("execution {} return {} costs {} ms", action, returnValue, costTime);
    }
}
