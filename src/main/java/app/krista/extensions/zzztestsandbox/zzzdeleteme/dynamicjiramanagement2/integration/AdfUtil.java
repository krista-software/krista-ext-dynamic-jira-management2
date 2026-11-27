package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Builds and reads the Atlassian Document Format (ADF) that Jira REST API v3 requires for rich-text
 * fields (issue description, comment body). Only plain-text paragraphs are produced/read - sufficient
 * for natural-language-driven summaries/comments; Jira itself still renders the result normally.
 */
public final class AdfUtil {

    private AdfUtil() {
    }

    /** Wraps plain text as a single-paragraph ADF document. Blank/null text becomes an empty doc. */
    public static JsonObject plainTextToAdf(String text) {
        JsonObject doc = new JsonObject();
        doc.addProperty("type", "doc");
        doc.addProperty("version", 1);
        JsonArray content = new JsonArray();
        if (text != null && !text.isBlank()) {
            JsonObject paragraph = new JsonObject();
            paragraph.addProperty("type", "paragraph");
            JsonArray paragraphContent = new JsonArray();
            JsonObject textNode = new JsonObject();
            textNode.addProperty("type", "text");
            textNode.addProperty("text", text);
            paragraphContent.add(textNode);
            paragraph.add("content", paragraphContent);
            content.add(paragraph);
        }
        doc.add("content", content);
        return doc;
    }

    /** Flattens an ADF document (or any nested node) back to plain text, concatenating text nodes. */
    public static String adfToPlainText(JsonElement adf) {
        if (adf == null || adf.isJsonNull()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        flatten(adf, out);
        return out.toString().strip();
    }

    private static void flatten(JsonElement node, StringBuilder out) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            for (JsonElement child : node.getAsJsonArray()) {
                flatten(child, out);
            }
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        JsonObject obj = node.getAsJsonObject();
        String type = obj.has("type") ? obj.get("type").getAsString() : "";
        if ("text".equals(type) && obj.has("text")) {
            out.append(obj.get("text").getAsString());
        }
        if (obj.has("content")) {
            flatten(obj.get("content"), out);
        }
        if ("paragraph".equals(type) || "heading".equals(type)) {
            out.append('\n');
        }
    }
}
