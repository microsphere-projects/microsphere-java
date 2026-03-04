package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the StringArrayList class
 */
class StringArrayListTest {

    private StringArrayList stringArrayList;

    @BeforeEach
    void setUp() {
        stringArrayList = new StringArrayList();
    }

    @Test
    void testDefaultConstructor() {
        // Verify that the StringArrayList can be instantiated and is empty initially
        assertNotNull(stringArrayList, "StringArrayList should be instantiated successfully");
        assertTrue(stringArrayList.isEmpty(), "StringArrayList should be empty by default");
        assertEquals(0, stringArrayList.size(), "StringArrayList size should be 0 by default");
    }

    @Test
    void testAddAndGetSize() {
        // Test adding elements and checking size
        stringArrayList.add("element1");
        assertEquals(1, stringArrayList.size(), "Size should be 1 after adding one element");

        stringArrayList.add("element2");
        assertEquals(2, stringArrayList.size(), "Size should be 2 after adding two elements");
    }

    @Test
    void testAddAndGetElements() {
        // Test adding elements and retrieving them
        String element1 = "Hello";
        String element2 = "World";
        String element3 = "Test";

        stringArrayList.add(element1);
        stringArrayList.add(element2);
        stringArrayList.add(element3);

        assertEquals(element1, stringArrayList.get(0), "Element at index 0 should match added value");
        assertEquals(element2, stringArrayList.get(1), "Element at index 1 should match added value");
        assertEquals(element3, stringArrayList.get(2), "Element at index 2 should match added value");
    }

    @Test
    void testAddAll() {
        // Test adding multiple elements at once
        String[] elements = {"item1", "item2", "item3"};
        
        boolean result = stringArrayList.addAll(Arrays.asList(elements));
        assertTrue(result, "addAll should return true when elements are added");
        
        assertEquals(3, stringArrayList.size(), "Size should match number of added elements");
        assertEquals("item1", stringArrayList.get(0), "First element should match");
        assertEquals("item2", stringArrayList.get(1), "Second element should match");
        assertEquals("item3", stringArrayList.get(2), "Third element should match");
    }

    @Test
    void testRemoveElement() {
        // Test removing elements
        stringArrayList.add("element1");
        stringArrayList.add("element2");
        
        boolean removed = stringArrayList.remove("element1");
        assertTrue(removed, "Should return true when element is successfully removed");
        assertEquals(1, stringArrayList.size(), "Size should decrease after removal");
        assertEquals("element2", stringArrayList.get(0), "Remaining element should still be accessible");
    }

    @Test
    void testClear() {
        // Test clearing all elements
        stringArrayList.add("element1");
        stringArrayList.add("element2");
        stringArrayList.add("element3");
        
        assertEquals(3, stringArrayList.size(), "Size should be 3 before clear");
        
        stringArrayList.clear();
        
        assertTrue(stringArrayList.isEmpty(), "List should be empty after clear");
        assertEquals(0, stringArrayList.size(), "Size should be 0 after clear");
    }

    @Test
    void testContains() {
        // Test contains functionality
        String element = "test_element";
        stringArrayList.add(element);
        
        assertTrue(stringArrayList.contains(element), "List should contain the added element");
        assertFalse(stringArrayList.contains("non_existent"), "List should not contain non-existent element");
    }

    @Test
    void testIterator() {
        // Test iterator functionality
        String[] elements = {"a", "b", "c"};
        stringArrayList.addAll(Arrays.asList(elements));
        
        Iterator<String> iterator = stringArrayList.iterator();
        int index = 0;
        
        while (iterator.hasNext() && index < elements.length) {
            String nextElement = iterator.next();
            assertEquals(elements[index], nextElement, "Iterator should return elements in order");
            index++;
        }
        
        assertFalse(iterator.hasNext(), "Iterator should be exhausted after processing all elements");
    }

    @Test
    void testInheritanceFromArrayList() {
        // Verify that StringArrayList properly extends ArrayList<String>
        assertTrue(stringArrayList instanceof ArrayList, "StringArrayList should extend ArrayList");
        assertTrue(stringArrayList instanceof java.util.List, "StringArrayList should implement List interface");
        assertTrue(stringArrayList instanceof java.util.Collection, "StringArrayList should implement Collection interface");
    }
}
