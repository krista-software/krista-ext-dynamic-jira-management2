package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import app.krista.extensions.util.KeyValueStore;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.DynamicJiraManagement2Attributes;
import com.google.gson.Gson;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * Persists two different things in the platform KeyValueStore, following the github/salesforce/
 * servicenow AttributeStore pattern:
 *  - a transient candidate {@link DynamicJiraManagement2Attributes} under a random "authContextId_*"
 *    key, carrying the Setup-tab form through the OAuth browser round-trip during validate-before-save;
 *  - a durable {@link JiraOAuthToken} per invoker, so the Graph-style access/refresh tokens acquired
 *    via the OAuth callback are visible to every later catalog request, not just the authenticator.
 *
 * KeyValueStore handles encryption at rest - no custom encryption layer here.
 */
@Service
public class JiraAttributeStore {

    private static final Logger LOG = LoggerFactory.getLogger(JiraAttributeStore.class);
    private static final Gson GSON = new Gson();
    private static final String TOKEN_KEY_PREFIX = "jiraOAuthToken_";

    private final KeyValueStore keyValueStore;

    @Inject
    public JiraAttributeStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /** Transient: random authContextId key, used only during the validate-before-save OAuth round-trip. */
    public String save(DynamicJiraManagement2Attributes attributes) {
        String key = "authContextId_" + UUID.randomUUID();
        keyValueStore.put(key, GSON.toJson(attributes.toMap()));
        return key;
    }

    @SuppressWarnings("unchecked")
    public DynamicJiraManagement2Attributes load(String key) {
        Object stored = keyValueStore.get(key);
        if (stored == null) {
            return null;
        }
        try {
            Map<String, Object> map = GSON.fromJson(String.valueOf(stored), Map.class);
            return DynamicJiraManagement2Attributes.create(map);
        } catch (Exception e) {
            LOG.error("Failed to deserialize candidate attributes for key {}", key, e);
            return null;
        }
    }

    public void remove(String key) {
        keyValueStore.remove(key);
    }

    /** Durable OAuth token, keyed by invoker id. */
    public void saveToken(String invokerId, JiraOAuthToken token) {
        keyValueStore.put(TOKEN_KEY_PREFIX + invokerId, GSON.toJson(token));
        LOG.info("Saved Jira OAuth token for invoker {} (cloudId {})", invokerId, token.cloudId);
    }

    public JiraOAuthToken loadToken(String invokerId) {
        Object stored = keyValueStore.get(TOKEN_KEY_PREFIX + invokerId);
        if (stored == null) {
            return null;
        }
        try {
            return GSON.fromJson(String.valueOf(stored), JiraOAuthToken.class);
        } catch (Exception e) {
            LOG.error("Failed to deserialize OAuth token for invoker {}", invokerId, e);
            return null;
        }
    }

    public void removeToken(String invokerId) {
        keyValueStore.remove(TOKEN_KEY_PREFIX + invokerId);
    }
}
