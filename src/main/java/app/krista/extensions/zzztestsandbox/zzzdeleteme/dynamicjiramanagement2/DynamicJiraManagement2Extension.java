package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.authentication.DynamicJiraManagement2RequestAuthenticator;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiClient;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraAttributeStore;
import app.krista.model.field.NamedValuedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Dynamic Jira Management2 - schema-driven Jira Cloud ticket-management extension.
 *
 * Two admin-selectable authentication modes, per the user's confirmed choice of both mechanisms:
 * ApiToken (Krista Pattern A - HTTP Basic over email:apiToken, direct to the tenant's own site) and
 * OAuth (Krista Pattern C - 3-legged, via api.atlassian.com). AuthType is a PickOne, matching the
 * servicenow Oauth/BasicAuth precedent cited in authentication-patterns.md. No custom Authentication
 * tab is required for this - both options live on the standard Setup tab.
 */
@Field.PickOne(value = DynamicJiraManagement2Attributes.AUTH_TYPE,
        values = {DynamicJiraManagement2Attributes.AUTH_TYPE_API_TOKEN, DynamicJiraManagement2Attributes.AUTH_TYPE_OAUTH},
        required = true)
@Field.Text(value = DynamicJiraManagement2Attributes.EMAIL, required = false,
        description = "Required for API Token mode: the Atlassian account email that owns the API token.")
@Field.Text(value = DynamicJiraManagement2Attributes.API_TOKEN, isSecured = true, required = false,
        description = "Required for API Token mode: an Atlassian API token generated at id.atlassian.com/manage-profile/security/api-tokens.")
@Field.Text(value = DynamicJiraManagement2Attributes.SITE_URL, required = false,
        description = "Required for API Token mode: your Jira Cloud site, e.g. https://yourcompany.atlassian.net")
@Field.Text(value = DynamicJiraManagement2Attributes.OAUTH_CLIENT_ID, required = false,
        description = "Required for OAuth mode: the Client ID of an Atlassian OAuth 2.0 (3LO) app.")
@Field.Text(value = DynamicJiraManagement2Attributes.OAUTH_CLIENT_SECRET, isSecured = true, required = false,
        description = "Required for OAuth mode: the Client Secret of the Atlassian OAuth 2.0 (3LO) app.")
@Field.Text(value = DynamicJiraManagement2Attributes.DEFAULT_ASSIGNEE_ACCOUNT_ID, required = false,
        description = "Optional: Jira accountId used as the default Assignee on new tickets when none is given and the project defines no default.")
@Extension(name = "Dynamic Jira Management2", version = "1.0.0",
        description = "Schema-driven Jira Cloud ticket management through natural language.")
@StaticResource(path = "docs", file = "docs")
@Java(version = Java.Version.JAVA_21)
public final class DynamicJiraManagement2Extension {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicJiraManagement2Extension.class);

    private final Invoker invoker;
    private final DynamicJiraManagement2Attributes attributes;
    private final JiraAttributeStore attributeStore;
    private final JiraApiClient apiClient;
    private final DynamicJiraManagement2RequestAuthenticator requestAuthenticator;

    @Inject
    public DynamicJiraManagement2Extension(Invoker invoker, DynamicJiraManagement2Attributes attributes,
                                            JiraAttributeStore attributeStore, JiraApiClient apiClient) {
        this.invoker = invoker;
        this.attributes = attributes;
        this.attributeStore = attributeStore;
        this.apiClient = apiClient;
        // Built with plain `new` - HK2 cannot bind the bare invokerId String this class needs.
        this.requestAuthenticator = new DynamicJiraManagement2RequestAuthenticator(
                attributeStore, attributes, invoker.getInvokerId());
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> connectionAttributes) throws IOException {
        DynamicJiraManagement2Attributes candidate = DynamicJiraManagement2Attributes.create(invoker, connectionAttributes);
        if (candidate.isOAuth()) {
            if (candidate.getOauthClientId().isBlank() || candidate.getOauthClientSecret().isBlank()) {
                throw new IllegalArgumentException("OAuth Client ID and OAuth Client Secret are required for OAuth mode.");
            }
            if (attributeStore.loadToken(invoker.getInvokerId()) == null) {
                String authContextId = attributeStore.save(candidate);
                throw new MustAuthorizeException("Jira authorization required. Please connect your Jira account.",
                        List.of(new NamedValuedField("userId", "Text", invoker.getInvokerId(), Map.of(), Map.of()),
                                new NamedValuedField("authContextId", "Text", authContextId, Map.of(), Map.of())));
            }
        } else {
            if (candidate.getEmail().isBlank() || candidate.getApiToken().isBlank() || candidate.getSiteUrl().isBlank()) {
                throw new IllegalArgumentException(
                        "Account Email, API Token, and Jira Site URL are all required for API Token mode.");
            }
        }
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() throws IOException {
        try {
            apiClient.get("/myself", Map.of());
        } catch (JiraApiException e) {
            LOGGER.error("Test Connection failed for invoker {}: {}", invoker.getInvokerId(), e.getMessage());
            throw new IOException(e.getMessage(), e);
        } catch (MustAuthorizeException e) {
            throw new IOException("Jira authorization is required. Please reconnect your Jira account via the Setup tab.", e);
        }
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        attributes.update(newAttributes);
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }
}
