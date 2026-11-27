package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.authentication.CallbackUrls;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.AtlassianOAuthApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Connection configuration for Dynamic Jira Management2.
 *
 * Supports two admin-selectable authentication modes (AuthType), per the user's confirmed choice of
 * both "API Token" and "OAuth 2.0": ApiToken (HTTP Basic over email:apiToken, direct to the tenant's
 * own site) and OAuth (3-legged, routed through api.atlassian.com/ex/jira/{cloudId}). Only the fields
 * for the selected mode are required; validated in the extension's VALIDATE_ATTRIBUTES hook.
 */
@Service
public class DynamicJiraManagement2Attributes {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicJiraManagement2Attributes.class);

    public static final String AUTH_TYPE = "Authentication Method";
    public static final String EMAIL = "Account Email";
    public static final String API_TOKEN = "API Token";
    public static final String SITE_URL = "Jira Site URL";
    public static final String OAUTH_CLIENT_ID = "OAuth Client ID";
    public static final String OAUTH_CLIENT_SECRET = "OAuth Client Secret";
    public static final String DEFAULT_ASSIGNEE_ACCOUNT_ID = "Default Assignee Account ID";
    private static final String ROUTING_URL = "__RoutingUrl";

    public static final String AUTH_TYPE_API_TOKEN = "ApiToken";
    public static final String AUTH_TYPE_OAUTH = "OAuth";

    /** JAX-RS application path hosting the OAuth callback (Pattern C). jaxrsId stays the platform default "rest". */
    public static final String APP_PATH = "jiraoauth";

    private String authType;
    private String email;
    private String apiToken;
    private String siteUrl;
    private String oauthClientId;
    private String oauthClientSecret;
    private String defaultAssigneeAccountId;
    private String routingUrl;

    @Inject
    public DynamicJiraManagement2Attributes(Invoker invoker) {
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        update(invoker.getAttributes());
    }

    public DynamicJiraManagement2Attributes(Map<String, Object> attributes) {
        update(attributes);
    }

    public static DynamicJiraManagement2Attributes create(Invoker invoker, Map<String, Object> attributes) {
        DynamicJiraManagement2Attributes result = new DynamicJiraManagement2Attributes(invoker);
        result.update(attributes);
        return result;
    }

    public static DynamicJiraManagement2Attributes create(Map<String, Object> attributes) {
        return new DynamicJiraManagement2Attributes(attributes);
    }

    public void update(Map<String, Object> attributes) {
        this.authType = getString(attributes, AUTH_TYPE, AUTH_TYPE_API_TOKEN);
        this.email = getString(attributes, EMAIL, "");
        this.apiToken = getString(attributes, API_TOKEN, "");
        this.siteUrl = stripTrailingSlash(getString(attributes, SITE_URL, ""));
        this.oauthClientId = getString(attributes, OAUTH_CLIENT_ID, "");
        this.oauthClientSecret = getString(attributes, OAUTH_CLIENT_SECRET, "");
        this.defaultAssigneeAccountId = getString(attributes, DEFAULT_ASSIGNEE_ACCOUNT_ID, "");
        String storedRoutingUrl = getString(attributes, ROUTING_URL, "");
        if (!storedRoutingUrl.isBlank()) {
            this.routingUrl = storedRoutingUrl;
        }
        LOGGER.info("Dynamic Jira Management2 attributes updated - AuthType: {}, Site URL: {}", authType, siteUrl);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AUTH_TYPE, authType);
        map.put(EMAIL, email);
        map.put(API_TOKEN, apiToken);
        map.put(SITE_URL, siteUrl);
        map.put(OAUTH_CLIENT_ID, oauthClientId);
        map.put(OAUTH_CLIENT_SECRET, oauthClientSecret);
        map.put(DEFAULT_ASSIGNEE_ACCOUNT_ID, defaultAssigneeAccountId);
        map.put(ROUTING_URL, routingUrl);
        return map;
    }

    private String getString(Map<String, Object> attributes, String key, String defaultValue) {
        Object value = attributes == null ? null : attributes.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String stripTrailingSlash(String s) {
        return s == null ? "" : s.replaceAll("/+$", "");
    }

    public boolean isOAuth() {
        return AUTH_TYPE_OAUTH.equalsIgnoreCase(authType);
    }

    public OAuth20Service getOAuth20Service() {
        String callbackUrl = CallbackUrls.callbackUrl(routingUrl, null, APP_PATH);
        return new ServiceBuilder(oauthClientId)
                .apiSecret(oauthClientSecret)
                .callback(callbackUrl)
                .defaultScope("read:jira-work write:jira-work read:jira-user offline_access")
                .build(AtlassianOAuthApi.instance());
    }

    public String getAuthType() { return authType; }
    public String getEmail() { return email; }
    public String getApiToken() { return apiToken; }
    public String getSiteUrl() { return siteUrl; }
    public String getOauthClientId() { return oauthClientId; }
    public String getOauthClientSecret() { return oauthClientSecret; }
    public String getDefaultAssigneeAccountId() { return defaultAssigneeAccountId; }
    public String getRoutingUrl() { return routingUrl; }
}
