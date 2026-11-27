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

/**
 * Workflow transitions - the only supported way to change a ticket's status. The status field is
 * never updated directly: every change goes through a validated transition, then is verified against
 * the ticket's resulting status.
 */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class WorkflowArea {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowArea.class);

    private final JiraService service;

    @Inject
    public WorkflowArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0020-4a00-8000-000000000020",
            name = "Get Available Workflow Transitions",
            description = "Retrieve the workflow transitions currently available for a ticket, including any field a transition additionally requires.",
            area = "Workflow",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Transitions", type = "[ Entity(Transition) ]", required = false)
    public ExtensionResponse getAvailableWorkflowTransitions(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Transitions", service.getTransitions(issueKey));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Available Workflow Transitions failed for {}", issueKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0021-4a00-8000-000000000021",
            name = "Transition Ticket Status",
            description = "Move a ticket to a new status via a named, currently-available workflow transition (never a direct status update); verifies the resulting status.",
            area = "Workflow",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Ticket", type = "Entity(Ticket)", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse transitionTicketStatus(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey,
            @Field.Text(name = "Transition Name", required = true, attributes = {}, options = {}) String transitionName,
            @Field.Desc(name = "Additional Fields", type = "[ { FieldId: Text, Value: Text } ]", required = false) List<Map<String, Object>> additionalFields) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Ticket ticket = service.transitionTicket(issueKey, transitionName, additionalFields);
            out.put("Ticket", ticket);
            out.put("Success", true);
            out.put("Message", issueKey + " is now " + ticket.statusName + ".");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Transition Ticket Status failed for {}", issueKey, e);
            out.put("Success", false);
            out.put("Message", e.getMessage());
            return new ExtensionResponseBuilder().success(out).build();
        }
    }
}
