package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import static io.microsphere.test.model.Color.BLUE;
import static io.microsphere.test.model.Color.RED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the MapTypeModel class
 */
class MapTypeModelTest {

    private MapTypeModel model;

    @BeforeEach
    void setUp() {
        model = new MapTypeModel();
    }

    @Test
    void testDefaultValues() {
        // Verify all map fields are null by default
        assertNull(model.getStrings(), "strings map should be null by default");
        assertNull(model.getColors(), "colors sorted map should be null by default");
        assertNull(model.getPrimitiveTypeModels(), "primitiveTypeModels navigable map should be null by default");
        assertNull(model.getModels(), "models hash map should be null by default");
        assertNull(model.getModelArrays(), "modelArrays tree map should be null by default");
    }

    @Test
    void testSetGetStrings() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");

        model.setStrings(testMap);
        assertSame(testMap, model.getStrings(), "Should return the same map reference that was set");

        // Verify the contents match
        assertEquals(testMap, model.getStrings(), "Map contents should match the set value");
    }

    @Test
    void testSetGetColors() {
        SortedMap<String, Color> testMap = new TreeMap<>();
        testMap.put("red_key", RED);
        testMap.put("blue_key", BLUE);

        model.setColors(testMap);
        assertSame(testMap, model.getColors(), "Should return the same map reference that was set");

        // Verify the contents match
        assertEquals(testMap, model.getColors(), "SortedMap contents should match the set value");
    }

    @Test
    void testSetGetPrimitiveTypeModels() {
        NavigableMap<Color, PrimitiveTypeModel> testMap = new TreeMap<>();
        testMap.put(RED, new PrimitiveTypeModel());
        testMap.put(BLUE, new PrimitiveTypeModel());

        model.setPrimitiveTypeModels(testMap);
        assertSame(testMap, model.getPrimitiveTypeModels(), "Should return the same map reference that was set");

        // Verify the contents match
        assertEquals(testMap, model.getPrimitiveTypeModels(), "NavigableMap contents should match the set value");
    }

    @Test
    void testSetGetModels() {
        HashMap<String, Model> testMap = new HashMap<>();
        testMap.put("model1", new Model());
        testMap.put("model2", new Model());

        model.setModels(testMap);
        assertSame(testMap, model.getModels(), "Should return the same map reference that was set");

        // Verify the contents match
        assertEquals(testMap, model.getModels(), "HashMap contents should match the set value");
    }

    @Test
    void testSetGetModelArrays() {
        TreeMap<PrimitiveTypeModel, Model[]> testMap = new TreeMap<>((a, b) -> 1); // Using custom comparator to avoid issues with PrimitiveTypeModel not implementing Comparable
        testMap.put(new PrimitiveTypeModel(), new Model[]{new Model()});
        testMap.put(new PrimitiveTypeModel(), new Model[]{new Model(), new Model()});

        model.setModelArrays(testMap);
        assertSame(testMap, model.getModelArrays(), "Should return the same map reference that was set");

        // Verify the contents match
        assertEquals(testMap, model.getModelArrays(), "TreeMap contents should match the set value");
    }

    @Test
    void testSetNullValues() {
        // Test setting each field to null
        model.setStrings(null);
        assertNull(model.getStrings(), "strings should be null after setting to null");

        model.setColors(null);
        assertNull(model.getColors(), "colors should be null after setting to null");

        model.setPrimitiveTypeModels(null);
        assertNull(model.getPrimitiveTypeModels(), "primitiveTypeModels should be null after setting to null");

        model.setModels(null);
        assertNull(model.getModels(), "models should be null after setting to null");

        model.setModelArrays(null);
        assertNull(model.getModelArrays(), "modelArrays should be null after setting to null");
    }

    @Test
    void testMapMutability() {
        Map<String, String> originalMap = new HashMap<>();
        originalMap.put("initial", "value");
        model.setStrings(originalMap);

        // Modify the original map
        originalMap.put("added", "new_value");

        // Check if the change is reflected in the getter result
        assertTrue(model.getStrings().containsKey("added"), 
            "Changes to the original map should be reflected since maps are passed by reference");
    }
}
