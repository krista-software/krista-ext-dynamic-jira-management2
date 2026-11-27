package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

/**
 * Durable OAuth token record for one invoker, persisted via {@link JiraAttributeStore}. Plain
 * Gson-serializable value type - no Krista annotations.
 */
public class JiraOAuthToken {

    public String accessToken;
    public String refreshToken;
    public long expiresAtEpochMillis;
    public String cloudId;
    public String siteUrl;

    public boolean isExpiringSoon() {
        return System.currentTimeMillis() > (expiresAtEpochMillis - 60_000L);
    }
}
