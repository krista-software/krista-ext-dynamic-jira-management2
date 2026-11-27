package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Jira resolution (e.g. Fixed, Won't Fix, Duplicate) that can be set when a ticket is closed. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "Resolution", id = "localDomainEntity_7f3a9c10-1006-4e21-9a3f-5d2b61001006",
        primaryKey = "Id", supportStore = false,
        description = "A resolution value (e.g. Fixed, Won't Fix, Duplicate) usable as a Search Tickets filter or a workflow-transition field.")
public class Resolution {

    @Field.Text(name = "Id", required = false)
    public String id;

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        return fields;
    }

    public static Resolution fromJson(JsonObject json) {
        Resolution resolution = new Resolution();
        if (json == null) {
            return resolution;
        }
        resolution.id = JsonHelper.text(json, "id");
        resolution.name = JsonHelper.text(json, "name");
        return resolution;
    }
}
