package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import java.util.List;

/**
 * Structured error raised by {@link JiraApiClient} and the service layer - carries a user-facing
 * message and a category so Area methods can build meaningful, specific failure responses instead of
 * a generic "request failed".
 */
public class JiraApiException extends Exception {

    public enum Category {
        AUTHENTICATION,
        PERMISSION,
        NOT_FOUND,
        VALIDATION,
        RATE_LIMITED,
        TIMEOUT,
        UPSTREAM,
        UNKNOWN
    }

    private final Category category;
    private final List<String> fieldErrors;

    public JiraApiException(Category category, String message) {
        this(category, message, List.of());
    }

    public JiraApiException(Category category, String message, List<String> fieldErrors) {
        super(message);
        this.category = category;
        this.fieldErrors = fieldErrors == null ? List.of() : fieldErrors;
    }

    public JiraApiException(Category category, String message, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.fieldErrors = List.of();
    }

    public Category getCategory() {
        return category;
    }

    public List<String> getFieldErrors() {
        return fieldErrors;
    }
}
