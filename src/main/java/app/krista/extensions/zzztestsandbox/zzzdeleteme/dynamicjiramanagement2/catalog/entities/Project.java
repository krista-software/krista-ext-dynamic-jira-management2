package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Jira project. References User via Lead Display Name (the project lead's display name).
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "Project", id = "localDomainEntity_7f3a9c10-1001-4e21-9a3f-5d2b61001001",
        primaryKey = "Id", supportStore = false,
        description = "A Jira project that tickets belong to. Lead Display Name references User.Display Name.")
public class Project {

    @Field.Text(name = "Id", required = false)
    public String id;

    @Field.Text(name = "Key", required = false)
    public String key;

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "Project Type Key", required = false)
    public String projectTypeKey;

    @Field.Text(name = "Style", required = false)
    public String style;

    @Field.Text(name = "Lead Display Name", required = false)
    public String leadDisplayName;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Key", key);
        fields.put("Name", name);
        fields.put("Project Type Key", projectTypeKey);
        fields.put("Style", style);
        fields.put("Lead Display Name", leadDisplayName);
        return fields;
    }

    public static Project fromJson(JsonObject json) {
        Project project = new Project();
        if (json == null) {
            return project;
        }
        project.id = JsonHelper.text(json, "id");
        project.key = JsonHelper.text(json, "key");
        project.name = JsonHelper.text(json, "name");
        project.projectTypeKey = JsonHelper.text(json, "projectTypeKey");
        project.style = JsonHelper.text(json, "style");
        JsonObject lead = JsonHelper.object(json, "lead");
        if (lead != null) {
            project.leadDisplayName = JsonHelper.text(lead, "displayName");
        }
        return project;
    }
}
