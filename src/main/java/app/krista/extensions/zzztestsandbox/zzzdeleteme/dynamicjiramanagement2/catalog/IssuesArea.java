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
import java.util.List;
import java.util.Map;

/** Core ticket CRUD: create, retrieve, and update tickets using the live, discovered field schema. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class IssuesArea {

    private static final Logger LOG = LoggerFactory.getLogger(IssuesArea.class);

    private final JiraService service;

    @Inject
    public IssuesArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0014-4a00-8000-000000000014",
            name = "Create Ticket",
            description = "Create a Jira ticket. Fields are validated against the live, discovered schema for the chosen project and issue type before any API call is made; project-specific/custom fields go in Additional Fields. Reporter is always the authenticated user.",
            area = "Issues",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Ticket", type = "Entity(Ticket)", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse createTicket(
            @Field.Text(name = "Project Key", required = true, attributes = {}, options = {}) String projectKey,
            @Field.Text(name = "Issue Type Id", required = true, attributes = {}, options = {}) String issueTypeId,
            @Field.Text(name = "Summary", required = true, attributes = {}, options = {}) String summary,
            @Field(name = "Description", type = "Paragraph", required = false, attributes = {}, options = {}) String description,
            @Field.Text(name = "Priority", required = false, attributes = {}, options = {}) String priority,
            @Field.Text(name = "Assignee", required = false, attributes = {}, options = {}) String assignee,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Components", type = "[ Text ]", required = false) List<String> components,
            @Field.Date(name = "Due Date", required = false, attributes = {}, options = {}) Long dueDate,
            @Field.Desc(name = "Additional Fields", type = "[ { FieldId: Text, Value: Text } ]", required = false) List<Map<String, Object>> additionalFields) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Ticket ticket = service.createTicket(projectKey, issueTypeId, summary, description, priority, assignee,
                    labels, components, dueDate, additionalFields);
            out.put("Ticket", ticket);
            out.put("Success", true);
            out.put("Message", "Created " + ticket.key + ".");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Create Ticket failed for project {}", projectKey, e);
            out.put("Success", false);
            out.put("Message", e.getMessage());
            return new ExtensionResponseBuilder().success(out).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0015-4a00-8000-000000000015",
            name = "Get Ticket Details",
            description = "Retrieve a ticket's full details by issue key or id.",
            area = "Issues",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Ticket", type = "Entity(Ticket)", required = false)
    public ExtensionResponse getTicketDetails(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Ticket", service.getTicket(issueKey));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Ticket Details failed for {}", issueKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0016-4a00-8000-000000000016",
            name = "Update Ticket Fields",
            description = "Update only the fields explicitly supplied on a ticket - fields left blank here are never overwritten. Validated against the ticket's live editable-field schema.",
            area = "Issues",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Ticket", type = "Entity(Ticket)", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse updateTicketFields(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey,
            @Field.Text(name = "Summary", required = false, attributes = {}, options = {}) String summary,
            @Field(name = "Description", type = "Paragraph", required = false, attributes = {}, options = {}) String description,
            @Field.Text(name = "Priority", required = false, attributes = {}, options = {}) String priority,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Components", type = "[ Text ]", required = false) List<String> components,
            @Field.Date(name = "Due Date", required = false, attributes = {}, options = {}) Long dueDate,
            @Field.Desc(name = "Additional Fields", type = "[ { FieldId: Text, Value: Text } ]", required = false) List<Map<String, Object>> additionalFields) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Ticket ticket = service.updateTicket(issueKey, summary, description, priority, labels, components, dueDate, additionalFields);
            out.put("Ticket", ticket);
            out.put("Success", true);
            out.put("Message", "Updated " + ticket.key + ".");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Update Ticket Fields failed for {}", issueKey, e);
            out.put("Success", false);
            out.put("Message", e.getMessage());
            return new ExtensionResponseBuilder().success(out).build();
        }
    }
}
