package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** One historical field-change event on a ticket. References User via Author Display Name. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "ChangelogEntry", id = "localDomainEntity_7f3a9c10-1011-4e21-9a3f-5d2b61001011",
        primaryKey = "Id", supportStore = false,
        description = "One historical field-change event on a Ticket. Author Display Name references User.Display Name.")
public class ChangelogEntry {

    @Field.Text(name = "Id", required = false)
    public String id;

    @ToString
    @Field.Text(name = "Author Display Name", required = false)
    public String authorDisplayName;

    @Field.Date(name = "Created", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long created;

    @Field.Desc(name = "Changes", type = "[ { Field: Text, FromValue: Text, ToValue: Text } ]", required = false)
    public List<Map<String, Object>> changes;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new java.util.LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Author Display Name", authorDisplayName);
        fields.put("Created", created);
        fields.put("Changes", changes);
        return fields;
    }

    public static ChangelogEntry fromJson(JsonObject json) {
        ChangelogEntry entry = new ChangelogEntry();
        if (json == null) {
            return entry;
        }
        entry.id = JsonHelper.text(json, "id");
        JsonObject author = JsonHelper.object(json, "author");
        if (author != null) {
            entry.authorDisplayName = JsonHelper.text(author, "displayName");
        }
        entry.created = JiraDates.parse(JsonHelper.text(json, "created"));
        List<Map<String, Object>> changes = new ArrayList<>();
        if (json.has("items") && json.get("items").isJsonArray()) {
            JsonArray items = json.getAsJsonArray("items");
            for (var item : items) {
                JsonObject itemObj = item.getAsJsonObject();
                changes.add(Map.of(
                        "Field", String.valueOf(JsonHelper.text(itemObj, "field")),
                        "FromValue", String.valueOf(JsonHelper.text(itemObj, "fromString")),
                        "ToValue", String.valueOf(JsonHelper.text(itemObj, "toString"))));
            }
        }
        entry.changes = changes;
        return entry;
    }
}
