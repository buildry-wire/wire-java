package mn.wire;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parameters for updating a WebhookEndpoint. */
public class WebhookEndpointUpdateParams {
    private final Map<String, Object> body = new LinkedHashMap<>();
    private String idempotencyKey;

    public static WebhookEndpointUpdateParams create() {
        return new WebhookEndpointUpdateParams();
    }

    public WebhookEndpointUpdateParams url(String url) {
        body.put("url", url);
        return this;
    }

    public WebhookEndpointUpdateParams enabledEvents(List<String> events) {
        body.put("enabled_events", events);
        return this;
    }

    public WebhookEndpointUpdateParams status(String status) {
        body.put("status", status);
        return this;
    }

    public WebhookEndpointUpdateParams idempotencyKey(String key) {
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
