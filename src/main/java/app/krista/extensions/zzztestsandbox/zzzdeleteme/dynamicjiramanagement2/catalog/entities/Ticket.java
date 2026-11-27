package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.Searchable;
import app.krista.extension.impl.anno.ToString;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.AdfUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Jira ticket (issue). The central domain object of this extension - every create/update/search/
 * workflow/assignment/comment operation reads or writes a Ticket. Searchable (supportStore = true,
 * backed by TicketStore / JQL) so other Krista workflows can resolve Tickets generically, in addition
 * to the dedicated Search Tickets catalog request. Project Key references Project.Key; Reporter/
 * Assignee Display Name reference User.Display Name; Status/Priority/Issue Type/Resolution Name
 * reference the respective reference-data entities by name.
 */
@Searchable
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "Ticket", id = "localDomainEntity_7f3a9c10-1008-4e21-9a3f-5d2b61001008",
        primaryKey = "Key", supportStore = true,
        description = "A Jira ticket. Project Key references Project.Key; Reporter/Assignee Display Name reference User.Display Name; Status/Priority/Issue Type/Resolution Name reference their reference-data entities by name.")
public class Ticket {

    @Searchable
    @Field.Text(name = "Id", required = false)
    public String id;

    @Searchable
    @ToString
    @Field.Text(name = "Key", required = false)
    public String key;

    @Searchable
    @Field.Text(name = "Summary", required = false)
    public String summary;

    @Field(name = "Description", type = "Paragraph", required = false)
    public String description;

    @Searchable
    @Field.Text(name = "Status Name", required = false)
    public String statusName;

    @Searchable
    @Field.Text(name = "Priority Name", required = false)
    public String priorityName;

    @Searchable
    @Field.Text(name = "Issue Type Name", required = false)
    public String issueTypeName;

    @Searchable
    @Field.Text(name = "Project Key", required = false)
    public String projectKey;

    @Searchable
    @Field.Text(name = "Reporter Display Name", required = false)
    public String reporterDisplayName;

    @Field.Text(name = "Reporter Account Id", required = false)
    public String reporterAccountId;

    @Searchable
    @Field.Text(name = "Assignee Display Name", required = false)
    public String assigneeDisplayName;

    @Field.Text(name = "Assignee Account Id", required = false)
    public String assigneeAccountId;

    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public List<String> labels;

    @Field.Desc(name = "Components", type = "[ { Id: Text, Name: Text } ]", required = false)
    public List<Map<String, Object>> components;

    @Searchable
    @Field.Date(name = "Due Date", required = false, includeTimeOfDay = false,
            allowPast = true, allowToday = true, allowFuture = true)
    public Long dueDate;

    @Searchable
    @Field.Date(name = "Created", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long created;

    @Searchable
    @Field.Date(name = "Updated", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long updated;

    @Searchable
    @Field.Text(name = "Resolution Name", required = false)
    public String resolutionName;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Key", key);
        fields.put("Summary", summary);
        fields.put("Description", description);
        fields.put("Status Name", statusName);
        fields.put("Priority Name", priorityName);
        fields.put("Issue Type Name", issueTypeName);
        fields.put("Project Key", projectKey);
        fields.put("Reporter Display Name", reporterDisplayName);
        fields.put("Reporter Account Id", reporterAccountId);
        fields.put("Assignee Display Name", assigneeDisplayName);
        fields.put("Assignee Account Id", assigneeAccountId);
        fields.put("Labels", labels);
        fields.put("Components", components);
        fields.put("Due Date", dueDate);
        fields.put("Created", created);
        fields.put("Updated", updated);
        fields.put("Resolution Name", resolutionName);
        return fields;
    }

    public static Ticket fromJson(JsonObject issueJson) {
        Ticket ticket = new Ticket();
        if (issueJson == null) {
            return ticket;
        }
        ticket.id = JsonHelper.text(issueJson, "id");
        ticket.key = JsonHelper.text(issueJson, "key");
        JsonObject fields = JsonHelper.object(issueJson, "fields");
        if (fields == null) {
            return ticket;
        }
        ticket.summary = JsonHelper.text(fields, "summary");
        if (fields.has("description")) {
            ticket.description = AdfUtil.adfToPlainText(fields.get("description"));
        }
        JsonObject status = JsonHelper.object(fields, "status");
        if (status != null) {
            ticket.statusName = JsonHelper.text(status, "name");
        }
        JsonObject priority = JsonHelper.object(fields, "priority");
        if (priority != null) {
            ticket.priorityName = JsonHelper.text(priority, "name");
        }
        JsonObject issueType = JsonHelper.object(fields, "issuetype");
        if (issueType != null) {
            ticket.issueTypeName = JsonHelper.text(issueType, "name");
        }
        JsonObject project = JsonHelper.object(fields, "project");
        if (project != null) {
            ticket.projectKey = JsonHelper.text(project, "key");
        }
        JsonObject reporter = JsonHelper.object(fields, "reporter");
        if (reporter != null) {
            ticket.reporterDisplayName = JsonHelper.text(reporter, "displayName");
            ticket.reporterAccountId = JsonHelper.text(reporter, "accountId");
        }
        JsonObject assignee = JsonHelper.object(fields, "assignee");
        if (assignee != null) {
            ticket.assigneeDisplayName = JsonHelper.text(assignee, "displayName");
            ticket.assigneeAccountId = JsonHelper.text(assignee, "accountId");
        }
        List<String> labels = new ArrayList<>();
        if (fields.has("labels") && fields.get("labels").isJsonArray()) {
            for (var label : fields.getAsJsonArray("labels")) {
                labels.add(label.getAsString());
            }
        }
        ticket.labels = labels;
        List<Map<String, Object>> components = new ArrayList<>();
        if (fields.has("components") && fields.get("components").isJsonArray()) {
            JsonArray componentsArray = fields.getAsJsonArray("components");
            for (var component : componentsArray) {
                JsonObject componentObj = component.getAsJsonObject();
                components.add(Map.of("Id", String.valueOf(JsonHelper.text(componentObj, "id")),
                        "Name", String.valueOf(JsonHelper.text(componentObj, "name"))));
            }
        }
        ticket.components = components;
        ticket.dueDate = JiraDates.parse(JsonHelper.text(fields, "duedate"));
        ticket.created = JiraDates.parse(JsonHelper.text(fields, "created"));
        ticket.updated = JiraDates.parse(JsonHelper.text(fields, "updated"));
        JsonObject resolution = JsonHelper.object(fields, "resolution");
        if (resolution != null) {
            ticket.resolutionName = JsonHelper.text(resolution, "name");
        }
        return ticket;
    }
}
