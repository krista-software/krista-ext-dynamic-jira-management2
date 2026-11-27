package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Jira workflow status (e.g. To Do, In Progress, Done), with its status category. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "Status", id = "localDomainEntity_7f3a9c10-1005-4e21-9a3f-5d2b61001005",
        primaryKey = "Id", supportStore = false,
        description = "A workflow status a ticket can be in (e.g. To Do, In Progress, Done).")
public class Status {

    @Field.Text(name = "Id", required = false)
    public String id;

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "Category Key", required = false)
    public String categoryKey;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        fields.put("Category Key", categoryKey);
        return fields;
    }

    public static Status fromJson(JsonObject json) {
        Status status = new Status();
        if (json == null) {
            return status;
        }
        status.id = JsonHelper.text(json, "id");
        status.name = JsonHelper.text(json, "name");
        JsonObject category = JsonHelper.object(json, "statusCategory");
        if (category != null) {
            status.categoryKey = JsonHelper.text(category, "key");
        }
        return status;
    }
}
