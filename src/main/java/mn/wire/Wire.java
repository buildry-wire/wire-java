package mn.wire;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Wire is the API client. Construct it with an API key ({@code sk_live_...}) and an optional
 * {@link WireOptions}. The key is never logged nor placed in error messages.
 *
 * <pre>{@code
 * Wire wire = new Wire("sk_live_...");
 * PaymentIntent pi = wire.paymentIntents().create(
 *     PaymentIntentCreateParams.create().amount(50000).currency("MNT"));
 * }</pre>
 */
public final class Wire {

    private static final Gson GSON = new Gson();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SDK_VERSION = "1.0.0";

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final Duration backoff;
    private final HttpClient httpClient;

    private final PaymentIntents paymentIntents;
    private final Charges charges;
    private final Events events;
    private final WebhookEndpoints webhookEndpoints;
    private final Webhooks webhooks;

    public Wire(String apiKey) {
        this(apiKey, WireOptions.defaults());
    }

    public Wire(String apiKey, WireOptions opts) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey is required");
        }
        this.apiKey = apiKey;
        this.baseUrl = opts.baseUrl();
        this.timeout = opts.timeout();
        this.maxRetries = opts.maxRetries();
        this.backoff = opts.backoff();
        this.httpClient = opts.httpClient() != null
                ? opts.httpClient()
                : HttpClient.newBuilder().connectTimeout(this.timeout).build();

        this.paymentIntents = new PaymentIntents(this);
        this.charges = new Charges(this);
        this.events = new Events(this);
        this.webhookEndpoints = new WebhookEndpoints(this);
        this.webhooks = new Webhooks();
    }

    public PaymentIntents paymentIntents() {
        return paymentIntents;
    }

    public Charges charges() {
        return charges;
    }

    public Events events() {
        return events;
    }

    public WebhookEndpoints webhookEndpoints() {
        return webhookEndpoints;
    }

    public Webhooks webhooks() {
        return webhooks;
    }

    // ---- request engine -------------------------------------------------

    /** Options for a single HTTP request. */
    static final class RequestOptions {
        Map<String, Object> body;
        Map<String, Object> query;
        String idempotencyKey;
    }

    <T> T request(String method, String path, RequestOptions opts, Type responseType) {
        String url = buildUrl(path, opts == null ? null : opts.query);

        String bodyStr = null;
        if (opts != null && opts.body != null) {
            bodyStr = GSON.toJson(opts.body);
        }

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .header("User-Agent", "wire-java/" + SDK_VERSION);

        if (bodyStr != null) {
            rb.header("Content-Type", "application/json");
            rb.method(method, HttpRequest.BodyPublishers.ofString(bodyStr, StandardCharsets.UTF_8));
        } else {
            rb.method(method, HttpRequest.BodyPublishers.noBody());
        }

        if ("POST".equals(method)) {
            String key = opts != null && opts.idempotencyKey != null
                    ? opts.idempotencyKey
                    : newIdempotencyKey();
            rb.header("Idempotency-Key", key);
        }

        HttpRequest httpRequest = rb.build();

        for (int attempt = 0; ; attempt++) {
            HttpResponse<String> resp;
            try {
                resp = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (attempt < maxRetries) {
                    sleep(backoffMillis(attempt));
                    continue;
                }
                throw new WireConnectionException("request failed: " + e.getMessage());
            }

            int status = resp.statusCode();
            if ((status == 429 || status >= 500) && attempt < maxRetries) {
                long retryAfter = parseRetryAfter(resp.headers().firstValue("retry-after").orElse(null));
                sleep(retryAfter > 0 ? retryAfter : backoffMillis(attempt));
                continue;
            }

            String text = resp.body() != null ? resp.body() : "";
            if (status >= 200 && status < 300) {
                return decode(text, responseType);
            }
            throw ErrorDecoder.decode(status, text);
        }
    }

    private <T> T decode(String text, Type responseType) {
        if (text.isEmpty()) {
            return GSON.fromJson("{}", responseType);
        }
        return GSON.fromJson(text, responseType);
    }

    private String buildUrl(String path, Map<String, Object> query) {
        StringBuilder sb = new StringBuilder(baseUrl).append(path);
        if (query != null && !query.isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Object> e : query.entrySet()) {
                Object v = e.getValue();
                if (v == null) {
                    continue;
                }
                String s = String.valueOf(v);
                if (s.isEmpty() || "0".equals(s)) {
                    continue;
                }
                parts.add(enc(e.getKey()) + "=" + enc(s));
            }
            if (!parts.isEmpty()) {
                sb.append('?').append(String.join("&", parts));
            }
        }
        return sb.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** Exponential backoff with full jitter: random in {@code [0, base * 2^attempt]}. */
    private long backoffMillis(int attempt) {
        long base = backoff.toMillis() * (1L << attempt);
        if (base <= 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextLong(base + 1);
    }

    private static long parseRetryAfter(String header) {
        if (header == null || header.isEmpty()) {
            return 0;
        }
        try {
            long seconds = Long.parseLong(header.trim());
            return seconds > 0 ? seconds * 1000L : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String newIdempotencyKey() {
        byte[] buf = new byte[16];
        RANDOM.nextBytes(buf);
        StringBuilder sb = new StringBuilder("idk_");
        for (byte b : buf) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    // Helper used by resources to build a parameterized list type token.
    static <T> Type listType(Class<T> element) {
        return TypeToken.getParameterized(WireList.class, element).getType();
    }
}
