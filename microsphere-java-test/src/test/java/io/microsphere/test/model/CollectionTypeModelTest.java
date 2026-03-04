package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static io.microsphere.test.model.Color.BLUE;
import static io.microsphere.test.model.Color.RED;
import static io.microsphere.test.model.Color.YELLOW;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the CollectionTypeModel class
 */
class CollectionTypeModelTest {

    private CollectionTypeModel model;

    @BeforeEach
    void setUp() {
        model = new CollectionTypeModel();
    }

    @Test
    void testDefaultValues() {
        // Verify all collection fields are null by default
        assertNull(model.getStrings(), "strings collection should be null by default");
        assertNull(model.getColors(), "colors list should be null by default");
        assertNull(model.getPrimitiveTypeModels(), "primitiveTypeModels queue should be null by default");
        assertNull(model.getModels(), "models deque should be null by default");
        assertNull(model.getModelArrays(), "modelArrays set should be null by default");
    }

    @Test
    void testSetGetStrings() {
        Collection<String> testCollection = asList("hello", "world", "test");
        
        model.setStrings(testCollection);
        assertSame(testCollection, model.getStrings(), "Should return the same collection reference that was set");
        
        // Verify the contents match
        assertEquals(testCollection, model.getStrings(), "Collection contents should match the set value");
    }

    @Test
    void testSetGetColors() {
        List<Color> testList = asList(RED, BLUE, YELLOW);
        
        model.setColors(testList);
        assertSame(testList, model.getColors(), "Should return the same list reference that was set");
        
        // Verify the contents match
        assertEquals(testList, model.getColors(), "List contents should match the set value");
    }

    @Test
    void testSetGetPrimitiveTypeModels() {
        Queue<PrimitiveTypeModel> testQueue = new LinkedList<>();
        testQueue.add(new PrimitiveTypeModel());
        testQueue.add(new PrimitiveTypeModel());
        
        model.setPrimitiveTypeModels(testQueue);
        assertSame(testQueue, model.getPrimitiveTypeModels(), "Should return the same queue reference that was set");
        
        // Verify the contents match
        assertEquals(testQueue, model.getPrimitiveTypeModels(), "Queue contents should match the set value");
    }

    @Test
    void testSetGetModels() {
        Deque<Model> testDeque = new LinkedList<>();
        testDeque.add(new Model());
        testDeque.add(new Model());
        
        model.setModels(testDeque);
        assertSame(testDeque, model.getModels(), "Should return the same deque reference that was set");
        
        // Verify the contents match
        assertEquals(testDeque, model.getModels(), "Deque contents should match the set value");
    }

    @Test
    void testSetGetModelArrays() {
        Set<Model[]> testSet = new HashSet<>();
        testSet.add(new Model[]{new Model(), new Model()});
        testSet.add(new Model[]{new Model()});
        
        model.setModelArrays(testSet);
        assertSame(testSet, model.getModelArrays(), "Should return the same set reference that was set");
        
        // Verify the contents match
        assertEquals(testSet, model.getModelArrays(), "Set contents should match the set value");
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
    void testCollectionMutability() {
        List<Color> originalList = new ArrayList<>(asList(RED, BLUE));
        model.setColors(originalList);
        
        // Modify the original collection
        originalList.add(RED);
        
        // Check if the change is reflected in the getter result
        assertTrue(model.getColors().contains(RED),
            "Changes to the original collection should be reflected since collections are passed by reference");
    }
}
