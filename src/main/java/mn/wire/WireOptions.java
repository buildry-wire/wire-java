package mn.wire;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for a {@link Wire} client. Build with {@link #builder()}; all values have sensible
 * defaults: base URL {@code https://api.wire.mn}, 30s timeout, 2 retries, 500ms base backoff.
 */
public final class WireOptions {

    public static final String DEFAULT_BASE_URL = "https://api.wire.mn";

    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final Duration backoff;
    private final HttpClient httpClient;

    private WireOptions(Builder b) {
        String url = b.baseUrl != null ? b.baseUrl : DEFAULT_BASE_URL;
        // strip trailing slashes
        this.baseUrl = url.replaceAll("/+$", "");
        this.timeout = b.timeout != null ? b.timeout : Duration.ofSeconds(30);
        this.maxRetries = b.maxRetries != null ? b.maxRetries : 2;
        this.backoff = b.backoff != null ? b.backoff : Duration.ofMillis(500);
        this.httpClient = b.httpClient;
    }

    public static WireOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    String baseUrl() {
        return baseUrl;
    }

    Duration timeout() {
        return timeout;
    }

    int maxRetries() {
        return maxRetries;
    }

    Duration backoff() {
        return backoff;
    }

    HttpClient httpClient() {
        return httpClient;
    }

    public static final class Builder {
        private String baseUrl;
        private Duration timeout;
        private Integer maxRetries;
        private Duration backoff;
        private HttpClient httpClient;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder backoff(Duration backoff) {
            this.backoff = backoff;
            return this;
        }

        /** Inject a custom {@link HttpClient} (e.g. for tests or proxies). */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public WireOptions build() {
            return new WireOptions(this);
        }
    }
}
