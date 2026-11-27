package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.authentication;

/**
 * Builds the OAuth redirect URI (the value passed to ScribeJava's `.callback(...)` and registered
 * with Atlassian). This is the only place the extension's jaxrsId matters.
 *
 *   jaxrsId set on @Extension  ->  <routingUrl>/<jaxrsId>/<applicationPath>/callback
 *   jaxrsId NOT set            ->  <routingUrl>/rest/<applicationPath>/callback   ("rest" is the platform default)
 */
public final class CallbackUrls {

    public static final String DEFAULT_JAXRS_ID = "rest";

    private CallbackUrls() {
    }

    public static String callbackUrl(String routingUrl, String jaxrsId, String applicationPath) {
        StringBuilder url = new StringBuilder(trimSlashes(routingUrl == null ? "" : routingUrl, false));
        url.append('/').append(isBlank(jaxrsId) ? DEFAULT_JAXRS_ID : trimSlashes(jaxrsId, true));
        String appPath = applicationPath == null ? "" : trimSlashes(applicationPath, true);
        if (!appPath.isEmpty()) {
            url.append('/').append(appPath);
        }
        return url.append("/callback").toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String trimSlashes(String s, boolean both) {
        String result = s.trim().replaceAll("/+$", "");
        return both ? result.replaceAll("^/+", "") : result;
    }
}
