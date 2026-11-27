package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import java.util.List;
import java.util.Map;

/**
 * Internal, backend-agnostic representation of one discovered field's rules (mandatory/optional,
 * data type, allowed values, default), parsed from Jira's createmeta/editmeta responses. This is the
 * shape schema discovery, validation, and request building all share.
 */
public class FieldSchemaDto {
    public String fieldId;
    public String name;
    public boolean required;
    public String dataType;
    public boolean custom;
    public List<Map<String, String>> allowedValues; // each: {"id": ..., "value": ...}
    public String defaultValueDisplay;
    public boolean hasDefaultValue;

    public boolean isAllowedValue(String candidate) {
        if (allowedValues == null || allowedValues.isEmpty() || candidate == null) {
            return true; // no fixed value list - anything goes (free text/number/date/etc.)
        }
        for (Map<String, String> option : allowedValues) {
            if (candidate.equalsIgnoreCase(option.get("id")) || candidate.equalsIgnoreCase(option.get("value"))) {
                return true;
            }
        }
        return false;
    }
}
