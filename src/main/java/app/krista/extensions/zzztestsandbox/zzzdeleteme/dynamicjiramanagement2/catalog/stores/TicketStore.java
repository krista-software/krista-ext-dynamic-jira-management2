package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.stores;

import app.krista.extension.executor.QueryClause;
import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extension.util.EntityStore;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Ticket;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JqlBuilder;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.service.JiraService;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EntityStore for Ticket - lets Krista's generic entity search (KME) resolve and query tickets, as a
 * complement to the dedicated Search Tickets catalog request. Delegates to {@link JiraService}, which
 * owns the real HTTP/JQL/transform logic; this class only adapts the KME SearchQuery contract.
 */
@Service
public class TicketStore implements EntityStore<Ticket> {

    private static final Logger LOG = LoggerFactory.getLogger(TicketStore.class);

    private final JiraService service;

    @Inject
    public TicketStore(JiraService service) {
        this.service = service;
    }

    @Override
    public Ticket create(Map<String, Object> entityAttributeFields) {
        if (entityAttributeFields == null || entityAttributeFields.isEmpty()) {
            LOG.warn("Ticket create: attribute field map is null/empty");
            return new Ticket();
        }
        try {
            String projectKey = String.valueOf(entityAttributeFields.get("Project Key"));
            String issueTypeId = String.valueOf(entityAttributeFields.get("Issue Type Name"));
            String summary = String.valueOf(entityAttributeFields.get("Summary"));
            Object description = entityAttributeFields.get("Description");
            return service.createTicket(projectKey, issueTypeId, summary,
                    description == null ? null : String.valueOf(description),
                    null, null, null, null, null, List.of());
        } catch (Exception cause) {
            LOG.error("Ticket create via entity search failed", cause);
            return new Ticket();
        }
    }

    @Override
    public Ticket get(String primaryKey) {
        if (primaryKey == null || primaryKey.isEmpty()) {
            LOG.warn("Ticket get: primary key (issue key) is null/empty");
            return null;
        }
        try {
            return service.getTicket(primaryKey);
        } catch (Exception cause) {
            LOG.error("Ticket get failed for {}", primaryKey, cause);
            return null;
        }
    }

    @Override
    public Ticket update(Ticket entity) {
        if (entity == null || entity.key == null) {
            LOG.warn("Ticket update: entity or key is null");
            return null;
        }
        try {
            return service.updateTicket(entity.key, entity.summary, entity.description, entity.priorityName,
                    entity.labels, null, entity.dueDate, List.of());
        } catch (Exception cause) {
            LOG.error("Ticket update failed for {}", entity.key, cause);
            return null;
        }
    }

    @Override
    public void delete(String primaryKey) {
        throw new UnsupportedOperationException(
                "Deleting Jira tickets is not supported by this extension - use Transition Ticket Status instead.");
    }

    @Override
    public boolean contains(String primaryKey) {
        return get(primaryKey) != null;
    }

    @Override
    public List<Ticket> search(SearchQuery searchQuery, long pageIndex, int pageSize) throws IOException {
        try {
            if (searchQuery == null || searchQuery.getSearchConditions() == null
                    || searchQuery.getSearchConditions().isEmpty()) {
                return service.searchTicketsForEntityStore("order by updated desc", Math.max(pageSize, 50));
            }
            List<SearchCondition> conditions = searchQuery.getSearchConditions();
            for (SearchCondition sc : conditions) {
                if (("Key".equalsIgnoreCase(sc.getFieldName()) || "Id".equalsIgnoreCase(sc.getFieldName()))
                        && sc.getOperand() != null) {
                    String key = String.valueOf(sc.getOperand()).trim();
                    if (!key.isEmpty()) {
                        Ticket t = get(key);
                        return t != null ? List.of(t) : List.of();
                    }
                }
            }
            String jql = JqlBuilder.fromSearchConditions(conditions);
            List<QueryClause> clauses = searchQuery.getQueryClauses();
            String orderBy = buildOrderBy(clauses);
            String fullJql = (jql.isBlank() ? "" : jql + " ") + orderBy;
            if (fullJql.isBlank()) {
                return service.searchTicketsForEntityStore("order by updated desc", Math.max(pageSize, 50));
            }
            return service.searchTicketsForEntityStore(fullJql, Math.max(pageSize, 50));
        } catch (Exception cause) { // search NEVER throws to the executor
            LOG.error("Ticket search failed", cause);
            return List.of();
        }
    }

    private String buildOrderBy(List<QueryClause> clauses) {
        if (clauses == null || clauses.isEmpty()) {
            return "order by updated desc";
        }
        List<String> parts = new ArrayList<>();
        for (QueryClause clause : clauses) {
            parts.add(clause.getFieldName());
        }
        return parts.isEmpty() ? "order by updated desc" : "order by " + String.join(", ", parts);
    }

    @Override
    public List<Ticket> search(List<SearchCondition> conditions, long pageIndex, int pageSize) {
        return List.of();
    }

    @Override
    public long count(List<SearchCondition> conditions) {
        return 0;
    }

    @Override
    public List<String> lookup(Map<String, Object> fields, List<String> fieldNames) {
        return List.of();
    }
}
