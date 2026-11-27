package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import java.util.List;

/**
 * Plain parameter holder for the Search Tickets catalog request's filters, passed from the Area
 * method into {@link JqlBuilder#fromExplicitFilters} and the service layer. "me" is already resolved
 * to an accountId by the caller before this object is built.
 */
public class SearchTicketsFilter {
    public String projectKey;
    public String issueKey;
    public String status;
    public String reporterAccountId;
    public String assigneeAccountId;
    public String priority;
    public String issueType;
    public List<String> labels;
    public List<String> components;
    public String resolution;
    public String textSearch;
    public Long createdAfter;
    public Long createdBefore;
    public Long updatedAfter;
    public Long updatedBefore;
    public Long dueAfter;
    public Long dueBefore;
    public String sortBy;
    public Boolean sortDescending;
    public Integer maxResults;
    public String nextPageToken;
}
