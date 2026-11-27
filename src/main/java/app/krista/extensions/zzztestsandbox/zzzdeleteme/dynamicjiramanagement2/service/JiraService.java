package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.service;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.DynamicJiraManagement2Attributes;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.ChangelogEntry;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Comment;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.FieldSchema;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.IssueType;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Priority;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Project;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Resolution;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Status;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Ticket;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Transition;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.User;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.AdfUtil;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.FieldSchemaDto;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.IdempotencyGuard;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiClient;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JqlBuilder;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.SchemaCache;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.SearchTicketsFilter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business-logic orchestrator shared by every Area. Owns schema discovery + caching, field
 * validation against the live schema, request building (including ADF), name-to-id resolution, JQL
 * search, and the idempotency guard - the Area classes stay thin framework glue.
 */
@Service
public class JiraService {

    private static final Logger LOG = LoggerFactory.getLogger(JiraService.class);
    private static final Map<String, String> FIXED_CREATE_FIELD_IDS = Map.of(
            "ProjectKey", "project", "IssueTypeId", "issuetype", "Summary", "summary",
            "Description", "description", "Priority", "priority", "Assignee", "assignee",
            "Labels", "labels", "Components", "components", "DueDate", "duedate");

    public record PagedResult<T>(List<T> items, long count) {
    }

    public record TicketSearchResult(List<Ticket> tickets, Long approxCount, String nextPageToken, boolean isLast) {
    }

    private final JiraApiClient client;
    private final DynamicJiraManagement2Attributes attributes;
    private final SchemaCache schemaCache;
    private final IdempotencyGuard idempotencyGuard;
    private final String invokerId;

    @Inject
    public JiraService(JiraApiClient client, DynamicJiraManagement2Attributes attributes, SchemaCache schemaCache,
                        IdempotencyGuard idempotencyGuard, Invoker invoker) {
        this.client = client;
        this.attributes = attributes;
        this.schemaCache = schemaCache;
        this.idempotencyGuard = idempotencyGuard;
        this.invokerId = invoker.getInvokerId();
    }

    // ============================================================= Projects & Metadata =============

    public PagedResult<Project> listProjects(String searchText, Integer startAt, Integer maxResults)
            throws JiraApiException, MustAuthorizeException {
        Map<String, String> query = new LinkedHashMap<>();
        if (searchText != null && !searchText.isBlank()) {
            query.put("query", searchText);
        }
        query.put("startAt", String.valueOf(startAt != null ? startAt : 0));
        query.put("maxResults", String.valueOf(maxResults != null ? maxResults : 50));
        JsonObject page = client.get("/project/search", query).getAsJsonObject();
        List<Project> projects = new ArrayList<>();
        for (JsonElement value : page.getAsJsonArray("values")) {
            projects.add(Project.fromJson(value.getAsJsonObject()));
        }
        long total = page.has("total") ? page.get("total").getAsLong() : projects.size();
        return new PagedResult<>(projects, total);
    }

    public Project getProject(String projectKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(projectKey, "Project Key");
        JsonObject response = client.get("/project/" + encode(projectKey), Map.of()).getAsJsonObject();
        return Project.fromJson(response);
    }

