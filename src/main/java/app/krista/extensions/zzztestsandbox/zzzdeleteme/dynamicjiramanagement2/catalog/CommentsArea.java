package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.Attribute;
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

/** Adding and retrieving ticket comments. */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class CommentsArea {

    private static final Logger LOG = LoggerFactory.getLogger(CommentsArea.class);

    private final JiraService service;

    @Inject
    public CommentsArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0018-4a00-8000-000000000018",
            name = "Add Comment To Ticket",
            description = "Add a comment to a ticket, optionally restricted to a project role.",
            area = "Comments",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "Comment", type = "Entity(Comment)", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse addCommentToTicket(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey,
            @Field(name = "Comment Text", type = "Paragraph", required = true, attributes = {}, options = {}) String commentText,
            @Field.Text(name = "Visibility Role", required = false, attributes = {}, options = {}) String visibilityRole) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Comment", service.addComment(issueKey, commentText, visibilityRole));
            out.put("Success", true);
            out.put("Message", "Comment added to " + issueKey + ".");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Add Comment To Ticket failed for {}", issueKey, e);
            out.put("Success", false);
            out.put("Message", e.getMessage());
            return new ExtensionResponseBuilder().success(out).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0019-4a00-8000-000000000019",
            name = "Get Ticket Comments",
            description = "Retrieve a ticket's comments, paginated.",
            area = "Comments",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Comments", type = "[ Entity(Comment) ]", required = false)
    @Field(name = "Count", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse getTicketComments(
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey,
            @Field(name = "Start At", type = "Number", required = false, attributes = {}, options = {}) Double startAt,
            @Field(name = "Max Results", type = "Number", required = false, attributes = {}, options = {}) Double maxResults) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            var result = service.getComments(issueKey, toInt(startAt), toInt(maxResults));
            out.put("Comments", result.items());
            out.put("Count", (double) result.count());
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Ticket Comments failed for {}", issueKey, e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }

    private Integer toInt(Double value) {
        return value == null ? null : value.intValue();
    }
}
