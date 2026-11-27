package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Jira user, identified by Atlassian accountId (Jira Cloud no longer uses usernames). */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
@Entity(name = "User", id = "localDomainEntity_7f3a9c10-1007-4e21-9a3f-5d2b61001007",
        primaryKey = "Account Id", supportStore = false,
        description = "A Jira user. Ticket.Reporter Display Name and Ticket.Assignee Display Name reference User.Display Name.")
public class User {

    @Field.Text(name = "Account Id", required = false)
    public String accountId;

    @ToString
    @Field.Text(name = "Display Name", required = false)
    public String displayName;

    @Field.Text(name = "Email", required = false)
    public String email;

    @Field.Boolean(name = "Active", required = false)
    public Boolean active;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Account Id", accountId);
        fields.put("Display Name", displayName);
        fields.put("Email", email);
        fields.put("Active", active);
        return fields;
    }

    public static User fromJson(JsonObject json) {
        User user = new User();
        if (json == null) {
            return user;
        }
        user.accountId = JsonHelper.text(json, "accountId");
        user.displayName = JsonHelper.text(json, "displayName");
        user.email = JsonHelper.text(json, "emailAddress");
        user.active = JsonHelper.bool(json, "active");
        return user;
    }
}
