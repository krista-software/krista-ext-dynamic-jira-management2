package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Jira issue type (e.g. Bug, Task, Story) scoped to a project. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "IssueType", id = "localDomainEntity_7f3a9c10-1002-4e21-9a3f-5d2b61001002",
        primaryKey = "Id", supportStore = false,
        description = "An issue type (e.g. Bug, Task, Story) available within a Project; drives which field schema Discover Ticket Field Schema returns.")
public class IssueType {

    @Field.Text(name = "Id", required = false)
    public String id;

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field(name = "Description", type = "Paragraph", required = false)
    public String description;

    @Field.Boolean(name = "Is Subtask", required = false)
    public Boolean isSubtask;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Name", name);
        fields.put("Description", description);
        fields.put("Is Subtask", isSubtask);
        return fields;
    }

    public static IssueType fromJson(JsonObject json) {
        IssueType type = new IssueType();
        if (json == null) {
            return type;
        }
        type.id = JsonHelper.text(json, "id");
        type.name = JsonHelper.text(json, "name");
        type.description = JsonHelper.text(json, "description");
        type.isSubtask = JsonHelper.bool(json, "subtask");
        return type;
    }
}
