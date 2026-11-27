package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.api;

import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.DynamicJiraManagement2Attributes;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiClient;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraAttributeStore;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraOAuthToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * OAuth2 callback resource (Pattern C, secondary auth option). Exchanges the authorization code for
 * access/refresh tokens, resolves the Jira Cloud site (cloudId) reachable with that token, persists
 * the token record, then notifies the platform the account is authorized.
 *
 * `state` is "{@code <userId>}" or "{@code <userId>#<authContextId>}" (see
 * DynamicJiraManagement2RequestAuthenticator) - the authContextId form is used the first time an
 * admin switches AuthType to OAuth on the Setup tab and saves (validate-before-save), so the
 * not-yet-persisted candidate client id/secret are still available during the browser round-trip.
 */
@Path("/")
public final class JiraAuthCallbackResource {

    private static final Logger LOG = LoggerFactory.getLogger(JiraAuthCallbackResource.class);

    private final AuthorizationListener authorizationListener;
    private final JiraAttributeStore attributeStore;
    private final JiraApiClient apiClient;
    private final DynamicJiraManagement2Attributes defaultAttributes;

    @Inject
    public JiraAuthCallbackResource(AuthorizationListener authorizationListener,
                                     JiraAttributeStore attributeStore,
                                     JiraApiClient apiClient,
                                     DynamicJiraManagement2Attributes defaultAttributes) {
        this.authorizationListener = authorizationListener;
        this.attributeStore = attributeStore;
        this.apiClient = apiClient;
        this.defaultAttributes = defaultAttributes;
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML)
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state) {
        try {
            String[] parts = state == null ? new String[0] : state.split("#");
            String userId = parts.length > 0 ? parts[0] : null;
            String authContextId = parts.length > 1 ? parts[1] : null;

            DynamicJiraManagement2Attributes effectiveAttributes = authContextId != null
                    ? attributeStore.load(authContextId)
                    : defaultAttributes;
            if (effectiveAttributes == null) {
                LOG.error("No candidate Jira attributes found for authContextId {}", authContextId);
                return Response.ok("Authorization failed: the configuration session expired. Please try again.").build();
            }

            OAuth20Service oAuth20Service = effectiveAttributes.getOAuth20Service();
            OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);

            JsonObject site = apiClient.resolveAccessibleResource(accessToken.getAccessToken());

            JiraOAuthToken token = new JiraOAuthToken();
            token.accessToken = accessToken.getAccessToken();
            token.refreshToken = accessToken.getRefreshToken();
            token.expiresAtEpochMillis = System.currentTimeMillis()
                    + (accessToken.getExpiresIn() != null ? accessToken.getExpiresIn() * 1000L : 3600_000L);
            token.cloudId = site.get("id").getAsString();
            token.siteUrl = site.has("url") ? site.get("url").getAsString() : "";

            attributeStore.saveToken(userId, token);
            if (authContextId != null) {
                attributeStore.remove(authContextId);
            }

            authorizationListener.authorized(userId);
            return Response.ok("Authorization complete. You may close this window.").build();
        } catch (Exception e) {
            LOG.error("Jira OAuth callback failed", e);
            return Response.ok("Authorization failed: " + e.getMessage()).build();
        }
    }
}
