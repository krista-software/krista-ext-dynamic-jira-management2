package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaCacheTest {

    @Test
    void secondCallWithinTtlDoesNotReloadFromSupplier() {
        SchemaCache cache = new SchemaCache();
        AtomicInteger loadCount = new AtomicInteger();
        String key = SchemaCache.createIssueKey("inv1", "KE", "10001");

        cache.getOrLoad(key, () -> { loadCount.incrementAndGet(); return List.of(sampleField()); });
        cache.getOrLoad(key, () -> { loadCount.incrementAndGet(); return List.of(sampleField()); });

        assertEquals(1, loadCount.get());
    }

    @Test
    void invalidateForcesReload() {
        SchemaCache cache = new SchemaCache();
        AtomicInteger loadCount = new AtomicInteger();
        String key = SchemaCache.createIssueKey("inv1", "KE", "10001");

        cache.getOrLoad(key, () -> { loadCount.incrementAndGet(); return List.of(sampleField()); });
        cache.invalidate(key);
        cache.getOrLoad(key, () -> { loadCount.incrementAndGet(); return List.of(sampleField()); });

        assertEquals(2, loadCount.get());
    }

    @Test
    void emptyLoadResultIsNotCachedSoNextCallRetries() {
        SchemaCache cache = new SchemaCache();
        AtomicInteger loadCount = new AtomicInteger();
        String key = SchemaCache.createIssueKey("inv1", "KE", "10001");

        cache.getOrLoad(key, () -> { loadCount.incrementAndGet(); return List.of(); });
        cache.getOrLoad(key, () -> { loadCount.incrementAndGet(); return List.of(); });

        assertEquals(2, loadCount.get());
    }

    @Test
    void createAndEditKeysAreDistinctForSameInvoker() {
        String createKey = SchemaCache.createIssueKey("inv1", "KE", "10001");
        String editKey = SchemaCache.editIssueKey("inv1", "KE-5");
        org.junit.jupiter.api.Assertions.assertNotEquals(createKey, editKey);
    }

    private FieldSchemaDto sampleField() {
        FieldSchemaDto dto = new FieldSchemaDto();
        dto.fieldId = "summary";
        dto.name = "Summary";
        dto.required = true;
        return dto;
    }
}
