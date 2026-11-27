package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * ScribeJava descriptor for Atlassian's OAuth 2.0 (3LO) endpoints, used by the OAuth authentication
 * option (Krista Pattern C). Atlassian Cloud OAuth is not tenant-specific: both the authorize and
 * token endpoints are fixed, global URLs; the resulting token is then used against
 * https://api.atlassian.com/ex/jira/{cloudId}/rest/api/3/... after resolving cloudId via
 * accessible-resources.
 */
public final class AtlassianOAuthApi extends DefaultApi20 {

    private static final AtlassianOAuthApi INSTANCE = new AtlassianOAuthApi();

    private AtlassianOAuthApi() {
    }

    public static AtlassianOAuthApi instance() {
        return INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://auth.atlassian.com/oauth/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://auth.atlassian.com/authorize";
    }
}
