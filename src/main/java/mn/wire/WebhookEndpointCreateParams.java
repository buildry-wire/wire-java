package mn.wire;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parameters for creating a WebhookEndpoint. */
public class WebhookEndpointCreateParams {
    private final Map<String, Object> body = new LinkedHashMap<>();
    private String idempotencyKey;

    public static WebhookEndpointCreateParams create() {
        return new WebhookEndpointCreateParams();
    }

    public WebhookEndpointCreateParams url(String url) {
        body.put("url", url);
        return this;
    }

    public WebhookEndpointCreateParams enabledEvents(List<String> events) {
        body.put("enabled_events", events);
        return this;
    }

    public WebhookEndpointCreateParams idempotencyKey(String key) {
        this.idempotencyKey = key;
        return this;
    }

    Map<String, Object> body() {
        return body;
    }

    String idempotencyKey() {
        return idempotencyKey;
    }
}
