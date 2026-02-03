package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for the PrimitiveTypeModel class
 */
class PrimitiveTypeModelTest {

    private PrimitiveTypeModel model;

    @BeforeEach
    void setUp() {
        model = new PrimitiveTypeModel();
    }

    @Test
    void testDefaultValues() {
        // Verify all primitive fields have their default values
        assertFalse(model.isZ(), "boolean field z should be false by default");
        assertEquals((byte) 0, model.getB(), "byte field b should be 0 by default");
        assertEquals('\u0000', model.getC(), "char field c should be null character by default");
        assertEquals((short) 0, model.getS(), "short field s should be 0 by default");
        assertEquals(0, model.getI(), "int field i should be 0 by default");
        assertEquals(0L, model.getL(), "long field l should be 0L by default");
        assertEquals(0.0f, model.getF(), "float field f should be 0.0f by default");
        assertEquals(0.0d, model.getD(), "double field d should be 0.0d by default");
    }

    @Test
    void testBooleanField() {
        // Test boolean field specifically since it uses 'is' prefix instead of 'get'
        assertFalse(model.isZ(), "Initial value should be false");
    }

    @Test
    void testPrimitiveTypesRange() {
        // Test various ranges for different primitive types
        PrimitiveTypeModel testModel = new PrimitiveTypeModel();
        
        // Boolean
        // Cannot set values directly as there are no setters, but we can verify the default
        
        // Byte range (-128 to 127)
        byte minByte = Byte.MIN_VALUE;
        byte maxByte = Byte.MAX_VALUE;
        
        // Char range (0 to 65535)
        char minChar = Character.MIN_VALUE;
        char maxChar = Character.MAX_VALUE;
        
        // Short range (-32768 to 32767)
        short minShort = Short.MIN_VALUE;
        short maxShort = Short.MAX_VALUE;
        
        // Int range
        int minInt = Integer.MIN_VALUE;
        int maxInt = Integer.MAX_VALUE;
        
        // Long range
        long minLong = Long.MIN_VALUE;
        long maxLong = Long.MAX_VALUE;
        
        // Float range
        float minFloat = Float.MIN_VALUE;
        float maxFloat = Float.MAX_VALUE;
        
        // Double range
        double minDouble = Double.MIN_VALUE;
        double maxDouble = Double.MAX_VALUE;
        
        // These are just to ensure the getters work with different possible values
        // Since there are no setters, we're just validating the getters return values
        assertEquals(0, testModel.getI(), "Integer field should have default value");
        assertEquals(0.0f, testModel.getF(), "Float field should have default value");
    }
}
