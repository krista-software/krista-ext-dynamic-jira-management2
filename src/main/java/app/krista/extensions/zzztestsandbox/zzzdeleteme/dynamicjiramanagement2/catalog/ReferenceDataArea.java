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
import java.util.List;
import java.util.Map;

/** Jira-instance-wide reference data: priorities, statuses, resolutions, and known labels. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class ReferenceDataArea {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceDataArea.class);

    private final JiraService service;

    @Inject
    public ReferenceDataArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0006-4a00-8000-000000000006",
            name = "List Priorities",
            description = "Retrieve the priority levels configured on this Jira instance.",
            area = "Reference Data",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Priorities", type = "[ Entity(Priority) ]", required = false)
    public ExtensionResponse listPriorities() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Priorities", service.listPriorities());
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("List Priorities failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0007-4a00-8000-000000000007",
            name = "List Statuses",
            description = "Retrieve workflow statuses - scoped to a project (and optionally issue type) if given, otherwise every status on the instance.",
            area = "Reference Data",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Statuses", type = "[ Entity(Status) ]", required = false)
    public ExtensionResponse listStatuses(
            @Field.Text(name = "Project Key", required = false, attributes = {}, options = {}) String projectKey,
            @Field.Text(name = "Issue Type Id", required = false, attributes = {}, options = {}) String issueTypeId) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Statuses", service.listStatuses(projectKey, issueTypeId));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("List Statuses failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0008-4a00-8000-000000000008",
            name = "List Resolutions",
            description = "Retrieve the resolution values configured on this Jira instance, for Search Tickets filters and workflow transitions.",
            area = "Reference Data",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Resolutions", type = "[ Entity(Resolution) ]", required = false)
    public ExtensionResponse listResolutions() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Resolutions", service.listResolutions());
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("List Resolutions failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0009-4a00-8000-000000000009",
            name = "List Known Labels",
            description = "Retrieve labels already in use on this Jira instance, for auto-population/autocomplete on Create and Update Ticket.",
            area = "Reference Data",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public ExtensionResponse listKnownLabels(
            @Field.Text(name = "Search Text", required = false, attributes = {}, options = {}) String searchText) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            List<String> labels = service.listLabels(searchText);
            out.put("Labels", labels);
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("List Known Labels failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }
}
