package mn.wire;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/** A tiny in-process HTTP server backed by the JDK's {@link HttpServer} for resource tests. */
final class MockServer implements AutoCloseable {

    /** A request handler returning a status + JSON body for each call. */
    interface Handler {
        Response handle(String method, String pathAndQuery, java.util.Map<String, String> headers, String body) throws IOException;
    }

    static final class Response {
        final int status;
        final String json;
        final String retryAfter;

        Response(int status, String json) {
            this(status, json, null);
        }

        Response(int status, String json, String retryAfter) {
            this.status = status;
            this.json = json;
            this.retryAfter = retryAfter;
        }
    }

    private final HttpServer server;
    private final String url;

    private MockServer(HttpServer server, String url) {
        this.server = server;
        this.url = url;
    }

    static MockServer start(Handler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> dispatch(exchange, handler));
        server.start();
        String url = "http://127.0.0.1:" + server.getAddress().getPort();
        return new MockServer(server, url);
    }

    private static void dispatch(HttpExchange exchange, Handler handler) throws IOException {
        String method = exchange.getRequestMethod();
        String pathAndQuery = exchange.getRequestURI().getRawPath();
        if (exchange.getRequestURI().getRawQuery() != null) {
            pathAndQuery += "?" + exchange.getRequestURI().getRawQuery();
        }
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        exchange.getRequestHeaders().forEach((k, v) -> {
            if (!v.isEmpty()) {
                headers.put(k.toLowerCase(java.util.Locale.ROOT), v.get(0));
            }
        });

        String body;
        try (InputStream in = exchange.getRequestBody()) {
            body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        Response resp = handler.handle(method, pathAndQuery, headers, body);
        byte[] out = resp.json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        if (resp.retryAfter != null) {
            exchange.getResponseHeaders().add("Retry-After", resp.retryAfter);
        }
        // HEAD/204 must not send a body length; here all responses carry JSON.
        exchange.sendResponseHeaders(resp.status, out.length == 0 ? -1 : out.length);
        if (out.length > 0) {
            exchange.getResponseBody().write(out);
        }
        exchange.close();
    }

    String url() {
        return url;
    }

    Wire client() {
        return client(2);
    }

    Wire client(int maxRetries) {
        return new Wire("sk_test_123", WireOptions.builder()
                .baseUrl(url)
                .maxRetries(maxRetries)
                .backoff(java.time.Duration.ofMillis(1))
                .build());
    }

    @Override
    public void close() {
        server.stop(0);
    }

    /** Convenience for capturing a single value from inside a handler lambda. */
    static <T> AtomicReference<T> capture() {
        return new AtomicReference<>();
    }
}
