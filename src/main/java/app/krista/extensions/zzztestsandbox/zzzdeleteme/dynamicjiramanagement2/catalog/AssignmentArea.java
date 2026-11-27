package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Ticket;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Finding candidate assignees, and assigning/reassigning/unassigning tickets, with permission handling. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class AssignmentArea {

    private static final Logger LOG = LoggerFactory.getLogger(AssignmentArea.class);

    private final JiraService service;

    @Inject
    public AssignmentArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0022-4a00-8000-000000000022",
            name = "Search Users",
            description = "Search Jira users, preferring assignment-eligible users when a project is given.",
            area = "Assignment",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Users", type = "[ Entity(User) ]", required = false)
    public ExtensionResponse searchUsers(
            @Field.Text(name = "Search Text", required = false, attributes = {}, options = {}) String searchText,
            @Field.Text(name = "Project Key", required = false, attributes = {}, options = {}) String projectKey,
            @Field(name = "Max Results", type = "Number", required = false, attributes = {}, options = {}) Double maxResults) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Users", service.searchUsers(searchText, projectKey, maxResults == null ? null : maxResults.intValue()));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Search Users failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0023-4a00-8000-000000000023",
            name = "Assign Or Reassign Ticket",
            description = "Assign a ticket to the current authenticated user, or to a user matched by name/email, with permission validation.",
            area = "Assignment",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Ticket", type = "Entity(Ticket)", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse assignOrReassignTicket(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey,
            @Field.Boolean(name = "Assign To Current User", required = false, attributes = {}, options = {}) Boolean assignToCurrentUser,
            @Field.Text(name = "Assignee Search Text", required = false, attributes = {}, options = {}) String assigneeSearchText) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Ticket ticket = service.assignTicket(issueKey, Boolean.TRUE.equals(assignToCurrentUser), assigneeSearchText);
            out.put("Ticket", ticket);
            out.put("Success", true);
            out.put("Message", issueKey + " is now assigned to " + ticket.assigneeDisplayName + ".");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Assign Or Reassign Ticket failed for {}", issueKey, e);
            out.put("Success", false);
            out.put("Message", e.getMessage());
            return new ExtensionResponseBuilder().success(out).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0024-4a00-8000-000000000024",
            name = "Unassign Ticket",
            description = "Remove the assignee from a ticket.",
            area = "Assignment",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse unassignTicket(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            service.unassignTicket(issueKey);
            out.put("Success", true);
            out.put("Message", issueKey + " is now unassigned.");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Unassign Ticket failed for {}", issueKey, e);
            out.put("Success", false);
            out.put("Message", e.getMessage());
            return new ExtensionResponseBuilder().success(out).build();
        }
    }
}
