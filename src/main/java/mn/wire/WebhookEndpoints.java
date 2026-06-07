package mn.wire;

/** Operations on WebhookEndpoints. */
public final class WebhookEndpoints {

    private final Wire client;

    WebhookEndpoints(Wire client) {
        this.client = client;
    }

    public WebhookEndpoint create(WebhookEndpointCreateParams params) {
        Wire.RequestOptions opts = new Wire.RequestOptions();
        opts.body = params.body();
        opts.idempotencyKey = params.idempotencyKey();
        return client.request("POST", "/v1/webhook_endpoints", opts, WebhookEndpoint.class);
    }

    public WebhookEndpoint retrieve(String id) {
        return client.request("GET", "/v1/webhook_endpoints/" + Util.pathSegment(id), null, WebhookEndpoint.class);
    }

    public WebhookEndpoint update(String id, WebhookEndpointUpdateParams params) {
        Wire.RequestOptions opts = new Wire.RequestOptions();
        opts.body = params.body();
        opts.idempotencyKey = params.idempotencyKey();
        return client.request("POST", "/v1/webhook_endpoints/" + Util.pathSegment(id), opts, WebhookEndpoint.class);
    }

    public Deleted delete(String id) {
        return client.request("DELETE", "/v1/webhook_endpoints/" + Util.pathSegment(id), null, Deleted.class);
    }

    public AutoPagingIterable<WebhookEndpoint> list() {
        return list(null);
    }

    public AutoPagingIterable<WebhookEndpoint> list(ListParams params) {
        return new AutoPagingIterable<>(client, "/v1/webhook_endpoints", Wire.listType(WebhookEndpoint.class), params);
    }
}
