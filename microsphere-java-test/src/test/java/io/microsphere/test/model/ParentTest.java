package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Parent class
 */
class ParentTest {

    private Parent parent;

    @BeforeEach
    void setUp() {
        parent = new Parent();
    }

    @Test
    void testDefaultValues() {
        // Verify all primitive fields have their default values
        assertEquals((byte) 0, parent.getB(), "byte field b should be 0 by default");
        assertEquals((short) 0, parent.getS(), "short field s should be 0 by default");
        assertEquals(0, parent.getI(), "int field i should be 0 by default");
        assertEquals(0L, parent.getL(), "long field l should be 0L by default");
    }

    @Test
    void testSetGetByte() {
        byte testValue = (byte) 42;
        parent.setB(testValue);
        assertEquals(testValue, parent.getB(), "getB should return the set value");
    }

    @Test
    void testSetGetShort() {
        short testValue = (short) 1000;
        parent.setS(testValue);
        assertEquals(testValue, parent.getS(), "getS should return the set value");
    }

    @Test
    void testSetGetInt() {
        int testValue = 123456;
        parent.setI(testValue);
        assertEquals(testValue, parent.getI(), "getI should return the set value");
    }

    @Test
    void testSetGetLong() {
        long testValue = 9876543210L;
        parent.setL(testValue);
        assertEquals(testValue, parent.getL(), "getL should return the set value");
    }

    @Test
    void testInheritanceFromAncestor() {
        // Verify that the parent inherits from Ancestor class
        assertTrue(parent instanceof Ancestor, "Parent should extend Ancestor class");
        
        // Test inherited functionality
        assertFalse(parent.isZ(), "Should inherit default z value of false from Ancestor");
        
        parent.setZ(true);
        assertTrue(parent.isZ(), "Should be able to modify inherited z field");
    }

    @Test
    void testMultipleValueChanges() {
        // Test changing values multiple times
        parent.setB((byte) 1);
        assertEquals((byte) 1, parent.getB(), "b should be 1");
        
        parent.setB((byte) 2);
        assertEquals((byte) 2, parent.getB(), "b should be 2 after second assignment");
        
        parent.setI(100);
        assertEquals(100, parent.getI(), "i should be 100");
        
        parent.setI(200);
        assertEquals(200, parent.getI(), "i should be 200 after second assignment");
    }
}
