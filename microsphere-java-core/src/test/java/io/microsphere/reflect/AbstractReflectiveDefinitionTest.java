package io.microsphere.reflect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.lang.DeprecationTest.DEPRECATION;
import static io.microsphere.lang.DeprecationTest.SINCE;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.util.Version.ofVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link ReflectiveDefinition} Test
 *
 * @param <D> the sub-type of {@link ReflectiveDefinition}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ReflectiveDefinition
 * @since 1.0.0
 */
public abstract class AbstractReflectiveDefinitionTest<D extends ReflectiveDefinition> {

    private final List<Object>[] headConstructorArgumentsArray = new List[]{
            ofList(SINCE, getClassName()),
            ofList(ofVersion(SINCE), getClassName()),
            ofList(SINCE, DEPRECATION, getClassName()),
            ofList(ofVersion(SINCE), DEPRECATION, getClassName())
    };

    protected List<D> definitions;

    @BeforeEach
    void setUp() throws Throwable {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Class<?> defintionClass = (Class) parameterizedType.getActualTypeArguments()[0];
        int length = headConstructorArgumentsArray.length;
        definitions = newArrayList(length);
        for (int i = 0; i < length; i++) {
            List<Object> headArguments = headConstructorArgumentsArray[i];
            List<Object> tailArguments = getTailConstructorArguments();
            Object[] arguments = new Object[headArguments.size() + tailArguments.size()];
            Class[] parameterTypes = new Class[headArguments.size() + tailArguments.size()];
            int index = 0;
            for (Object argument : headArguments) {
                arguments[index] = argument;
                parameterTypes[index] = argument.getClass();
                index++;
            }
            for (Object argument : tailArguments) {
                arguments[index] = argument;
                parameterTypes[index] = argument.getClass();
                index++;
            }
            Constructor constructor = findConstructor(defintionClass, parameterTypes);
            definitions.add((D) constructor.newInstance(arguments));
        }
    }

    protected String getClassName() {
        return getClass().getName();
    }

    protected abstract List<Object> getTailConstructorArguments();

    @Test
    public void testGetSince() {
        for (D definition : definitions) {
            assertNotNull(definition.getSince());
        }
    }

    @Test
    public void testGetDeprecation() {
        assertNull(definitions.get(0).getDeprecation());
        assertNull(definitions.get(1).getDeprecation());
        assertNotNull(definitions.get(2).getDeprecation());
        assertNotNull(definitions.get(3).getDeprecation());
    }

    @Test
    public void testGetClassName() {
        for (D definition : definitions) {
            assertNotNull(definition.getClassName());
        }
    }

    @Test
    public void testGetResolvedClass() {
        for (D definition : definitions) {
            assertNotNull(definition.getResolvedClass());
        }
    }

    @Test
    public void testIsDeprecated() {
        assertFalse(definitions.get(0).isDeprecated());
        assertFalse(definitions.get(1).isDeprecated());
        assertTrue(definitions.get(2).isDeprecated());
        assertTrue(definitions.get(3).isDeprecated());
    }

    @Test
    public void testIsPresent() {
        for (D definition : definitions) {
            assertNotNull(definition.isPresent());
        }
    }

    @Test
    public void testTestEquals() {
        assertEquals(definitions.get(0), definitions.get(1));
        assertEquals(definitions.get(2), definitions.get(3));
        assertNotEquals(definitions.get(0), definitions.get(2));
    }

    @Test
    public void testTestHashCode() {
        assertEquals(definitions.get(0).hashCode(), definitions.get(1).hashCode());
        assertEquals(definitions.get(2).hashCode(), definitions.get(3).hashCode());
        assertNotEquals(definitions.get(0).hashCode(), definitions.get(2).hashCode());
    }

    @Test
    public void testTestToString() {
        assertEquals(definitions.get(0).toString(), definitions.get(1).toString());
        assertEquals(definitions.get(2).toString(), definitions.get(3).toString());
        assertNotEquals(definitions.get(0).toString(), definitions.get(2).toString());
    }
}