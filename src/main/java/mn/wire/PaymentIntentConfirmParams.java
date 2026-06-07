package mn.wire;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parameters for confirming a PaymentIntent. */
public class PaymentIntentConfirmParams {
    private final Map<String, Object> body = new LinkedHashMap<>();
    private String idempotencyKey;

    public static PaymentIntentConfirmParams create() {
        return new PaymentIntentConfirmParams();
    }

    public PaymentIntentConfirmParams returnUrl(String url) {
        body.put("return_url", url);
        return this;
    }

    public PaymentIntentConfirmParams idempotencyKey(String key) {
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
