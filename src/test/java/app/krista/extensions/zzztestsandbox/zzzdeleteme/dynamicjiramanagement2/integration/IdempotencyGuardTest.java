package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdempotencyGuardTest {

    @Test
    void returnsNullWhenNothingRemembered() {
        IdempotencyGuard guard = new IdempotencyGuard();
        String fp = IdempotencyGuard.fingerprint("inv1", "KE", "10001", "Summary", "Desc");
        assertNull(guard.findRecentDuplicate(fp));
    }

    @Test
    void returnsRememberedIssueKeyForIdenticalFingerprint() {
        IdempotencyGuard guard = new IdempotencyGuard();
        String fp = IdempotencyGuard.fingerprint("inv1", "KE", "10001", "Summary", "Desc");
        guard.remember(fp, "KE-123");

        assertEquals("KE-123", guard.findRecentDuplicate(fp));
    }

    @Test
    void fingerprintIsCaseInsensitiveOnSummaryAndDescription() {
        String fpLower = IdempotencyGuard.fingerprint("inv1", "KE", "10001", "fix login bug", "details");
        String fpUpper = IdempotencyGuard.fingerprint("inv1", "KE", "10001", "FIX LOGIN BUG", "DETAILS");
        assertEquals(fpLower, fpUpper);
    }

    @Test
    void differentInvokerProducesDifferentFingerprint() {
        String fp1 = IdempotencyGuard.fingerprint("inv1", "KE", "10001", "Summary", "Desc");
        String fp2 = IdempotencyGuard.fingerprint("inv2", "KE", "10001", "Summary", "Desc");
        org.junit.jupiter.api.Assertions.assertNotEquals(fp1, fp2);
    }
}
