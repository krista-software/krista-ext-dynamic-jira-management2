package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.AdfUtil;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** A comment on a Jira ticket. References User via Author Display Name. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "Comment", id = "localDomainEntity_7f3a9c10-1009-4e21-9a3f-5d2b61001009",
        primaryKey = "Id", supportStore = false,
        description = "A comment on a Ticket. Author Display Name references User.Display Name.")
public class Comment {

    @Field.Text(name = "Id", required = false)
    public String id;

    @Field.Text(name = "Author Display Name", required = false)
    public String authorDisplayName;

    @ToString
    @Field(name = "Body", type = "Paragraph", required = false)
    public String body;

    @Field.Date(name = "Created", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long created;

    @Field.Date(name = "Updated", required = false, includeTimeOfDay = true,
            allowPast = true, allowToday = true, allowFuture = false)
    public Long updated;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Id", id);
        fields.put("Author Display Name", authorDisplayName);
        fields.put("Body", body);
        fields.put("Created", created);
        fields.put("Updated", updated);
        return fields;
    }

    public static Comment fromJson(JsonObject json) {
        Comment comment = new Comment();
        if (json == null) {
            return comment;
        }
        comment.id = JsonHelper.text(json, "id");
        JsonObject author = JsonHelper.object(json, "author");
        if (author != null) {
            comment.authorDisplayName = JsonHelper.text(author, "displayName");
        }
        if (json.has("body")) {
            comment.body = AdfUtil.adfToPlainText(json.get("body"));
        }
        comment.created = JiraDates.parse(JsonHelper.text(json, "created"));
        comment.updated = JiraDates.parse(JsonHelper.text(json, "updated"));
        return comment;
    }
}
