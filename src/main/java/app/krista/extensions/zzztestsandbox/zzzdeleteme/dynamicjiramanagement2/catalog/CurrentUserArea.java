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
import java.util.Map;

/** Resolves the authenticated Jira identity - used to default Reporter and to interpret "me". */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class CurrentUserArea {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentUserArea.class);

    private final JiraService service;

    @Inject
    public CurrentUserArea(JiraService service) {
        this.service = service;
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0013-4a00-8000-000000000013",
            name = "Get Current User",
            description = "Retrieve the authenticated Jira user (GET /myself).",
            area = "Current User",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "User", type = "Entity(User)", required = false)
    public ExtensionResponse getCurrentUser() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("User", service.getCurrentUser());
            return new ExtensionResponseBuilder().success(out).build();
        } catch (JiraApiException e) {
            LOG.error("Get Current User failed", e);
            return new ExtensionResponseBuilder().failure(e.getMessage()).build();
        }
    }
}
