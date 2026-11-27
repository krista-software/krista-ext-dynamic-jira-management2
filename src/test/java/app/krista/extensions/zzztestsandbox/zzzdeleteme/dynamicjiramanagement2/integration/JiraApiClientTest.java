package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.DynamicJiraManagement2Attributes;
import com.google.gson.JsonElement;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JiraApiClientTest {

    private MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    private JiraApiClient buildClient() {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInvokerId()).thenReturn("invoker-1");

        DynamicJiraManagement2Attributes attributes = DynamicJiraManagement2Attributes.create(Map.of(
                DynamicJiraManagement2Attributes.AUTH_TYPE, DynamicJiraManagement2Attributes.AUTH_TYPE_API_TOKEN,
                DynamicJiraManagement2Attributes.EMAIL, "user@example.com",
                DynamicJiraManagement2Attributes.API_TOKEN, "dummy-token",
                DynamicJiraManagement2Attributes.SITE_URL, server.url("").toString().replaceAll("/$", "")));

        JiraAttributeStore attributeStore = mock(JiraAttributeStore.class);
        return new JiraApiClient(attributes, attributeStore, invoker);
    }

    @Test
    void getSendsBasicAuthAndParsesJsonResponse() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"accountId\":\"acc-1\"}"));
        JiraApiClient client = buildClient();

        JsonElement result = client.get("/myself", Map.of());

        assertEquals("acc-1", result.getAsJsonObject().get("accountId").getAsString());
        RecordedRequest recorded = server.takeRequest();
        assertTrue(recorded.getHeader("Authorization").startsWith("Basic "));
        assertEquals("/rest/api/3/myself", recorded.getPath());
    }

    @Test
    void notFoundTranslatesToJiraApiExceptionWithCategory() {
        server.enqueue(new MockResponse().setResponseCode(404)
                .setBody("{\"errorMessages\": [\"Issue does not exist\"]}"));
        JiraApiClient client = buildClient();

        JiraApiException e = assertThrows(JiraApiException.class, () -> client.get("/issue/KE-999", Map.of()));
        assertEquals(JiraApiException.Category.NOT_FOUND, e.getCategory());
        assertTrue(e.getMessage().contains("Issue does not exist"));
    }

    @Test
    void retriesOnRateLimitThenSucceeds() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"ok\":true}"));
        JiraApiClient client = buildClient();

        JsonElement result = client.get("/myself", Map.of());

        assertTrue(result.getAsJsonObject().get("ok").getAsBoolean());
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void emptyBodyOn204ReturnsJsonNull() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));
        JiraApiClient client = buildClient();

        JsonElement result = client.put("/issue/KE-1", null);

        assertTrue(result.isJsonNull());
    }

    @Test
    void missingCredentialsThrowsAuthenticationCategoryWithoutCallingServer() throws MustAuthorizeException {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInvokerId()).thenReturn("invoker-2");
        DynamicJiraManagement2Attributes attributes = DynamicJiraManagement2Attributes.create(Map.of(
                DynamicJiraManagement2Attributes.AUTH_TYPE, DynamicJiraManagement2Attributes.AUTH_TYPE_API_TOKEN));
        JiraAttributeStore attributeStore = mock(JiraAttributeStore.class);
        JiraApiClient client = new JiraApiClient(attributes, attributeStore, invoker);

        JiraApiException e = assertThrows(JiraApiException.class, () -> client.get("/myself", Map.of()));
        assertEquals(JiraApiException.Category.AUTHENTICATION, e.getCategory());
        assertEquals(0, server.getRequestCount());
    }
}
