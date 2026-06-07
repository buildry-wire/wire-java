package mn.wire;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parameters for creating a PaymentIntent. {@code amount} is in minor units (MNT integer). */
public class PaymentIntentCreateParams {
    private final Map<String, Object> body = new LinkedHashMap<>();
    private String idempotencyKey;

    public static PaymentIntentCreateParams create() {
        return new PaymentIntentCreateParams();
    }

    public PaymentIntentCreateParams amount(long amount) {
        body.put("amount", amount);
        return this;
    }

    public PaymentIntentCreateParams currency(String currency) {
        body.put("currency", currency);
        return this;
    }

    public PaymentIntentCreateParams automaticOperator(boolean v) {
        body.put("automatic_operator", v);
        return this;
    }

    /** The operator ids enabled on your account, e.g. {@code List.of("sandbox")}. */
    public PaymentIntentCreateParams allowedOperators(List<String> operators) {
        body.put("allowed_operators", operators);
        return this;
    }

    public PaymentIntentCreateParams metadata(Map<String, String> metadata) {
        body.put("metadata", metadata);
        return this;
    }

    public PaymentIntentCreateParams idempotencyKey(String key) {
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
