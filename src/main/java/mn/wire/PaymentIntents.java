package mn.wire;

/** Operations on PaymentIntents. */
public final class PaymentIntents {

    private final Wire client;

    PaymentIntents(Wire client) {
        this.client = client;
    }

    public PaymentIntent create(PaymentIntentCreateParams params) {
        Wire.RequestOptions opts = new Wire.RequestOptions();
        opts.body = params.body();
        opts.idempotencyKey = params.idempotencyKey();
        return client.request("POST", "/v1/payment_intents", opts, PaymentIntent.class);
    }

    public PaymentIntent retrieve(String id) {
        return client.request("GET", "/v1/payment_intents/" + Util.pathSegment(id), null, PaymentIntent.class);
    }

    public PaymentIntent confirm(String id) {
        return confirm(id, PaymentIntentConfirmParams.create());
    }

    public PaymentIntent confirm(String id, PaymentIntentConfirmParams params) {
        Wire.RequestOptions opts = new Wire.RequestOptions();
        opts.body = params.body();
        opts.idempotencyKey = params.idempotencyKey();
        return client.request("POST", "/v1/payment_intents/" + Util.pathSegment(id) + "/confirm", opts, PaymentIntent.class);
    }

    public PaymentIntent cancel(String id) {
        Wire.RequestOptions opts = new Wire.RequestOptions();
        opts.body = new java.util.LinkedHashMap<>();
        return client.request("POST", "/v1/payment_intents/" + Util.pathSegment(id) + "/cancel", opts, PaymentIntent.class);
    }

    public AutoPagingIterable<PaymentIntent> list() {
        return list(null);
    }

    public AutoPagingIterable<PaymentIntent> list(ListParams params) {
        return new AutoPagingIterable<>(client, "/v1/payment_intents", Wire.listType(PaymentIntent.class), params);
    }
}
