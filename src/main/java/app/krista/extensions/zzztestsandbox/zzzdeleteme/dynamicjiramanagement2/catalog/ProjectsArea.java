package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities.Project;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Project lookups: which projects are accessible, and a project's own details/components. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class ProjectsArea {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsArea.class);

    private final JiraService service;

    @Inject
    public ProjectsArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0003-4a00-8000-000000000003",
            name = "List Accessible Projects",
            description = "Retrieve the Jira projects the authenticated user can access, as the first step of the dynamic schema-discovery workflow.",
            area = "Projects",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Projects", type = "[ Entity(Project) ]", required = false)
    @Field(name = "Count", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse listAccessibleProjects(
            @Field.Text(name = "Search Text", required = false, attributes = {}, options = {}) String searchText,
            @Field(name = "Start At", type = "Number", required = false, attributes = {}, options = {}) Double startAt,
            @Field(name = "Max Results", type = "Number", required = false, attributes = {}, options = {}) Double maxResults) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            var result = service.listProjects(searchText, toInt(startAt), toInt(maxResults));
            out.put("Projects", result.items());
            out.put("Count", (double) result.count());
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("List Accessible Projects failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0004-4a00-8000-000000000004",
            name = "Get Project Details",
            description = "Retrieve a single Jira project's details by key.",
            area = "Projects",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Project", type = "Entity(Project)", required = false)
    public ExtensionResponse getProjectDetails(
            @Field.Text(name = "Project Key", required = true, attributes = {}, options = {}) String projectKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Project project = service.getProject(projectKey);
            out.put("Project", project);
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Project Details failed for {}", projectKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0005-4a00-8000-000000000005",
            name = "Get Project Components",
            description = "Retrieve the components configured on a project, for populating/validating the Components field.",
            area = "Projects",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Components", type = "[ { Id: Text, Name: Text } ]", required = false)
    public ExtensionResponse getProjectComponents(
            @Field.Text(name = "Project Key", required = true, attributes = {}, options = {}) String projectKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> components = service.getProjectComponents(projectKey);
            out.put("Components", components);
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Project Components failed for {}", projectKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    private Integer toInt(Double value) {
        return value == null ? null : value.intValue();
    }
}
