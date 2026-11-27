package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdfUtilTest {

    @Test
    void plainTextBecomesSingleParagraphDocument() {
        JsonObject adf = AdfUtil.plainTextToAdf("Hello world");

        assertEquals("doc", adf.get("type").getAsString());
        assertEquals(1, adf.get("version").getAsInt());
        assertEquals(1, adf.getAsJsonArray("content").size());
        JsonObject paragraph = adf.getAsJsonArray("content").get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
        assertEquals("Hello world", paragraph.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString());
    }

    @Test
    void blankTextProducesEmptyContentArray() {
        JsonObject adf = AdfUtil.plainTextToAdf("  ");
        assertTrue(adf.getAsJsonArray("content").isEmpty());
    }

    @Test
    void nullTextProducesEmptyContentArray() {
        JsonObject adf = AdfUtil.plainTextToAdf(null);
        assertTrue(adf.getAsJsonArray("content").isEmpty());
    }

    @Test
    void roundTripsPlainTextThroughAdf() {
        JsonObject adf = AdfUtil.plainTextToAdf("Round trip text");
        String recovered = AdfUtil.adfToPlainText(adf);
        assertEquals("Round trip text", recovered);
    }

    @Test
    void nullAdfBecomesEmptyString() {
        assertEquals("", AdfUtil.adfToPlainText(null));
    }

    @Test
    void extractsTextAcrossMultipleParagraphs() {
        JsonObject first = AdfUtil.plainTextToAdf("First");
        JsonObject multi = new JsonObject();
        multi.addProperty("type", "doc");
        multi.addProperty("version", 1);
        com.google.gson.JsonArray content = new com.google.gson.JsonArray();
        content.addAll(first.getAsJsonArray("content"));
        content.addAll(AdfUtil.plainTextToAdf("Second").getAsJsonArray("content"));
        multi.add("content", content);

        String recovered = AdfUtil.adfToPlainText(multi);

        assertTrue(recovered.contains("First"));
        assertTrue(recovered.contains("Second"));
    }
}
