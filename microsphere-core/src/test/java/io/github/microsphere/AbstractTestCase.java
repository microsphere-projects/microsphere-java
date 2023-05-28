/**
 *
 */
package io.github.microsphere;

import io.github.microsphere.util.ClassLoaderUtils;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static io.github.microsphere.collection.QueueUtils.*;
import static java.util.Collections.*;

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

    public static final String TEST_ELEMENT = "test";

    public static final Collection<?> NULL_COLLECTION = null;

    public static final List<?> NULL_LIST = null;

    public static final Set<?> NULL_SET = null;

    public static final Queue<?> NULL_QUEUE = null;

    public static final Deque<?> NULL_DEQUE = null;

    public static final Collection<?> EMPTY_COLLECTION = emptySet();

    public static final List<?> EMPTY_LIST = emptyList();

    public static final Set<?> EMPTY_SET = emptySet();

    public static final Queue<?> EMPTY_QUEUE = emptyQueue();

    public static final Deque<?> EMPTY_DEQUE = emptyDeque();

    public static final List<?> SINGLETON_LIST = singletonList(TEST_ELEMENT);

    public static final Set<?> SINGLETON_SET = singleton(TEST_ELEMENT);

    public static final Queue<?> SINGLETON_QUEUE = singletonQueue(TEST_ELEMENT);

    public static final Deque<?> SINGLETON_DEQUE = singletonDeque(TEST_ELEMENT);


    protected final ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void info(Object object, Object... others) {
        info(object);
        for (Object o : others) {
            info(o);
        }
    }

    public void info(Object object) {
        logger.info(String.valueOf(object));
    }

    public void info(String object, Object... args) {
        logger.info(object, args);
    }

    public void info(Iterable<Object> iterable) {
        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            info(iterator.next());
        }
    }
}
