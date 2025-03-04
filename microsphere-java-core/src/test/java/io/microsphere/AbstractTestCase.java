/**
 *
 */
package io.microsphere;

import io.microsphere.lang.function.ThrowableAction;
import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static io.microsphere.collection.QueueUtils.emptyDeque;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static io.microsphere.collection.QueueUtils.singletonDeque;
import static io.microsphere.collection.QueueUtils.singletonQueue;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see AbstractTestCase
 * @since 1.0.0
 */
@Disabled
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

    public static final File tempDir = new File(JAVA_IO_TMPDIR);

    protected final ClassLoader classLoader = getDefaultClassLoader();

    protected final Logger logger = getLogger(getClass());

    public void log(Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace(String.valueOf(object));
        }
    }

    public void log(String object, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(object, args);
        }
    }

    protected <T extends Throwable> void assertThrowable(ThrowableAction action, Class<T> throwableType) {
        assertThrowable(action, throwable -> {
            assertEquals(throwableType, throwable.getClass());
        });
    }

    protected void assertThrowable(ThrowableAction action, Consumer<Throwable> failureHandler) {
        Throwable failure = null;
        try {
            action.execute();
        } catch (Throwable t) {
            failure = t;
        }
        assertNotNull(failure);
        failureHandler.accept(failure);
    }

    protected File makeRandomTempDirectory() {
        File tempDir = createTempFile(createRandomFileName());
        assertTrue(tempDir.mkdir());
        return tempDir;
    }

    protected File createRandomTempFile() {
        return createTempFile(createRandomFileName());
    }

    protected File createRandomFile(File parentDir) {
        return new File(parentDir, createRandomFileName());
    }

    protected String createRandomFileName() {
        return UUID.randomUUID().toString();
    }

    protected File createTempFile(String path) {
        return new File(tempDir, path);
    }
}
