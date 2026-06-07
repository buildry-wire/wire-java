package mn.wire;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Decodes the Wire {@code {"error": {...}}} envelope into a {@link WireException}. */
final class ErrorDecoder {

    private static final Gson GSON = new Gson();

    private ErrorDecoder() {
    }

    static WireException decode(int status, String body) {
        try {
            JsonObject root = GSON.fromJson(body, JsonObject.class);
            if (root != null && root.has("error") && root.get("error").isJsonObject()) {
                JsonObject e = root.getAsJsonObject("error");
                String message = str(e, "message");
                return WireException.builder()
                        .type(str(e, "type"))
                        .code(str(e, "code"))
                        .param(str(e, "param"))
                        .requestId(str(e, "request_id"))
                        .docUrl(str(e, "doc_url"))
                        .operatorDeclineCode(str(e, "operator_decline_code"))
                        .statusCode(status)
                        .build(message != null ? message : "request failed");
            }
        } catch (RuntimeException ignored) {
            // fall through to generic error
        }
        return WireException.builder()
                .type("api_error")
                .statusCode(status)
                .build("unexpected response (status " + status + ")");
    }

    private static String str(JsonObject o, String key) {
        if (o.has(key) && o.get(key).isJsonPrimitive()) {
            return o.get(key).getAsString();
        }
        return null;
    }
}
