package io.microsphere.test.model;

import io.microsphere.annotation.ConfigurationProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the ConfigurationPropertyModel class
 */
class ConfigurationPropertyModelTest {

    private ConfigurationPropertyModel model;

    @BeforeEach
    void setUp() {
        model = new ConfigurationPropertyModel();
    }

    @Test
    void testDefaultValues() {
        // Verify all fields have their default values
        assertNull(model.getName(), "name should be null by default");
        assertNull(model.getType(), "type should be null by default");
        assertNull(model.getDefaultValue(), "defaultValue should be null by default");
        assertFalse(model.isRequired(), "required should be false by default");
        assertNull(model.getDescription(), "description should be null by default");
    }

    @Test
    void testSetNameAndGet() {
        String testName = "test.config.property";
        model.setName(testName);
        assertEquals(testName, model.getName(), "getName should return the set value");
    }

    @Test
    void testSetTypeAndGet() {
        Class<?> testType = String.class;
        model.setType(testType);
        assertEquals(testType, model.getType(), "getType should return the set value");
    }

    @Test
    void testSetDefaultValueAndGet() {
        String testDefaultValue = "default_value";
        model.setDefaultValue(testDefaultValue);
        assertEquals(testDefaultValue, model.getDefaultValue(), "getDefaultValue should return the set value");
    }

    @Test
    void testSetRequiredAndGet() {
        model.setRequired(true);
        assertTrue(model.isRequired(), "isRequired should return true after setting to true");

        model.setRequired(false);
        assertFalse(model.isRequired(), "isRequired should return false after setting to false");
    }

    @Test
    void testSetDescriptionAndGet() {
        String testDescription = "This is a test configuration property";
        model.setDescription(testDescription);
        assertEquals(testDescription, model.getDescription(), "getDescription should return the set value");
    }

    @Test
    void testConfigurationPropertyAnnotations() {
        // Test that the ConfigurationProperty annotations are present on the fields
        java.lang.reflect.Field[] fields = ConfigurationPropertyModel.class.getDeclaredFields();
        
        boolean hasNameAnnotation = false;
        boolean hasTypeAnnotation = false;
        boolean hasDefaultValueAnnotation = false;
        boolean hasRequiredAnnotation = false;
        boolean hasDescriptionAnnotation = false;
        
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(ConfigurationProperty.class)) {
                ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
                
                switch (field.getName()) {
                    case "name":
                        if ("microsphere.annotation.processor.model.name".equals(annotation.name())) {
                            hasNameAnnotation = true;
                        }
                        break;
                    case "type":
                        if ("microsphere.annotation.processor.model.type".equals(annotation.name())) {
                            hasTypeAnnotation = true;
                        }
                        break;
                    case "defaultValue":
                        if ("microsphere.annotation.processor.model.default-value".equals(annotation.name())) {
                            hasDefaultValueAnnotation = true;
                        }
                        break;
                    case "required":
                        if ("microsphere.annotation.processor.model.required".equals(annotation.name())) {
                            hasRequiredAnnotation = true;
                        }
                        break;
                    case "description":
                        if ("microsphere.annotation.processor.model.description".equals(annotation.name())) {
                            hasDescriptionAnnotation = true;
                        }
                        break;
                }
            }
        }
        
        assertTrue(hasNameAnnotation, "name field should have ConfigurationProperty annotation with correct name");
        assertTrue(hasTypeAnnotation, "type field should have ConfigurationProperty annotation with correct name");
        assertTrue(hasDefaultValueAnnotation, "defaultValue field should have ConfigurationProperty annotation with correct name");
        assertTrue(hasRequiredAnnotation, "required field should have ConfigurationProperty annotation with correct name");
        assertTrue(hasDescriptionAnnotation, "description field should have ConfigurationProperty annotation with correct name");
    }

    @Test
    void testSetAllProperties() {
        // Test setting all properties and verifying they are returned correctly
        model.setName("full.test.name");
        model.setType(Integer.class);
        model.setDefaultValue("42");
        model.setRequired(true);
        model.setDescription("Complete test configuration");

        assertEquals("full.test.name", model.getName(), "Name should match set value");
        assertEquals(Integer.class, model.getType(), "Type should match set value");
        assertEquals("42", model.getDefaultValue(), "DefaultValue should match set value");
        assertTrue(model.isRequired(), "Required should match set value");
        assertEquals("Complete test configuration", model.getDescription(), "Description should match set value");
    }

    @Test
    void testNullValueHandling() {
        // Test setting fields to null
        model.setName(null);
        model.setType(null);
        model.setDefaultValue(null);
        model.setDescription(null);

        assertNull(model.getName(), "Name should be null");
        assertNull(model.getType(), "Type should be null");
        assertNull(model.getDefaultValue(), "DefaultValue should be null");
        assertNull(model.getDescription(), "Description should be null");
    }
}
