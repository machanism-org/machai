package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParamDescriptorAdditionalTest {
    @Test
    void gettersExposeMetadataAndNormalizeSentinels() {
        ParamDescriptor descriptor = new ParamDescriptor("name", "string", true, Param.NULL, Param.NOT_DEFINED);
        assertEquals("name", descriptor.getName());
        assertEquals("string", descriptor.getType());
        assertTrue(descriptor.isRequired());
        assertNull(descriptor.getDescription());
        assertNull(descriptor.getDefaultValue());
    }

    @Test
    void defaultValueCanBeUpdatedAndNonSentinelDescriptionIsRetained() {
        ParamDescriptor descriptor = new ParamDescriptor("n", "t", false, "description", "old");
        descriptor.setDefaultValue(12);
        assertFalse(descriptor.isRequired());
        assertEquals("description", descriptor.getDescription());
        assertEquals(12, descriptor.getDefaultValue());
        descriptor.setDefaultValue(Param.NULL);
        assertNull(descriptor.getDefaultValue());
    }
}
