package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import app.krista.extension.executor.SearchCondition;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compiles the Search Tickets catalog request's typed filters - and, separately, generic KME
 * {@link SearchCondition}s against the Ticket entity - into a Jira Query Language (JQL) string.
 * "me" is resolved by the caller (the authenticated account id) before any clause is built here.
 */
public final class JqlBuilder {

    private static final DateTimeFormatter JQL_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.US)
            .withZone(ZoneOffset.UTC);

    private JqlBuilder() {
    }

    public static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String epochToJqlDate(long epochMillis) {
        return JQL_DATE.format(Instant.ofEpochMilli(epochMillis));
    }

    /** Builds JQL from the Search Tickets catalog request's explicit, typed filter parameters. */
    public static String fromExplicitFilters(SearchTicketsFilter filter) {
        List<String> clauses = new ArrayList<>();
        addEquals(clauses, "project", filter.projectKey);
        addEquals(clauses, "key", filter.issueKey);
        addEquals(clauses, "status", filter.status);
        addEquals(clauses, "reporter", filter.reporterAccountId);
        addEquals(clauses, "assignee", filter.assigneeAccountId);
        addEquals(clauses, "priority", filter.priority);
        addEquals(clauses, "issuetype", filter.issueType);
        addEquals(clauses, "resolution", filter.resolution);
        addIn(clauses, "labels", filter.labels);
        addIn(clauses, "component", filter.components);
        if (filter.textSearch != null && !filter.textSearch.isBlank()) {
            clauses.add("text ~ \"" + escape(filter.textSearch) + "\"");
        }
        addDateRange(clauses, "created", filter.createdAfter, filter.createdBefore);
        addDateRange(clauses, "updated", filter.updatedAfter, filter.updatedBefore);
        addDateRange(clauses, "due", filter.dueAfter, filter.dueBefore);

        StringBuilder jql = new StringBuilder(String.join(" AND ", clauses));
        if (filter.sortBy != null && !filter.sortBy.isBlank()) {
            if (jql.length() > 0) {
                jql.append(' ');
            }
            jql.append("ORDER BY ").append(jqlOrderField(filter.sortBy))
                    .append(Boolean.TRUE.equals(filter.sortDescending) ? " DESC" : " ASC");
        }
        return jql.toString();
    }

    /** Translates generic KME search conditions against the Ticket entity into JQL (for TicketStore). */
    public static String fromSearchConditions(List<SearchCondition> conditions) {
        List<String> clauses = new ArrayList<>();
        for (SearchCondition condition : conditions) {
            String jqlField = ticketFieldToJql(condition.getFieldName());
            if (jqlField == null || condition.getOperand() == null) {
                continue; // unmapped/unsupported field - skip rather than fail the whole search
            }
            String value = String.valueOf(condition.getOperand());
            switch (condition.getOperator()) {
                case equals -> clauses.add(jqlField + " = \"" + escape(value) + "\"");
                case notequals -> clauses.add(jqlField + " != \"" + escape(value) + "\"");
                case contains, startswith, endswith -> clauses.add(jqlField + " ~ \"" + escape(value) + "\"");
                case gt -> clauses.add(jqlField + " > \"" + escape(value) + "\"");
                case gte -> clauses.add(jqlField + " >= \"" + escape(value) + "\"");
                case lt -> clauses.add(jqlField + " < \"" + escape(value) + "\"");
                case lte -> clauses.add(jqlField + " <= \"" + escape(value) + "\"");
                default -> { /* orderby handled via QueryClause, not here */ }
            }
        }
        return String.join(" AND ", clauses);
    }

    private static String ticketFieldToJql(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return switch (fieldName.toLowerCase(Locale.ROOT).replace(" ", "")) {
            case "key", "id" -> "key";
            case "summary" -> "summary";
            case "statusname" -> "status";
            case "priorityname" -> "priority";
            case "issuetypename" -> "issuetype";
            case "projectkey" -> "project";
            case "labels" -> "labels";
            case "duedate" -> "due";
            case "created" -> "created";
            case "updated" -> "updated";
            case "resolutionname" -> "resolution";
            default -> null;
        };
    }

    private static String jqlOrderField(String sortBy) {
        String mapped = ticketFieldToJql(sortBy);
        return mapped != null ? mapped : "updated";
    }

    private static void addEquals(List<String> clauses, String jqlField, String value) {
        if (value != null && !value.isBlank()) {
            clauses.add(jqlField + " = \"" + escape(value) + "\"");
        }
    }

    private static void addIn(List<String> clauses, String jqlField, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        List<String> quoted = new ArrayList<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                quoted.add("\"" + escape(v) + "\"");
            }
        }
        if (!quoted.isEmpty()) {
            clauses.add(jqlField + " in (" + String.join(", ", quoted) + ")");
        }
    }

    private static void addDateRange(List<String> clauses, String jqlField, Long after, Long before) {
        if (after != null) {
            clauses.add(jqlField + " >= \"" + epochToJqlDate(after) + "\"");
        }
        if (before != null) {
            clauses.add(jqlField + " <= \"" + epochToJqlDate(before) + "\"");
        }
    }
}
