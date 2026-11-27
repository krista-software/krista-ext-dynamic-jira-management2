package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.DynamicJiraManagement2Attributes;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for Jira Cloud REST API v3. Handles both authentication modes (API Token / OAuth),
 * base-URL selection, access-token refresh, retry/backoff on 429 and 5xx, and translation of non-2xx
 * responses into {@link JiraApiException}.
 */
@Service
public class JiraApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(JiraApiClient.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_ATTEMPTS = 3;

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final DynamicJiraManagement2Attributes attributes;
    private final JiraAttributeStore attributeStore;
    private final String invokerId;

    @Inject
    public JiraApiClient(DynamicJiraManagement2Attributes attributes, JiraAttributeStore attributeStore, Invoker invoker) {
        this.attributes = attributes;
        this.attributeStore = attributeStore;
        this.invokerId = invoker.getInvokerId();
    }

    public JsonElement get(String path, Map<String, String> query) throws JiraApiException, MustAuthorizeException {
        return execute("GET", path, query, null);
    }

    public JsonElement post(String path, JsonElement body) throws JiraApiException, MustAuthorizeException {
        return execute("POST", path, null, body);
    }

    public JsonElement put(String path, JsonElement body) throws JiraApiException, MustAuthorizeException {
        return execute("PUT", path, null, body);
    }

    private JsonElement execute(String method, String path, Map<String, String> query, JsonElement body)
            throws JiraApiException, MustAuthorizeException {
        String baseUrl = resolveBaseUrl();
        String authHeader = resolveAuthHeader();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (query != null) {
            query.forEach((k, v) -> {
                if (v != null) {
                    urlBuilder.addQueryParameter(k, v);
                }
            });
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", authHeader)
                .header("Accept", "application/json");

        RequestBody requestBody = null;
        if (body != null) {
            requestBody = RequestBody.create(body.toString(), JSON);
        }
        switch (method) {
            case "GET" -> requestBuilder.get();
            case "POST" -> requestBuilder.post(requestBody != null ? requestBody : RequestBody.create("", JSON));
            case "PUT" -> requestBuilder.put(requestBody != null ? requestBody : RequestBody.create("", JSON));
            default -> throw new IllegalArgumentException("Unsupported HTTP method " + method);
        }
        Request request = requestBuilder.build();

        JiraApiException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try (Response response = HTTP.newCall(request).execute()) {
                int status = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";
                if (status >= 200 && status < 300) {
                    if (responseBody.isBlank()) {
                        return com.google.gson.JsonNull.INSTANCE;
                    }
                    return JsonParser.parseString(responseBody);
                }
                if (status == 429 && attempt < MAX_ATTEMPTS) {
                    long waitMs = retryAfterMillis(response, attempt);
                    LOG.warn("Jira rate-limited invoker {} (attempt {}); waiting {}ms", invokerId, attempt, waitMs);
                    sleep(waitMs);
                    continue;
                }
                if (status >= 500 && attempt < MAX_ATTEMPTS) {
                    long waitMs = 500L * attempt;
                    LOG.warn("Jira returned {} for invoker {} (attempt {}); retrying in {}ms", status, invokerId, attempt, waitMs);
                    sleep(waitMs);
                    continue;
                }
                lastError = JiraErrorTranslator.translate(status, responseBody);
                LOG.error("Jira request failed: {} {} -> HTTP {} for invoker {}: {}", method, path, status, invokerId, lastError.getMessage());
                throw lastError;
            } catch (IOException timeoutOrNetwork) {
                if (attempt >= MAX_ATTEMPTS) {
                    LOG.error("Jira request timed out/network error for invoker {}: {} {}", invokerId, method, path, timeoutOrNetwork);
                    throw JiraErrorTranslator.timeout(timeoutOrNetwork);
                }
                sleep(300L * attempt);
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new JiraApiException(JiraApiException.Category.UNKNOWN, "Jira request failed after retries with no further detail.");
    }

    private long retryAfterMillis(Response response, int attempt) {
        String retryAfter = response.header("Retry-After");
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter.trim()) * 1000L;
            } catch (NumberFormatException ignored) {
                // fall through to default backoff
            }
        }
        return 1000L * attempt;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String resolveBaseUrl() throws JiraApiException, MustAuthorizeException {
        if (attributes.isOAuth()) {
            JiraOAuthToken token = ensureFreshOAuthToken();
            return "https://api.atlassian.com/ex/jira/" + token.cloudId + "/rest/api/3";
        }
        if (attributes.getSiteUrl().isBlank()) {
            throw new JiraApiException(JiraApiException.Category.AUTHENTICATION,
                    "Jira Site URL is not configured. Set it on the Setup tab under Your Own Credentials (API Token).");
        }
        return attributes.getSiteUrl() + "/rest/api/3";
    }

    private String resolveAuthHeader() throws JiraApiException, MustAuthorizeException {
        if (attributes.isOAuth()) {
            JiraOAuthToken token = ensureFreshOAuthToken();
            return "Bearer " + token.accessToken;
        }
        if (attributes.getEmail().isBlank() || attributes.getApiToken().isBlank()) {
            throw new JiraApiException(JiraApiException.Category.AUTHENTICATION,
                    "Account Email and API Token are not configured. Set them on the Setup tab under Your Own Credentials (API Token).");
        }
        return Credentials.basic(attributes.getEmail(), attributes.getApiToken());
    }

    private JiraOAuthToken ensureFreshOAuthToken() throws MustAuthorizeException {
        JiraOAuthToken token = attributeStore.loadToken(invokerId);
        if (token == null) {
            throw new MustAuthorizeException("Jira authorization required. Please connect your Jira account.",
                    List.of(new NamedValuedField("userId", "Text", invokerId, Map.of(), Map.of())));
        }
        if (token.isExpiringSoon()) {
            try {
                OAuth2AccessToken refreshed = attributes.getOAuth20Service().refreshAccessToken(token.refreshToken);
                token.accessToken = refreshed.getAccessToken();
                if (refreshed.getRefreshToken() != null) {
                    token.refreshToken = refreshed.getRefreshToken();
                }
                token.expiresAtEpochMillis = System.currentTimeMillis()
                        + (refreshed.getExpiresIn() != null ? refreshed.getExpiresIn() * 1000L : 3600_000L);
                attributeStore.saveToken(invokerId, token);
            } catch (Exception e) {
                LOG.error("Failed to refresh Jira OAuth token for invoker {}", invokerId, e);
                throw new MustAuthorizeException("Jira authorization expired and could not be refreshed. Please reconnect your Jira account.",
                        List.of(new NamedValuedField("userId", "Text", invokerId, Map.of(), Map.of())));
            }
        }
        return token;
    }

    /** Resolves the Jira Cloud site (cloudId + URL) reachable with a freshly-issued OAuth access token. */
    public JsonObject resolveAccessibleResource(String accessToken) throws JiraApiException {
        Request request = new Request.Builder()
                .url("https://api.atlassian.com/oauth/token/accessible-resources")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .get().build();
        try (Response response = HTTP.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw JiraErrorTranslator.translate(response.code(), responseBody);
            }
            JsonElement parsed = JsonParser.parseString(responseBody);
            if (!parsed.isJsonArray() || parsed.getAsJsonArray().isEmpty()) {
                throw new JiraApiException(JiraApiException.Category.NOT_FOUND,
                        "No accessible Jira Cloud site was returned for this account.");
            }
            return parsed.getAsJsonArray().get(0).getAsJsonObject();
        } catch (IOException e) {
            throw JiraErrorTranslator.timeout(e);
        }
    }
}
