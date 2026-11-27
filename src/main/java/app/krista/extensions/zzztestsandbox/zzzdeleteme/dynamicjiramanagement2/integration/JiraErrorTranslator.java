package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps a Jira REST API v3 HTTP error (status + body) to a {@link JiraApiException} with a clear,
 * user-facing message - never surfaces a raw stack trace or vendor payload to a catalog request
 * response.
 */
public final class JiraErrorTranslator {

    private JiraErrorTranslator() {
    }

    public static JiraApiException translate(int statusCode, String responseBody) {
        List<String> details = extractDetails(responseBody);
        String detailText = details.isEmpty() ? "" : " (" + String.join("; ", details) + ")";

        switch (statusCode) {
            case 400:
            case 422:
                return new JiraApiException(JiraApiException.Category.VALIDATION,
                        "Jira rejected the request as invalid" + detailText, details);
            case 401:
                return new JiraApiException(JiraApiException.Category.AUTHENTICATION,
                        "Jira authentication failed - the configured credentials were rejected" + detailText);
            case 403:
                return new JiraApiException(JiraApiException.Category.PERMISSION,
                        "The authenticated Jira user does not have permission to perform this action" + detailText);
            case 404:
                return new JiraApiException(JiraApiException.Category.NOT_FOUND,
                        "The requested Jira project, issue, or resource was not found" + detailText);
            case 429:
                return new JiraApiException(JiraApiException.Category.RATE_LIMITED,
                        "Jira rate-limited this request - retry after a short delay" + detailText);
            default:
                if (statusCode >= 500) {
                    return new JiraApiException(JiraApiException.Category.UPSTREAM,
                            "Jira returned a server error (HTTP " + statusCode + ")" + detailText);
                }
                return new JiraApiException(JiraApiException.Category.UNKNOWN,
                        "Jira returned an unexpected error (HTTP " + statusCode + ")" + detailText);
        }
    }

    public static JiraApiException timeout(Throwable cause) {
        return new JiraApiException(JiraApiException.Category.TIMEOUT,
                "Timed out waiting for Jira to respond", cause);
    }

    private static List<String> extractDetails(String responseBody) {
        List<String> details = new ArrayList<>();
        if (responseBody == null || responseBody.isBlank()) {
            return details;
        }
        try {
            JsonElement parsed = JsonParser.parseString(responseBody);
            if (!parsed.isJsonObject()) {
                return details;
            }
            JsonObject obj = parsed.getAsJsonObject();
            if (obj.has("errorMessages") && obj.get("errorMessages").isJsonArray()) {
                JsonArray arr = obj.getAsJsonArray("errorMessages");
                for (JsonElement e : arr) {
                    details.add(e.getAsString());
                }
            }
            if (obj.has("errors") && obj.get("errors").isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("errors").entrySet()) {
                    details.add(entry.getKey() + ": " + entry.getValue().getAsString());
                }
            }
        } catch (Exception ignored) {
            // Non-JSON or unexpected error body - fall back to no extracted details.
        }
        return details;
    }
}
