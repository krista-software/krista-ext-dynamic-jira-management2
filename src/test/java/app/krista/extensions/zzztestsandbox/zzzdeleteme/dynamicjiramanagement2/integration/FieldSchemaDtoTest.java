package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldSchemaDtoTest {

    @Test
    void anyValueIsAllowedWhenNoAllowedValuesListed() {
        FieldSchemaDto dto = new FieldSchemaDto();
        dto.allowedValues = List.of();
        assertTrue(dto.isAllowedValue("anything"));
    }

    @Test
    void matchesAllowedValueByIdCaseInsensitively() {
        FieldSchemaDto dto = new FieldSchemaDto();
        dto.allowedValues = List.of(Map.of("id", "3", "value", "High"));
        assertTrue(dto.isAllowedValue("3"));
    }

    @Test
    void matchesAllowedValueByNameCaseInsensitively() {
        FieldSchemaDto dto = new FieldSchemaDto();
        dto.allowedValues = List.of(Map.of("id", "3", "value", "High"));
        assertTrue(dto.isAllowedValue("high"));
    }

    @Test
    void rejectsValueNotInAllowedList() {
        FieldSchemaDto dto = new FieldSchemaDto();
        dto.allowedValues = List.of(Map.of("id", "3", "value", "High"));
        assertFalse(dto.isAllowedValue("Medium"));
    }
}
