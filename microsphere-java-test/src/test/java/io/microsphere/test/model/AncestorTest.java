package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Ancestor class
 */
class AncestorTest {

    private Ancestor ancestor;

    @BeforeEach
    void setUp() {
        ancestor = new Ancestor();
    }

    @Test
    void testDefaultConstructor() {
        // Verify that the default value of z is false
        assertFalse(ancestor.isZ(), "Default value of z should be false");
    }

    @Test
    void testSetZTrue() {
        // Set z to true and verify
        ancestor.setZ(true);
        assertTrue(ancestor.isZ(), "Value of z should be true after setting it to true");
    }

    @Test
    void testSetZFalse() {
        // Explicitly set z to false and verify
        ancestor.setZ(false);
        assertFalse(ancestor.isZ(), "Value of z should be false after setting it to false");
    }

    @Test
    void testSetZToggle() {
        // Test toggling the value from default false to true and back to false
        assertFalse(ancestor.isZ(), "Initial value should be false");

        ancestor.setZ(true);
        assertTrue(ancestor.isZ(), "Value should be true after first toggle");

        ancestor.setZ(false);
        assertFalse(ancestor.isZ(), "Value should be false after second toggle");
    }

    @Test
    void testMultipleInstanceIndependence() {
        // Create two instances and verify they maintain independent state
        Ancestor ancestor1 = new Ancestor();
        Ancestor ancestor2 = new Ancestor();

        // Initially both should have false
        assertFalse(ancestor1.isZ(), "First instance should initially be false");
        assertFalse(ancestor2.isZ(), "Second instance should initially be false");

        // Modify only the first instance
        ancestor1.setZ(true);

        // Verify that only the first instance changed
        assertTrue(ancestor1.isZ(), "First instance should be true after modification");
        assertFalse(ancestor2.isZ(), "Second instance should remain false");
    }

    @Test
    void testSerializableImplementation() {
        // Test that the class can be instantiated and used as a Serializable object
        // This test verifies basic functionality without actual serialization
        ancestor.setZ(true);
        assertTrue(ancestor.isZ(), "Should properly handle boolean value when used as Serializable");
        
        ancestor.setZ(false);
        assertFalse(ancestor.isZ(), "Should properly handle boolean value when used as Serializable");
    }
}
