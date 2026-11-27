package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A workflow transition a ticket can currently make, plus any field it additionally requires. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "Transition", id = "localDomainEntity_7f3a9c10-1010-4e21-9a3f-5d2b61001010",
        primaryKey = "Id", supportStore = false,
        description = "An available workflow transition for a specific Ticket - the only legal way to change its status.")
public class Transition {

    @Field.Text(name = "Id", required = false)
    public String id;

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "To Status Name", required = false)
    public String toStatusName;

    @Field.Desc(name = "Required Fields", type = "[ { FieldId: Text, Name: Text } ]", required = false)
    public List<Map<String, Object>> requiredFields;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new java.util.LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        fields.put("To Status Name", toStatusName);
        fields.put("Required Fields", requiredFields);
        return fields;
    }

    public static Transition fromJson(JsonObject json) {
        Transition transition = new Transition();
        if (json == null) {
            return transition;
        }
        transition.id = JsonHelper.text(json, "id");
        transition.name = JsonHelper.text(json, "name");
        JsonObject to = JsonHelper.object(json, "to");
        if (to != null) {
            transition.toStatusName = JsonHelper.text(to, "name");
        }
        List<Map<String, Object>> required = new ArrayList<>();
        JsonObject fields = JsonHelper.object(json, "fields");
        if (fields != null) {
            for (String fieldId : fields.keySet()) {
                JsonObject fieldDef = JsonHelper.object(fields, fieldId);
                if (fieldDef != null && JsonHelper.bool(fieldDef, "required")) {
                    required.add(Map.of("FieldId", fieldId, "Name", JsonHelper.text(fieldDef, "name")));
                }
            }
        }
        transition.requiredFields = required;
        return transition;
    }
}
