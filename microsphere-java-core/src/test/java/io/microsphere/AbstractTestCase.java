/**
 *
 */
package io.microsphere;

import io.microsphere.lang.function.ThrowableAction;
import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static io.microsphere.collection.QueueUtils.emptyDeque;
import static io.microsphere.collection.QueueUtils.emptyQueue;
import static io.microsphere.collection.QueueUtils.singletonDeque;
import static io.microsphere.collection.QueueUtils.singletonQueue;
import static io.microsphere.collection.SetUtils.newHashSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
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
 * @see AbstractTestCase
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractTestCase {

    public static final String TEST_ELEMENT = "test";

    public static final String TEST_NULL_STRING = null;

    public static final Object[] TEST_NULL_OBJECT_ARRAY = null;

    public static final String[] TEST_NULL_STRING_ARRAY = null;

    public static final Enumeration TEST_NULL_ENUMERATION = null;

    public static final Iterator TEST_NULL_ITERATOR = null;

    public static final Iterable TEST_NULL_ITERABLE = null;

    public static final Collection TEST_NULL_COLLECTION = null;

    public static final List TEST_NULL_LIST = null;

    public static final Set TEST_NULL_SET = null;

    public static final Queue TEST_NULL_QUEUE = null;

    public static final Deque TEST_NULL_DEQUE = null;

    public static final Collection TEST_EMPTY_COLLECTION = emptySet();

    public static final List TEST_EMPTY_LIST = emptyList();

    public static final Set TEST_EMPTY_SET = emptySet();

    public static final Queue TEST_EMPTY_QUEUE = emptyQueue();

    public static final Deque TEST_EMPTY_DEQUE = emptyDeque();

    public static final List TEST_SINGLETON_LIST = singletonList(TEST_ELEMENT);

    public static final Set TEST_SINGLETON_SET = singleton(TEST_ELEMENT);

    public static final Queue TEST_SINGLETON_QUEUE = singletonQueue(TEST_ELEMENT);

    public static final Deque TEST_SINGLETON_DEQUE = singletonDeque(TEST_ELEMENT);

    public static final File TEST_TEMP_DIR = new File(JAVA_IO_TMPDIR);

    public static final ClassLoader TEST_CLASS_LOADER = getClassLoader(AbstractTestCase.class);

    protected final ClassLoader classLoader = getClassLoader(getClass());

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

    protected File createRandomTempDirectory() {
        File tempDir = newTempFile(buildRandomFileName());
        assertTrue(tempDir.mkdir());
        return tempDir;
    }

    protected File createRandomDirectory(File parentDir) {
        File tempDir = newRandomFile(parentDir);
        assertTrue(tempDir.mkdir());
        return tempDir;
    }

    protected File createRandomTempFile() throws IOException {
        File randomTempFile = newRandomTempFile();
        assertTrue(randomTempFile.createNewFile());
        return randomTempFile;
    }

    protected File createRandomFile(File parentDir) throws IOException {
        File randomFile = newRandomFile(parentDir);
        assertTrue(randomFile.createNewFile());
        return randomFile;
    }

    protected File newRandomTempFile() {
        return newTempFile(buildRandomFileName());
    }

    protected File newRandomFile(File parentDir) {
        return new File(parentDir, buildRandomFileName());
    }

    protected String buildRandomFileName() {
        return UUID.randomUUID().toString();
    }

    protected File newTempFile(String path) {
        return new File(TEST_TEMP_DIR, path);
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

    protected void assertValues(List<?> values, Object... expectedValues) {
        assertValues(values, expectedValues.length, expectedValues);
    }

    protected void assertValues(List<?> values, int expectedSize, Object... expectedTypes) {
        assertEquals(expectedSize, values.size());
        assertEquals(newHashSet(expectedTypes), newHashSet(values));
    }

    protected void assertType(Type expect, Type actual) {
        assertEquals(asClass(expect), asClass(actual));
    }
}
