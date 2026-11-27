package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProjectTest {

    @Test
    void mapsProjectWithLead() {
        JsonObject json = JsonParser.parseString("""
                {"id": "1", "key": "KE", "name": "Krista Extensions", "projectTypeKey": "software",
                 "style": "classic", "lead": {"displayName": "Carol Lead"}}
                """).getAsJsonObject();

        Project project = Project.fromJson(json);

        assertEquals("1", project.id);
        assertEquals("KE", project.key);
        assertEquals("Krista Extensions", project.name);
        assertEquals("software", project.projectTypeKey);
        assertEquals("classic", project.style);
        assertEquals("Carol Lead", project.leadDisplayName);
    }

    @Test
    void missingLeadDoesNotThrow() {
        JsonObject json = JsonParser.parseString("{\"id\":\"1\",\"key\":\"KE\",\"name\":\"Krista Extensions\"}")
                .getAsJsonObject();

        Project project = Project.fromJson(json);

        assertNull(project.leadDisplayName);
    }

    @Test
    void nullJsonProducesEmptyProject() {
        Project project = Project.fromJson(null);
        assertNull(project.key);
    }

    @Test
    void toFieldsPreservesInsertionOrder() {
        Project project = Project.fromJson(JsonParser.parseString("{\"id\":\"1\",\"key\":\"KE\",\"name\":\"N\"}").getAsJsonObject());
        var fields = project.toFields();
        assertEquals(java.util.List.of("Id", "Key", "Name", "Project Type Key", "Style", "Lead Display Name"),
                new java.util.ArrayList<>(fields.keySet()));
    }
}
