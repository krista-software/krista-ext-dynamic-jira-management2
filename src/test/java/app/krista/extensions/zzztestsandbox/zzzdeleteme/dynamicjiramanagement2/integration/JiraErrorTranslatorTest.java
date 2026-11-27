package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JiraErrorTranslatorTest {

    @Test
    void mapsEachKnownStatusToItsCategory() {
        assertEquals(JiraApiException.Category.VALIDATION, JiraErrorTranslator.translate(400, "{}").getCategory());
        assertEquals(JiraApiException.Category.VALIDATION, JiraErrorTranslator.translate(422, "{}").getCategory());
        assertEquals(JiraApiException.Category.AUTHENTICATION, JiraErrorTranslator.translate(401, "{}").getCategory());
        assertEquals(JiraApiException.Category.PERMISSION, JiraErrorTranslator.translate(403, "{}").getCategory());
        assertEquals(JiraApiException.Category.NOT_FOUND, JiraErrorTranslator.translate(404, "{}").getCategory());
        assertEquals(JiraApiException.Category.RATE_LIMITED, JiraErrorTranslator.translate(429, "{}").getCategory());
        assertEquals(JiraApiException.Category.UPSTREAM, JiraErrorTranslator.translate(503, "{}").getCategory());
        assertEquals(JiraApiException.Category.UNKNOWN, JiraErrorTranslator.translate(418, "{}").getCategory());
    }

    @Test
    void extractsErrorMessagesArrayIntoDetails() {
        String body = "{\"errorMessages\": [\"Issue does not exist\"]}";
        JiraApiException e = JiraErrorTranslator.translate(404, body);
        assertTrue(e.getMessage().contains("Issue does not exist"));
    }

    @Test
    void extractsFieldErrorsMapIntoDetails() {
        String body = "{\"errors\": {\"summary\": \"Summary is required.\"}}";
        JiraApiException e = JiraErrorTranslator.translate(400, body);
        assertTrue(e.getFieldErrors().stream().anyMatch(d -> d.contains("summary") && d.contains("Summary is required.")));
    }

    @Test
    void malformedBodyDoesNotThrow() {
        JiraApiException e = JiraErrorTranslator.translate(500, "not json at all {{{");
        assertEquals(JiraApiException.Category.UPSTREAM, e.getCategory());
    }

    @Test
    void blankBodyProducesNoDetailSuffix() {
        JiraApiException e = JiraErrorTranslator.translate(404, "");
        assertEquals("The requested Jira project, issue, or resource was not found", e.getMessage());
    }

    @Test
    void timeoutProducesTimeoutCategory() {
        JiraApiException e = JiraErrorTranslator.timeout(new java.io.IOException("boom"));
        assertEquals(JiraApiException.Category.TIMEOUT, e.getCategory());
    }
}
