package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import app.krista.extension.executor.SearchCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JqlBuilderTest {

    @Test
    void combinesMultipleFiltersWithAnd() {
        SearchTicketsFilter filter = new SearchTicketsFilter();
        filter.projectKey = "KE";
        filter.status = "In Progress";
        filter.assigneeAccountId = "abc123";

        String jql = JqlBuilder.fromExplicitFilters(filter);

        assertTrue(jql.contains("project = \"KE\""));
        assertTrue(jql.contains("status = \"In Progress\""));
        assertTrue(jql.contains("assignee = \"abc123\""));
        assertTrue(jql.contains(" AND "));
    }

    @Test
    void groupsLabelsIntoInClause() {
        SearchTicketsFilter filter = new SearchTicketsFilter();
        filter.labels = List.of("urgent", "customer-reported");

        String jql = JqlBuilder.fromExplicitFilters(filter);

        assertEquals("labels in (\"urgent\", \"customer-reported\")", jql);
    }

    @Test
    void textSearchUsesContainsOperator() {
        SearchTicketsFilter filter = new SearchTicketsFilter();
        filter.textSearch = "login failure";

        String jql = JqlBuilder.fromExplicitFilters(filter);

        assertEquals("text ~ \"login failure\"", jql);
    }

    @Test
    void escapesQuotesAndBackslashesInValues() {
        SearchTicketsFilter filter = new SearchTicketsFilter();
        filter.status = "Needs \"Review\"";

        String jql = JqlBuilder.fromExplicitFilters(filter);

        assertEquals("status = \"Needs \\\"Review\\\"\"", jql);
    }

    @Test
    void appendsOrderByWhenSortRequested() {
        SearchTicketsFilter filter = new SearchTicketsFilter();
        filter.projectKey = "KE";
        filter.sortBy = "Updated";
        filter.sortDescending = true;

        String jql = JqlBuilder.fromExplicitFilters(filter);

        assertTrue(jql.endsWith("ORDER BY updated DESC"));
    }

    @Test
    void emptyFilterProducesEmptyJql() {
        String jql = JqlBuilder.fromExplicitFilters(new SearchTicketsFilter());
        assertEquals("", jql);
    }

    @Test
    void unmappedSearchConditionFieldIsSkippedNotFailed() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "value", "Some Unknown Field"),
                new SearchCondition(SearchCondition.Operator.equals, "KE", "Project Key"));

        String jql = JqlBuilder.fromSearchConditions(conditions);

        assertEquals("project = \"KE\"", jql);
    }

    @Test
    void searchConditionLikeOperatorMapsToContains() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.contains, "login", "Summary"));

        String jql = JqlBuilder.fromSearchConditions(conditions);

        assertEquals("summary ~ \"login\"", jql);
    }
}
