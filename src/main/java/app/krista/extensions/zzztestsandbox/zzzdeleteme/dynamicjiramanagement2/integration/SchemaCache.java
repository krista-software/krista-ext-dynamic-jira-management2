package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * In-memory cache of discovered field schemas, keyed by "Project Key + Issue Type" (or by issue key
 * for the edit-meta variant), with a TTL and manual-refresh support - per the requirement's "cache
 * schemas using Project Key + Issue Type, with expiry and refresh mechanisms".
 *
 * Scoped to the JVM process (shared across invokers deliberately: a project+issue-type's field
 * configuration is tenant-wide, not per-user, so sharing the cache avoids redundant lookups).
 */
@Service
public class SchemaCache {

    private static final long DEFAULT_TTL_MILLIS = 10L * 60 * 1000; // 10 minutes

    private static final class Entry {
        final List<FieldSchemaDto> value;
        final long expiresAt;

        Entry(List<FieldSchemaDto> value, long ttlMillis) {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    public static String createIssueKey(String invokerId, String projectKey, String issueTypeId) {
        return "create|" + invokerId + "|" + projectKey + "|" + issueTypeId;
    }

    public static String editIssueKey(String invokerId, String issueKey) {
        return "edit|" + invokerId + "|" + issueKey;
    }

    /** Returns the cached schema, or computes and caches it via the loader if missing/expired. */
    public List<FieldSchemaDto> getOrLoad(String key, Supplier<List<FieldSchemaDto>> loader) {
        Entry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.value;
        }
        List<FieldSchemaDto> loaded = loader.get();
        if (!loaded.isEmpty()) {
            // Don't cache an empty result - it's far more likely a transient fetch error than a
            // genuinely field-less issue type, and caching it would hide the real error for the TTL.
            cache.put(key, new Entry(loaded, DEFAULT_TTL_MILLIS));
        }
        return loaded;
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public void invalidateAll() {
        cache.clear();
    }
}
