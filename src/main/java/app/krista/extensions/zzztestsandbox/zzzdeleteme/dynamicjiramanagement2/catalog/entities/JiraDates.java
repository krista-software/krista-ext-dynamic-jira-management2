package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.catalog.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Parses Jira's ISO-8601 date/date-time strings into epoch millis for Krista's @Field.Date (Long). */
final class JiraDates {

    // Jira's own date-times use a non-colon numeric offset ("2024-01-15T10:30:00.000+0000"), which
    // ISO_OFFSET_DATE_TIME (colon-separated offset) does not accept - so that's tried first, with the
    // stricter ISO formatter as a fallback for any endpoint that does return a colon-separated offset.
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    private JiraDates() {
    }

    /** Parses either a full Jira date-time ("2024-01-15T10:30:00.000+0000") or a bare date ("2024-01-15"). */
    static Long parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return Instant.from(formatter.parse(raw)).toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // try the next formatter
            }
        }
        try {
            return LocalDate.parse(raw).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
