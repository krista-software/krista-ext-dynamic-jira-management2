package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import org.jvnet.hk2.annotations.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents duplicate ticket creation when a Create Ticket call is retried (network blip, workflow
 * re-run) with the same inputs: a short-lived, in-memory fingerprint cache mapping
 * (invoker + project + issue type + summary + description) to the issue key Jira already returned.
 */
@Service
public class IdempotencyGuard {

    private static final long WINDOW_MILLIS = 60_000; // 1 minute

    private static final class Entry {
        final String issueKey;
        final long expiresAt;

        Entry(String issueKey) {
            this.issueKey = issueKey;
            this.expiresAt = System.currentTimeMillis() + WINDOW_MILLIS;
        }
    }

    private final Map<String, Entry> recentCreates = new ConcurrentHashMap<>();

    public static String fingerprint(String invokerId, String projectKey, String issueTypeId,
                                      String summary, String description) {
        return invokerId + '|' + projectKey + '|' + issueTypeId + '|'
                + String.valueOf(summary).trim().toLowerCase()
                + '|' + String.valueOf(description).trim().toLowerCase();
    }

    /** Returns the issue key from a prior, still-fresh identical create, or null if none. */
    public String findRecentDuplicate(String fingerprint) {
        Entry entry = recentCreates.get(fingerprint);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expiresAt) {
            recentCreates.remove(fingerprint);
            return null;
        }
        return entry.issueKey;
    }

    public void remember(String fingerprint, String issueKey) {
        recentCreates.put(fingerprint, new Entry(issueKey));
    }
}
