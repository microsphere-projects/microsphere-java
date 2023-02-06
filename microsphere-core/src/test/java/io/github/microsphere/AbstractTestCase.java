/**
 *
 */
package io.github.microsphere;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Abstract {@link TestCase}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see AbstractTestCase
 * @since 1.0.0
 */
@Ignore
public abstract class AbstractTestCase {

    protected final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void echo(Object object, Object... others) {
        echo(object);
        for (Object o : others) {
            echo(o);
        }
    }

    public void echo(Object object) {
        logger.info(String.valueOf(object));
    }

    public void echo(String object, Object... args) {
        logger.info(object, args);
    }

    public void echo(Iterable<Object> iterable) {
        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            echo(iterator.next());
        }
    }
}
