package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.test.model.Color.BLUE;
import static io.microsphere.test.model.Color.RED;
import static io.microsphere.test.model.Color.YELLOW;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the ArrayTypeModel class
 */
class ArrayTypeModelTest {

    private ArrayTypeModel model;

    @BeforeEach
    void setUp() {
        model = new ArrayTypeModel();
    }

    @Test
    void testDefaultValues() {
        // Verify all array fields are null by default
        assertNull(model.getIntegers(), "integers array should be null by default");
        assertNull(model.getStrings(), "strings array should be null by default");
        assertNull(model.getPrimitiveTypeModels(), "primitiveTypeModels array should be null by default");
        assertNull(model.getModels(), "models array should be null by default");
        assertNull(model.getColors(), "colors array should be null by default");
    }

    @Test
    void testSetGetIntegers() {
        int[] testArray = {1, 2, 3, 4, 5};

        model.setIntegers(testArray);
        assertSame(testArray, model.getIntegers(), "Should return the same array reference that was set");

        // Verify the contents match
        assertArrayEquals(testArray, model.getIntegers(), "Array contents should match the set value");
    }

    @Test
    void testSetGetStrings() {
        String[] testArray = {"hello", "world", "test"};

        model.setStrings(testArray);
        assertSame(testArray, model.getStrings(), "Should return the same array reference that was set");

        // Verify the contents match
        assertArrayEquals(testArray, model.getStrings(), "Array contents should match the set value");
    }

    @Test
    void testSetGetPrimitiveTypeModels() {
        PrimitiveTypeModel[] testArray = {new PrimitiveTypeModel(), new PrimitiveTypeModel()};

        model.setPrimitiveTypeModels(testArray);
        assertSame(testArray, model.getPrimitiveTypeModels(), "Should return the same array reference that was set");

        // Verify the contents match
        assertArrayEquals(testArray, model.getPrimitiveTypeModels(), "Array contents should match the set value");
    }

    @Test
    void testSetGetModels() {
        Model[] testArray = {new Model(), new Model()};

        model.setModels(testArray);
        assertSame(testArray, model.getModels(), "Should return the same array reference that was set");

        // Verify the contents match
        assertArrayEquals(testArray, model.getModels(), "Array contents should match the set value");
    }

    @Test
    void testSetGetColors() {
        Color[] testArray = {RED, BLUE, YELLOW};

        model.setColors(testArray);
        assertSame(testArray, model.getColors(), "Should return the same array reference that was set");

        // Verify the contents match
        assertArrayEquals(testArray, model.getColors(), "Array contents should match the set value");
    }

    @Test
    void testSetNullValues() {
        // Test setting each field to null
        model.setIntegers(null);
        assertNull(model.getIntegers(), "integers should be null after setting to null");

        model.setStrings(null);
        assertNull(model.getStrings(), "strings should be null after setting to null");

        model.setPrimitiveTypeModels(null);
        assertNull(model.getPrimitiveTypeModels(), "primitiveTypeModels should be null after setting to null");

        model.setModels(null);
        assertNull(model.getModels(), "models should be null after setting to null");

        model.setColors(null);
        assertNull(model.getColors(), "colors should be null after setting to null");
    }

    @Test
    void testArrayMutability() {
        int[] originalArray = {1, 2, 3};
        model.setIntegers(originalArray);

        // Modify the original array
        originalArray[0] = 999;

        // Check if the change is reflected in the getter result
        assertEquals(999, model.getIntegers()[0],
                "Changes to the original array should be reflected since arrays are passed by reference");
    }
}