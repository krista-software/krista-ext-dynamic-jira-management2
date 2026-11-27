package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.SearchTicketsFilter;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced, JQL-backed ticket search with combined filters, sorting, and pagination. "me" (Reporter/
 * Assignee) resolves to the authenticated Jira identity. Uses Jira's current token-paginated search
 * endpoint; the exact total result count is replaced by Jira's own approximate-count endpoint, since
 * the exact-total endpoint this replaced is deprecated and no longer reliably available.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class SearchArea {

    private static final Logger LOG = LoggerFactory.getLogger(SearchArea.class);

    private final JiraService service;

    @Inject
    public SearchArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0025-4a00-8000-000000000025",
            name = "Search Tickets",
            description = "Search tickets by any combination of project, issue key, status, reporter, assignee, priority, issue type, labels, components, resolution, text, and created/updated/due date ranges. Use Reporter or Assignee = \"me\" to resolve the authenticated user.",
            area = "Search",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Tickets", type = "[ Entity(Ticket) ]", required = false)
    @Field(name = "Approx Count", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "Next Page Token", required = false)
    @Field.Boolean(name = "Is Last", required = false)
    public ExtensionResponse searchTickets(
            @Field.Text(name = "Project Key", required = false, attributes = {}, options = {}) String projectKey,
            @Field.Text(name = "Issue Key", required = false, attributes = {}, options = {}) String issueKey,
            @Field.Text(name = "Status", required = false, attributes = {}, options = {}) String status,
            @Field.Text(name = "Reporter", required = false, attributes = {}, options = {}) String reporter,
            @Field.Text(name = "Assignee", required = false, attributes = {}, options = {}) String assignee,
            @Field.Text(name = "Priority", required = false, attributes = {}, options = {}) String priority,
            @Field.Text(name = "Issue Type", required = false, attributes = {}, options = {}) String issueType,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Components", type = "[ Text ]", required = false) List<String> components,
            @Field.Text(name = "Resolution", required = false, attributes = {}, options = {}) String resolution,
            @Field.Text(name = "Text Search", required = false, attributes = {}, options = {}) String textSearch,
            @Field.Date(name = "Created After", required = false, attributes = {}, options = {}) Long createdAfter,
            @Field.Date(name = "Created Before", required = false, attributes = {}, options = {}) Long createdBefore,
            @Field.Date(name = "Updated After", required = false, attributes = {}, options = {}) Long updatedAfter,
            @Field.Date(name = "Updated Before", required = false, attributes = {}, options = {}) Long updatedBefore,
            @Field.Date(name = "Due After", required = false, attributes = {}, options = {}) Long dueAfter,
            @Field.Date(name = "Due Before", required = false, attributes = {}, options = {}) Long dueBefore,
            @Field.Text(name = "Sort By", required = false, attributes = {}, options = {}) String sortBy,
            @Field.Boolean(name = "Sort Descending", required = false, attributes = {}, options = {}) Boolean sortDescending,
            @Field(name = "Max Results", type = "Number", required = false, attributes = {}, options = {}) Double maxResults,
            @Field.Text(name = "Next Page Token", required = false, attributes = {}, options = {}) String nextPageToken) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SearchTicketsFilter filter = new SearchTicketsFilter();
            filter.projectKey = projectKey;
            filter.issueKey = issueKey;
            filter.status = status;
            filter.reporterAccountId = reporter;
            filter.assigneeAccountId = assignee;
            filter.priority = priority;
            filter.issueType = issueType;
            filter.labels = labels;
            filter.components = components;
            filter.resolution = resolution;
            filter.textSearch = textSearch;
            filter.createdAfter = createdAfter;
            filter.createdBefore = createdBefore;
            filter.updatedAfter = updatedAfter;
            filter.updatedBefore = updatedBefore;
            filter.dueAfter = dueAfter;
            filter.dueBefore = dueBefore;
            filter.sortBy = sortBy;
            filter.sortDescending = sortDescending;
            filter.maxResults = maxResults == null ? null : maxResults.intValue();
            filter.nextPageToken = nextPageToken;

            var result = service.searchTickets(filter);
            out.put("Tickets", result.tickets());
            out.put("Approx Count", result.approxCount() == null ? null : result.approxCount().doubleValue());
            out.put("Next Page Token", result.nextPageToken());
            out.put("Is Last", result.isLast());
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Search Tickets failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }
}
