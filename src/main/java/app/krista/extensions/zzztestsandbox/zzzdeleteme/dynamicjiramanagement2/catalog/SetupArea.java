package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiClient;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/** Standard connectivity area (named exactly "Setup" per the registered domain taxonomy). */
@Domain(id = "catEntryDomain_a11bb761-5f94-4d6f-86d5-d77e9cb47046",
        name = "ZZZ Delete Me",
        ecosystemId = "catEntryEcosystem_7b848154-eb50-4796-a8c3-4fa51c868c61",
        ecosystemName = "ZZZ Test Sandbox",
        ecosystemVersion = "0f4d9955-c318-422c-9332-d2eb1a9854c3")
public class SetupArea {

    private static final Logger LOG = LoggerFactory.getLogger(SetupArea.class);

    private final JiraApiClient client;
    private final String invokerId;

    @Inject
    public SetupArea(JiraApiClient client, Invoker invoker) {
        this.client = client;
        this.invokerId = invoker.getInvokerId();
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0001-4a00-8000-000000000001",
            name = "Test Connection",
            description = "Validate connectivity and authentication against Jira Cloud (GET /myself).",
            area = "SetUp",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse testConnection() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            client.get("/myself", Map.of());
            out.put("Success", true);
            out.put("Message", "Connected to Jira.");
        } catch (JiraApiException e) {
            LOG.error("Test connection failed for invoker {}: {}", invokerId, e.getMessage());
            out.put("Success", false);
            out.put("Message", e.getMessage());
        } catch (MustAuthorizeException e) {
            out.put("Success", false);
            out.put("Message", "Jira authorization is required. Please connect your Jira account via the Setup tab.");
        }
        return new ExtensionResponseBuilder().success(out).build();
    }

    @CatalogRequest(
            id = "localDomainRequest_7f3a9c10-0002-4a00-8000-000000000002",
            name = "Health Check",
            description = "Report extension JVM health and a lightweight Jira connectivity check.",
            area = "SetUp",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Is Healthy", required = false)
    @Field(name = "Heap Used MB", type = "Number", required = false)
    @Field.Text(name = "Details", required = false)
    public ExtensionResponse healthCheck() {
        Map<String, Object> out = new LinkedHashMap<>();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long heapUsedMb = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        boolean healthy;
        String details;
        try {
            client.get("/myself", Map.of());
            healthy = true;
            details = "OK";
        } catch (JiraApiException e) {
            healthy = false;
            details = e.getMessage();
        } catch (MustAuthorizeException e) {
            healthy = false;
            details = "Jira authorization is required.";
        }
        out.put("Is Healthy", healthy);
        out.put("Heap Used MB", (double) heapUsedMb);
        out.put("Details", details);
        return new ExtensionResponseBuilder().success(out).build();
    }
}