    public List<Map<String, Object>> getProjectComponents(String projectKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(projectKey, "Project Key");
        JsonElement response = client.get("/project/" + encode(projectKey) + "/components", Map.of());
        List<Map<String, Object>> components = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray()) {
            JsonObject obj = value.getAsJsonObject();
            components.add(Map.of("Id", textOrEmpty(obj, "id"), "Name", textOrEmpty(obj, "name")));
        }
        return components;
    }

    public List<IssueType> listIssueTypesForProject(String projectKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(projectKey, "Project Key");
        Map<String, String> query = Map.of("maxResults", "200");
        JsonObject response = client.get("/issue/createmeta/" + encode(projectKey) + "/issuetypes", query).getAsJsonObject();
        List<IssueType> types = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("values")) {
            types.add(IssueType.fromJson(value.getAsJsonObject()));
        }
        return types;
    }

    public List<Priority> listPriorities() throws JiraApiException, MustAuthorizeException {
        Map<String, String> query = Map.of("maxResults", "200");
        JsonObject response = client.get("/priority/search", query).getAsJsonObject();
        List<Priority> priorities = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("values")) {
            priorities.add(Priority.fromJson(value.getAsJsonObject()));
        }
        return priorities;
    }

    public List<Status> listStatuses(String projectKey, String issueTypeId) throws JiraApiException, MustAuthorizeException {
        List<Status> statuses = new ArrayList<>();
        if (projectKey != null && !projectKey.isBlank()) {
            JsonElement response = client.get("/project/" + encode(projectKey) + "/statuses", Map.of());
            for (JsonElement group : response.getAsJsonArray()) {
                JsonObject groupObj = group.getAsJsonObject();
                if (issueTypeId != null && !issueTypeId.isBlank() && !issueTypeId.equals(textOrEmpty(groupObj, "id"))) {
                    continue;
                }
                for (JsonElement status : groupObj.getAsJsonArray("statuses")) {
                    statuses.add(Status.fromJson(status.getAsJsonObject()));
                }
            }
        } else {
            JsonElement response = client.get("/status", Map.of());
            for (JsonElement status : response.getAsJsonArray()) {
                statuses.add(Status.fromJson(status.getAsJsonObject()));
            }
        }
        return statuses;
    }

    public List<Resolution> listResolutions() throws JiraApiException, MustAuthorizeException {
        Map<String, String> query = Map.of("maxResults", "200");
        JsonObject response = client.get("/resolution/search", query).getAsJsonObject();
        List<Resolution> resolutions = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("values")) {
            resolutions.add(Resolution.fromJson(value.getAsJsonObject()));
        }
        return resolutions;
    }

    public List<String> listLabels(String searchText) throws JiraApiException, MustAuthorizeException {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("maxResults", "200");
        JsonObject response = client.get("/label", query).getAsJsonObject();
        List<String> labels = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("values")) {
            String label = value.getAsString();
            if (searchText == null || searchText.isBlank() || label.toLowerCase().contains(searchText.toLowerCase())) {
                labels.add(label);
            }
        }
        return labels;
    }

    // ============================================================= Schema Discovery =================

    public List<FieldSchema> discoverCreateFieldSchema(String projectKey, String issueTypeId, boolean forceRefresh)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(projectKey, "Project Key");
        requireNonBlank(issueTypeId, "Issue Type Id");
        String cacheKey = SchemaCache.createIssueKey(invokerId, projectKey, issueTypeId);
        if (forceRefresh) {
            schemaCache.invalidate(cacheKey);
        }
        List<JiraApiException> errorBox = new ArrayList<>();
        List<FieldSchemaDto> dtos = schemaCache.getOrLoad(cacheKey, () -> {
            try {
                return fetchCreateFieldSchema(projectKey, issueTypeId);
            } catch (JiraApiException | MustAuthorizeException e) {
                errorBox.add(e instanceof JiraApiException jae ? jae
                        : new JiraApiException(JiraApiException.Category.AUTHENTICATION, e.getMessage()));
                return List.of();
            }
        });
        if (!errorBox.isEmpty() && dtos.isEmpty()) {
            throw errorBox.get(0);
        }
        return dtos.stream().map(FieldSchema::fromDto).toList();
    }

    private List<FieldSchemaDto> fetchCreateFieldSchema(String projectKey, String issueTypeId)
            throws JiraApiException, MustAuthorizeException {
        Map<String, String> query = Map.of("maxResults", "200");
        JsonObject response = client.get(
                "/issue/createmeta/" + encode(projectKey) + "/issuetypes/" + encode(issueTypeId), query).getAsJsonObject();
        List<FieldSchemaDto> result = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("values")) {
            result.add(parseFieldSchema(value.getAsJsonObject(), "fieldId"));
        }
        return result;
    }

    public List<FieldSchema> getEditFieldSchema(String issueKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        String cacheKey = SchemaCache.editIssueKey(invokerId, issueKey);
        List<FieldSchemaDto> dtos = schemaCache.getOrLoad(cacheKey, () -> {
            try {
                JsonObject response = client.get("/issue/" + encode(issueKey) + "/editmeta", Map.of()).getAsJsonObject();
                JsonObject fields = response.getAsJsonObject("fields");
                List<FieldSchemaDto> result = new ArrayList<>();
                for (String fieldId : fields.keySet()) {
                    JsonObject fieldDef = fields.getAsJsonObject(fieldId);
                    FieldSchemaDto dto = parseFieldSchemaBody(fieldDef);
                    dto.fieldId = fieldId;
                    result.add(dto);
                }
                return result;
            } catch (JiraApiException | MustAuthorizeException e) {
                LOG.error("Failed to load edit field schema for {}", issueKey, e);
                return List.<FieldSchemaDto>of();
            }
        });
        return dtos.stream().map(FieldSchema::fromDto).toList();
    }

    private FieldSchemaDto parseFieldSchema(JsonObject value, String idKey) {
        FieldSchemaDto dto = parseFieldSchemaBody(value);
        dto.fieldId = textOrEmpty(value, idKey);
        return dto;
    }

    private FieldSchemaDto parseFieldSchemaBody(JsonObject value) {
        FieldSchemaDto dto = new FieldSchemaDto();
        dto.name = textOrEmpty(value, "name");
        dto.required = value.has("required") && value.get("required").getAsBoolean();
        JsonObject schema = value.has("schema") && value.get("schema").isJsonObject() ? value.getAsJsonObject("schema") : null;
        dto.dataType = schema != null ? textOrEmpty(schema, "type") : "string";
        dto.custom = schema != null && schema.has("custom");
        dto.hasDefaultValue = value.has("hasDefaultValue") && value.get("hasDefaultValue").getAsBoolean();
        dto.defaultValueDisplay = value.has("defaultValue") && !value.get("defaultValue").isJsonNull()
                ? value.get("defaultValue").toString() : null;
        List<Map<String, String>> allowed = new ArrayList<>();
        if (value.has("allowedValues") && value.get("allowedValues").isJsonArray()) {
            for (JsonElement av : value.getAsJsonArray("allowedValues")) {
                if (!av.isJsonObject()) {
                    continue;
                }
                JsonObject avObj = av.getAsJsonObject();
                String id = avObj.has("id") ? avObj.get("id").getAsString() : "";
                String display = avObj.has("name") ? avObj.get("name").getAsString()
                        : avObj.has("value") ? avObj.get("value").getAsString() : id;
                allowed.add(Map.of("id", id, "value", display));
            }
        }
        dto.allowedValues = allowed;
        return dto;
    }

    // ============================================================= Users ============================

    public User getCurrentUser() throws JiraApiException, MustAuthorizeException {
        JsonObject response = client.get("/myself", Map.of()).getAsJsonObject();
        return User.fromJson(response);
    }

    public List<User> searchUsers(String searchText, String projectKey, Integer maxResults)
            throws JiraApiException, MustAuthorizeException {
        Map<String, String> query = new LinkedHashMap<>();
        if (searchText != null) {
            query.put("query", searchText);
        }
        query.put("maxResults", String.valueOf(maxResults != null ? maxResults : 50));
        JsonElement response;
        if (projectKey != null && !projectKey.isBlank()) {
            query.put("project", projectKey);
            response = client.get("/user/assignable/search", query);
        } else {
            response = client.get("/user/search", query);
        }
        List<User> users = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray()) {
            users.add(User.fromJson(value.getAsJsonObject()));
        }
        return users;
    }

    // ============================================================= Tickets ==========================

    public Ticket createTicket(String projectKey, String issueTypeId, String summary, String description,
                                String priority, String assignee, List<String> labels, List<String> components,
                                Long dueDate, List<Map<String, Object>> additionalFields)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(projectKey, "Project Key");
        requireNonBlank(issueTypeId, "Issue Type Id");
        requireNonBlank(summary, "Summary");

        String fingerprint = IdempotencyGuard.fingerprint(invokerId, projectKey, issueTypeId, summary, description);
        String duplicate = idempotencyGuard.findRecentDuplicate(fingerprint);
        if (duplicate != null) {
            LOG.info("Duplicate Create Ticket suppressed for invoker {} - returning existing {}", invokerId, duplicate);
            return getTicket(duplicate);
        }

        List<FieldSchemaDto> schema = toDto(discoverCreateFieldSchema(projectKey, issueTypeId, false));

        Map<String, Object> providedByFieldId = new LinkedHashMap<>();
        providedByFieldId.put("project", projectKey);
        providedByFieldId.put("issuetype", issueTypeId);
        providedByFieldId.put("summary", summary);
        if (notBlank(description)) providedByFieldId.put("description", description);
        if (notBlank(priority)) providedByFieldId.put("priority", priority);
        if (notBlank(assignee)) providedByFieldId.put("assignee", assignee);
        if (labels != null && !labels.isEmpty()) providedByFieldId.put("labels", labels);
        if (components != null && !components.isEmpty()) providedByFieldId.put("components", components);
        if (dueDate != null) providedByFieldId.put("duedate", dueDate);
        if (additionalFields != null) {
            for (Map<String, Object> extra : additionalFields) {
                Object fieldId = extra.get("FieldId");
                if (fieldId != null) {
                    providedByFieldId.put(String.valueOf(fieldId), extra.get("Value"));
                }
            }
        }

        validateAgainstSchema(schema, providedByFieldId);

        JsonObject fields = new JsonObject();
        fields.add("project", projectRef(projectKey));
        fields.add("issuetype", idRef(issueTypeId));
        fields.addProperty("summary", summary);
        if (notBlank(description)) {
            fields.add("description", AdfUtil.plainTextToAdf(description));
        }
        if (notBlank(priority)) {
            fields.add("priority", idOrNameRef(resolveAllowedValueId(schema, "priority", priority)));
        }
        if (notBlank(assignee)) {
            String accountId = resolveAssigneeAccountId(assignee, projectKey);
            fields.add("assignee", accountIdRef(accountId));
        } else if (notBlank(attributes.getDefaultAssigneeAccountId())) {
            fields.add("assignee", accountIdRef(attributes.getDefaultAssigneeAccountId()));
        }
        if (labels != null && !labels.isEmpty()) {
            fields.add("labels", toJsonArray(labels));
        }
        if (components != null && !components.isEmpty()) {
            fields.add("components", componentsArray(resolveComponentIds(projectKey, components)));
        }
        if (dueDate != null) {
            fields.addProperty("duedate", JqlBuilder.epochToJqlDate(dueDate).substring(0, 10).replace('/', '-'));
        }
        applyAdditionalFields(fields, additionalFields);

        JsonObject body = new JsonObject();
        body.add("fields", fields);
        JsonObject created = client.post("/issue", body).getAsJsonObject();
        String issueKey = textOrEmpty(created, "key");
        idempotencyGuard.remember(fingerprint, issueKey);
        LOG.info("Created Jira ticket {} for invoker {}", issueKey, invokerId);
        return getTicket(issueKey);
    }

    public Ticket getTicket(String issueKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        JsonObject response = client.get("/issue/" + encode(issueKey), Map.of("expand", "names")).getAsJsonObject();
        return Ticket.fromJson(response);
    }

    public Ticket updateTicket(String issueKey, String summary, String description, String priority,
                                List<String> labels, List<String> components, Long dueDate,
                                List<Map<String, Object>> additionalFields) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        List<FieldSchemaDto> schema = toDto(getEditFieldSchema(issueKey));

        JsonObject fields = new JsonObject();
        if (notBlank(summary)) fields.addProperty("summary", summary);
        if (notBlank(description)) fields.add("description", AdfUtil.plainTextToAdf(description));
        if (notBlank(priority)) fields.add("priority", idOrNameRef(resolveAllowedValueId(schema, "priority", priority)));
        if (labels != null && !labels.isEmpty()) fields.add("labels", toJsonArray(labels));
        if (components != null && !components.isEmpty()) {
            String projectKey = getTicket(issueKey).projectKey;
            fields.add("components", componentsArray(resolveComponentIds(projectKey, components)));
        }
        if (dueDate != null) fields.addProperty("duedate", JqlBuilder.epochToJqlDate(dueDate).substring(0, 10).replace('/', '-'));
        applyAdditionalFields(fields, additionalFields);

        if (fields.entrySet().isEmpty()) {
            throw new JiraApiException(JiraApiException.Category.VALIDATION, "No fields were supplied to update.");
        }

        Map<String, Object> providedByFieldId = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
            providedByFieldId.put(entry.getKey(), entry.getValue());
        }
        validateEditableAgainstSchema(schema, providedByFieldId);

        JsonObject body = new JsonObject();
        body.add("fields", fields);
        client.put("/issue/" + encode(issueKey), body);
        schemaCache.invalidate(SchemaCache.editIssueKey(invokerId, issueKey));
        LOG.info("Updated Jira ticket {} for invoker {}: fields {}", issueKey, invokerId, fields.keySet());
        return getTicket(issueKey);
    }

    public PagedResult<ChangelogEntry> getChangelog(String issueKey, Integer startAt, Integer maxResults)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        Map<String, String> query = Map.of(
                "startAt", String.valueOf(startAt != null ? startAt : 0),
                "maxResults", String.valueOf(maxResults != null ? maxResults : 50));
        JsonObject response = client.get("/issue/" + encode(issueKey) + "/changelog", query).getAsJsonObject();
        List<ChangelogEntry> entries = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("values")) {
            entries.add(ChangelogEntry.fromJson(value.getAsJsonObject()));
        }
        long total = response.has("total") ? response.get("total").getAsLong() : entries.size();
        return new PagedResult<>(entries, total);
    }

    // ============================================================= Comments =========================

    public Comment addComment(String issueKey, String commentText, String visibilityRole)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        requireNonBlank(commentText, "Comment Text");
        JsonObject body = new JsonObject();
        body.add("body", AdfUtil.plainTextToAdf(commentText));
        if (notBlank(visibilityRole)) {
            JsonObject visibility = new JsonObject();
            visibility.addProperty("type", "role");
            visibility.addProperty("value", visibilityRole);
            body.add("visibility", visibility);
        }
        JsonObject response = client.post("/issue/" + encode(issueKey) + "/comment", body).getAsJsonObject();
        return Comment.fromJson(response);
    }

    public PagedResult<Comment> getComments(String issueKey, Integer startAt, Integer maxResults)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        Map<String, String> query = Map.of(
                "startAt", String.valueOf(startAt != null ? startAt : 0),
                "maxResults", String.valueOf(maxResults != null ? maxResults : 50));
        JsonObject response = client.get("/issue/" + encode(issueKey) + "/comment", query).getAsJsonObject();
        List<Comment> comments = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("comments")) {
            comments.add(Comment.fromJson(value.getAsJsonObject()));
        }
        long total = response.has("total") ? response.get("total").getAsLong() : comments.size();
        return new PagedResult<>(comments, total);
    }

    // ============================================================= Workflow ==========================

    public List<Transition> getTransitions(String issueKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        JsonObject response = client.get("/issue/" + encode(issueKey) + "/transitions",
                Map.of("expand", "transitions.fields")).getAsJsonObject();
        List<Transition> transitions = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("transitions")) {
            transitions.add(Transition.fromJson(value.getAsJsonObject()));
        }
        return transitions;
    }

    public Ticket transitionTicket(String issueKey, String transitionName, List<Map<String, Object>> additionalFields)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        requireNonBlank(transitionName, "Transition Name");
        List<Transition> available = getTransitions(issueKey);
        Transition match = available.stream()
                .filter(t -> t.name.equalsIgnoreCase(transitionName))
                .findFirst()
                .orElseThrow(() -> new JiraApiException(JiraApiException.Category.VALIDATION,
                        "\"" + transitionName + "\" is not a currently available transition for " + issueKey
                                + ". Available: " + available.stream().map(t -> t.name).toList()));

        List<String> missingRequired = new ArrayList<>();
        if (match.requiredFields != null) {
            for (Map<String, Object> required : match.requiredFields) {
                String fieldId = String.valueOf(required.get("FieldId"));
                boolean supplied = additionalFields != null && additionalFields.stream()
                        .anyMatch(f -> fieldId.equals(String.valueOf(f.get("FieldId"))));
                if (!supplied) {
                    missingRequired.add(String.valueOf(required.get("Name")));
                }
            }
        }
        if (!missingRequired.isEmpty()) {
            throw new JiraApiException(JiraApiException.Category.VALIDATION,
                    "This transition requires additional field(s) that were not supplied: " + missingRequired, missingRequired);
        }

        JsonObject transitionBody = new JsonObject();
        JsonObject transitionRef = new JsonObject();
        transitionRef.addProperty("id", match.id);
        transitionBody.add("transition", transitionRef);
        JsonObject fields = new JsonObject();
        applyAdditionalFields(fields, additionalFields);
        if (!fields.entrySet().isEmpty()) {
            transitionBody.add("fields", fields);
        }
        client.post("/issue/" + encode(issueKey) + "/transitions", transitionBody);

        Ticket updated = getTicket(issueKey);
        if (!match.toStatusName.equalsIgnoreCase(updated.statusName)) {
            LOG.warn("Transition {} on {} reported success but status is now '{}', expected '{}'",
                    transitionName, issueKey, updated.statusName, match.toStatusName);
        }
        return updated;
    }

    // ============================================================= Assignment ========================

    public Ticket assignTicket(String issueKey, boolean assignToCurrentUser, String assigneeSearchText)
            throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        String accountId;
        if (assignToCurrentUser) {
            accountId = getCurrentUser().accountId;
        } else {
            requireNonBlank(assigneeSearchText, "Assignee Search Text");
            String projectKey = getTicket(issueKey).projectKey;
            accountId = resolveAssigneeAccountId(assigneeSearchText, projectKey);
        }
        JsonObject body = new JsonObject();
        body.addProperty("accountId", accountId);
        client.put("/issue/" + encode(issueKey) + "/assignee", body);
        return getTicket(issueKey);
    }

    public void unassignTicket(String issueKey) throws JiraApiException, MustAuthorizeException {
        requireNonBlank(issueKey, "Issue Key");
        JsonObject body = new JsonObject();
        body.add("accountId", com.google.gson.JsonNull.INSTANCE);
        client.put("/issue/" + encode(issueKey) + "/assignee", body);
    }

    // ============================================================= Search ============================

    public TicketSearchResult searchTickets(SearchTicketsFilter filter) throws JiraApiException, MustAuthorizeException {
        if (notBlank(filter.reporterAccountId) && "me".equalsIgnoreCase(filter.reporterAccountId)) {
            filter.reporterAccountId = getCurrentUser().accountId;
        }
        if (notBlank(filter.assigneeAccountId) && "me".equalsIgnoreCase(filter.assigneeAccountId)) {
            filter.assigneeAccountId = getCurrentUser().accountId;
        }
        String jql = JqlBuilder.fromExplicitFilters(filter);

        JsonObject body = new JsonObject();
        body.addProperty("jql", jql);
        body.addProperty("maxResults", filter.maxResults != null ? filter.maxResults : 50);
        if (notBlank(filter.nextPageToken)) {
            body.addProperty("nextPageToken", filter.nextPageToken);
        }
        JsonObject response = client.post("/search/jql", body).getAsJsonObject();
        List<Ticket> tickets = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("issues")) {
            tickets.add(Ticket.fromJson(value.getAsJsonObject()));
        }
        String nextPageToken = response.has("nextPageToken") ? textOrEmpty(response, "nextPageToken") : null;
        boolean isLast = !response.has("nextPageToken") || response.get("nextPageToken").isJsonNull();

        Long approxCount = null;
        try {
            JsonObject countBody = new JsonObject();
            countBody.addProperty("jql", jql);
            JsonObject countResponse = client.post("/search/approximate-count", countBody).getAsJsonObject();
            approxCount = countResponse.has("count") ? countResponse.get("count").getAsLong() : null;
        } catch (Exception e) {
            LOG.warn("Approximate count failed for jql [{}] - omitting count", jql, e);
        }
        return new TicketSearchResult(tickets, approxCount, nextPageToken, isLast);
    }

    /** Used by TicketStore (KME entity search) - plain JQL in, Ticket list out, no count/paging niceties. */
    public List<Ticket> searchTicketsForEntityStore(String jql, int maxResults) throws JiraApiException, MustAuthorizeException {
        JsonObject body = new JsonObject();
        body.addProperty("jql", jql == null || jql.isBlank() ? "order by updated desc" : jql);
        body.addProperty("maxResults", maxResults);
        JsonObject response = client.post("/search/jql", body).getAsJsonObject();
        List<Ticket> tickets = new ArrayList<>();
        for (JsonElement value : response.getAsJsonArray("issues")) {
            tickets.add(Ticket.fromJson(value.getAsJsonObject()));
        }
        return tickets;
    }

    // ============================================================= Validation / resolution helpers ===

    private void validateAgainstSchema(List<FieldSchemaDto> schema, Map<String, Object> providedByFieldId)
            throws JiraApiException {
        List<String> missing = new ArrayList<>();
        for (FieldSchemaDto field : schema) {
            if (!field.required) {
                continue;
            }
            if ("reporter".equals(field.fieldId)) {
                continue; // always the authenticated user - Jira defaults it, never asked of the caller
            }
            Object value = providedByFieldId.get(field.fieldId);
            boolean hasDefault = field.hasDefaultValue;
            if ((value == null || String.valueOf(value).isBlank()) && !hasDefault) {
                missing.add(field.name + " (" + field.fieldId + ")");
            }
        }
        if (!missing.isEmpty()) {
            throw new JiraApiException(JiraApiException.Category.VALIDATION,
                    "This project and issue type require the following field(s), which were not supplied: "
                            + String.join(", ", missing) + ". Please provide them and try again.", missing);
        }
    }

    private void validateEditableAgainstSchema(List<FieldSchemaDto> schema, Map<String, Object> providedByFieldId)
            throws JiraApiException {
        List<String> notEditable = new ArrayList<>();
        Map<String, FieldSchemaDto> byId = new LinkedHashMap<>();
        for (FieldSchemaDto field : schema) {
            byId.put(field.fieldId, field);
        }
        for (String fieldId : providedByFieldId.keySet()) {
            if (!byId.containsKey(fieldId)) {
                notEditable.add(fieldId);
            }
        }
        if (!notEditable.isEmpty()) {
            throw new JiraApiException(JiraApiException.Category.VALIDATION,
                    "The following field(s) are not editable on this ticket (check Get Editable Field Schema For Ticket): "
                            + notEditable, notEditable);
        }
    }

    private String resolveAllowedValueId(List<FieldSchemaDto> schema, String fieldId, String candidate) throws JiraApiException {
        FieldSchemaDto field = schema.stream().filter(f -> fieldId.equals(f.fieldId)).findFirst().orElse(null);
        if (field == null || field.allowedValues == null || field.allowedValues.isEmpty()) {
            return candidate; // no fixed list on this instance/project - pass through as an id
        }
        if (!field.isAllowedValue(candidate)) {
            List<String> options = field.allowedValues.stream().map(v -> v.get("value")).toList();
            throw new JiraApiException(JiraApiException.Category.VALIDATION,
                    "\"" + candidate + "\" is not a valid value for " + field.name + ". Valid options: " + options);
        }
        for (Map<String, String> option : field.allowedValues) {
            if (candidate.equalsIgnoreCase(option.get("id")) || candidate.equalsIgnoreCase(option.get("value"))) {
                return option.get("id");
            }
        }
        return candidate;
    }

    private String resolveAssigneeAccountId(String assigneeInput, String projectKey) throws JiraApiException, MustAuthorizeException {
        List<User> matches = searchUsers(assigneeInput, projectKey, 10);
        List<User> exact = matches.stream()
                .filter(u -> assigneeInput.equalsIgnoreCase(u.accountId) || assigneeInput.equalsIgnoreCase(u.displayName)
                        || assigneeInput.equalsIgnoreCase(u.email))
                .toList();
        if (exact.size() == 1) {
            return exact.get(0).accountId;
        }
        if (matches.size() == 1) {
            return matches.get(0).accountId;
        }
        if (matches.size() > 1) {
            throw new JiraApiException(JiraApiException.Category.VALIDATION,
                    "\"" + assigneeInput + "\" matches more than one user: "
                            + matches.stream().map(u -> u.displayName).toList() + ". Please be more specific.");
        }
        // No search match - assume the caller already supplied a literal accountId (e.g. from Search Users output).
        return assigneeInput;
    }

    private List<String> resolveComponentIds(String projectKey, List<String> componentNames) throws JiraApiException, MustAuthorizeException {
        List<Map<String, Object>> available = getProjectComponents(projectKey);
        List<String> ids = new ArrayList<>();
        for (String name : componentNames) {
            String match = available.stream()
                    .filter(c -> String.valueOf(c.get("Name")).equalsIgnoreCase(name) || String.valueOf(c.get("Id")).equals(name))
                    .map(c -> String.valueOf(c.get("Id")))
                    .findFirst()
                    .orElse(null);
            if (match == null) {
                throw new JiraApiException(JiraApiException.Category.VALIDATION,
                        "\"" + name + "\" is not a component configured on project " + projectKey + ".");
            }
            ids.add(match);
        }
        return ids;
    }

    private void applyAdditionalFields(JsonObject fields, List<Map<String, Object>> additionalFields) {
        if (additionalFields == null) {
            return;
        }
        for (Map<String, Object> entry : additionalFields) {
            Object fieldId = entry.get("FieldId");
            Object value = entry.get("Value");
            if (fieldId != null && value != null) {
                fields.addProperty(String.valueOf(fieldId), String.valueOf(value));
            }
        }
    }

    // ============================================================= small helpers =====================

    private List<FieldSchemaDto> toDto(List<FieldSchema> entities) {
        List<FieldSchemaDto> dtos = new ArrayList<>();
        for (FieldSchema e : entities) {
            FieldSchemaDto dto = new FieldSchemaDto();
            dto.fieldId = e.fieldId;
            dto.name = e.name;
            dto.required = Boolean.TRUE.equals(e.required);
            dto.dataType = e.dataType;
            dto.custom = Boolean.TRUE.equals(e.isCustomField);
            dto.hasDefaultValue = Boolean.TRUE.equals(e.hasDefaultValue);
            dto.defaultValueDisplay = e.defaultValue;
            List<Map<String, String>> allowed = new ArrayList<>();
            if (e.allowedValues != null) {
                for (Map<String, Object> v : e.allowedValues) {
                    allowed.add(Map.of("id", String.valueOf(v.get("Id")), "value", String.valueOf(v.get("Value"))));
                }
            }
            dto.allowedValues = allowed;
            dtos.add(dto);
        }
        return dtos;
    }

    private JsonObject projectRef(String projectKey) {
        JsonObject ref = new JsonObject();
        ref.addProperty("key", projectKey);
        return ref;
    }

    private JsonObject idRef(String id) {
        JsonObject ref = new JsonObject();
        ref.addProperty("id", id);
        return ref;
    }

    private JsonObject idOrNameRef(String idOrName) {
        return idRef(idOrName);
    }

    private JsonObject accountIdRef(String accountId) {
        JsonObject ref = new JsonObject();
        ref.addProperty("id", accountId);
        return ref;
    }

    private JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private JsonArray componentsArray(List<String> ids) {
        JsonArray array = new JsonArray();
        for (String id : ids) {
            array.add(idRef(id));
        }
        return array;
    }

    private static String textOrEmpty(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static void requireNonBlank(String value, String label) throws JiraApiException {
        if (value == null || value.isBlank()) {
            throw new JiraApiException(JiraApiException.Category.VALIDATION, label + " is required.");
        }
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
