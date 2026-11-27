package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dynamic project schema discovery - the highest-priority capability of this extension. Lets a
 * conversation/agent select a project and issue type, then see exactly which fields Jira requires,
 * their data types, allowed values, and defaults, before ever calling Create/Update Ticket.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class IssueTypesSchemaArea {

    private static final Logger LOG = LoggerFactory.getLogger(IssueTypesSchemaArea.class);

    private final JiraService service;

    @Inject
    public IssueTypesSchemaArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0010-4a00-8000-000000000010",
            name = "List Issue Types For Project",
            description = "Retrieve the issue types available within a project (e.g. Bug, Task, Story).",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Issue Types", type = "[ Entity(IssueType) ]", required = false)
    public ExtensionResponse listIssueTypesForProject(
            @Field.Text(name = "Project Key", required = true, attributes = {}, options = {}) String projectKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Issue Types", service.listIssueTypesForProject(projectKey));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("List Issue Types For Project failed for {}", projectKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0011-4a00-8000-000000000011",
            name = "Discover Ticket Field Schema",
            description = "Dynamically discover the field rules (mandatory/optional, data type, allowed values, defaults, custom fields) for creating a ticket of this issue type in this project. Cached by Project Key + Issue Type with a TTL; use Force Refresh to bypass a stale cache.",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Fields", type = "[ Entity(FieldSchema) ]", required = false)
    public ExtensionResponse discoverTicketFieldSchema(
            @Field.Text(name = "Project Key", required = true, attributes = {}, options = {}) String projectKey,
            @Field.Text(name = "Issue Type Id", required = true, attributes = {}, options = {}) String issueTypeId,
            @Field.Boolean(name = "Force Refresh", required = false, attributes = {}, options = {}) Boolean forceRefresh) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Fields", service.discoverCreateFieldSchema(projectKey, issueTypeId, Boolean.TRUE.equals(forceRefresh)));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Discover Ticket Field Schema failed for {}/{}", projectKey, issueTypeId, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0012-4a00-8000-000000000012",
            name = "Get Editable Field Schema For Ticket",
            description = "Dynamically discover which fields on an existing ticket are editable right now, and their rules - drives Update Ticket Fields validation.",
            area = "Issue Types & Schema",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Fields", type = "[ Entity(FieldSchema) ]", required = false)
    public ExtensionResponse getEditableFieldSchemaForTicket(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Fields", service.getEditFieldSchema(issueKey));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Editable Field Schema For Ticket failed for {}", issueKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }
}
