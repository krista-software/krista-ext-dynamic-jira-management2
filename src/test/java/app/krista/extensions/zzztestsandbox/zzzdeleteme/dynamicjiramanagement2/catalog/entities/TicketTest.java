package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketTest {

    private static final String SAMPLE_ISSUE = """
            {
              "id": "10001",
              "key": "KE-5",
              "fields": {
                "summary": "Login fails on mobile",
                "description": {"type": "doc", "version": 1, "content": [
                    {"type": "paragraph", "content": [{"type": "text", "text": "Steps to reproduce..."}]}
                ]},
                "status": {"name": "In Progress", "statusCategory": {"key": "indeterminate"}},
                "priority": {"id": "2", "name": "High"},
                "issuetype": {"id": "10000", "name": "Bug"},
                "project": {"key": "KE", "name": "Krista Extensions"},
                "reporter": {"accountId": "acc-1", "displayName": "Alice Reporter"},
                "assignee": {"accountId": "acc-2", "displayName": "Bob Assignee"},
                "labels": ["urgent", "mobile"],
                "components": [{"id": "100", "name": "Mobile App"}],
                "duedate": "2026-01-15",
                "created": "2026-01-01T10:00:00.000+0000",
                "updated": "2026-01-02T11:30:00.000+0000",
                "resolution": null
              }
            }
            """;

    @Test
    void mapsCoreFieldsFromRealisticIssuePayload() {
        JsonObject json = JsonParser.parseString(SAMPLE_ISSUE).getAsJsonObject();

        Ticket ticket = Ticket.fromJson(json);

        assertEquals("10001", ticket.id);
        assertEquals("KE-5", ticket.key);
        assertEquals("Login fails on mobile", ticket.summary);
        assertTrue(ticket.description.contains("Steps to reproduce"));
        assertEquals("In Progress", ticket.statusName);
        assertEquals("High", ticket.priorityName);
        assertEquals("Bug", ticket.issueTypeName);
        assertEquals("KE", ticket.projectKey);
        assertEquals("Alice Reporter", ticket.reporterDisplayName);
        assertEquals("acc-1", ticket.reporterAccountId);
        assertEquals("Bob Assignee", ticket.assigneeDisplayName);
        assertEquals("acc-2", ticket.assigneeAccountId);
        assertEquals(2, ticket.labels.size());
        assertEquals(1, ticket.components.size());
        assertEquals("Mobile App", ticket.components.get(0).get("Name"));
        assertNull(ticket.resolutionName);
        assertTrue(ticket.created < ticket.updated);
    }

    @Test
    void nullJsonProducesEmptyTicketNotNullPointer() {
        Ticket ticket = Ticket.fromJson(null);
        assertNull(ticket.key);
    }

    @Test
    void missingOptionalNestedObjectsDoNotThrow() {
        JsonObject json = JsonParser.parseString("{\"id\":\"1\",\"key\":\"KE-1\",\"fields\":{\"summary\":\"Bare\"}}")
                .getAsJsonObject();

        Ticket ticket = Ticket.fromJson(json);

        assertEquals("Bare", ticket.summary);
        assertNull(ticket.assigneeDisplayName);
        assertTrue(ticket.labels.isEmpty());
        assertTrue(ticket.components.isEmpty());
    }
}
