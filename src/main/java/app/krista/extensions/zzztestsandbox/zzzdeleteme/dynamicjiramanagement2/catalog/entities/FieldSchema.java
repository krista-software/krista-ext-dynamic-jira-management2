package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.FieldSchemaDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One row of a dynamically discovered field schema (the result of Discover Ticket Field Schema / Get
 * Editable Field Schema For Ticket) - field id, data type, whether it is mandatory, its allowed
 * values and default, and whether it is a custom field. This is the mechanism that lets the same
 * Create/Update Ticket request adapt to any project and issue type without a hardcoded form.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "FieldSchema", id = "localDomainEntity_7f3a9c10-1003-4e21-9a3f-5d2b61001003",
        primaryKey = "Field Id", supportStore = false,
        description = "One field's live validation rules for a given project + issue type (or, from the edit variant, an existing ticket) - mandatory/optional, data type, allowed values, and default.")
public class FieldSchema {

    @Field.Text(name = "Field Id", required = false)
    public String fieldId;

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Boolean(name = "Required", required = false)
    public Boolean required;

    @Field.Text(name = "Data Type", required = false)
    public String dataType;

    @Field.Boolean(name = "Is Custom Field", required = false)
    public Boolean isCustomField;

    @Field.Desc(name = "Allowed Values", type = "[ { Id: Text, Value: Text } ]", required = false)
    public List<Map<String, Object>> allowedValues;

    @Field.Text(name = "Default Value", required = false)
    public String defaultValue;

    @Field.Boolean(name = "Has Default Value", required = false)
    public Boolean hasDefaultValue;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Field Id", fieldId);
        fields.put("Name", name);
        fields.put("Required", required);
        fields.put("Data Type", dataType);
        fields.put("Is Custom Field", isCustomField);
        fields.put("Allowed Values", allowedValues);
        fields.put("Default Value", defaultValue);
        fields.put("Has Default Value", hasDefaultValue);
        return fields;
    }

    public static FieldSchema fromDto(FieldSchemaDto dto) {
        FieldSchema schema = new FieldSchema();
        if (dto == null) {
            return schema;
        }
        schema.fieldId = dto.fieldId;
        schema.name = dto.name;
        schema.required = dto.required;
        schema.dataType = dto.dataType;
        schema.isCustomField = dto.custom;
        schema.defaultValue = dto.defaultValueDisplay;
        schema.hasDefaultValue = dto.hasDefaultValue;
        if (dto.allowedValues != null) {
            schema.allowedValues = dto.allowedValues.stream()
                    .<Map<String, Object>>map(v -> Map.of("Id", String.valueOf(v.getOrDefault("id", "")),
                            "Value", String.valueOf(v.getOrDefault("value", ""))))
                    .toList();
        }
        return schema;
    }
}
