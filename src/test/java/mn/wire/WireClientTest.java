package mn.wire;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WireClientTest {

    @Test
    void sendsAuthAndDecodes() throws Exception {
        AtomicReference<String> auth = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            auth.set(h.getOrDefault("authorization", ""));
            return new MockServer.Response(200,
                    "{\"id\":\"pi_1\",\"object\":\"payment_intent\",\"amount\":50000}");
        })) {
            Wire wire = s.client();
            PaymentIntent pi = wire.paymentIntents().retrieve("pi_1");
            assertEquals("Bearer sk_test_123", auth.get());
            assertEquals("pi_1", pi.id);
            assertEquals(50000, pi.amount);
        }
    }

    @Test
    void retriesOn503() throws Exception {
        AtomicInteger n = new AtomicInteger(0);
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            if (n.incrementAndGet() < 3) {
                return new MockServer.Response(503, "{}");
            }
            return new MockServer.Response(200, "{\"id\":\"pi_1\",\"object\":\"payment_intent\"}");
        })) {
            Wire wire = s.client(3);
            wire.paymentIntents().retrieve("pi_1");
            assertEquals(3, n.get());
        }
    }

    @Test
    void retriesOn429HonoringRetryAfter() throws Exception {
        AtomicInteger n = new AtomicInteger(0);
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            if (n.incrementAndGet() < 2) {
                return new MockServer.Response(429, "{}", "0");
            }
            return new MockServer.Response(200, "{\"id\":\"pi_1\",\"object\":\"payment_intent\"}");
        })) {
            Wire wire = s.client(3);
            wire.paymentIntents().retrieve("pi_1");
            assertEquals(2, n.get());
        }
    }

    @Test
    void doesNotRetryOn400() throws Exception {
        AtomicInteger n = new AtomicInteger(0);
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            n.incrementAndGet();
            return new MockServer.Response(400,
                    "{\"error\":{\"type\":\"invalid_request_error\",\"message\":\"bad\"}}");
        })) {
            Wire wire = s.client(3);
            assertThrows(WireException.class, () -> wire.paymentIntents().retrieve("x"));
            assertEquals(1, n.get());
        }
    }

    @Test
    void sendsIdempotencyKeyOnPost() throws Exception {
        AtomicReference<String> key = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            key.set(h.get("idempotency-key"));
            return new MockServer.Response(200, "{\"id\":\"pi_1\",\"object\":\"payment_intent\"}");
        })) {
            Wire wire = s.client();
            wire.paymentIntents().create(PaymentIntentCreateParams.create().amount(1));
            assertNotNull(key.get());
            assertTrue(key.get().startsWith("idk_"));
        }
    }

    @Test
    void reusesIdempotencyKeyAcrossRetries() throws Exception {
        AtomicInteger n = new AtomicInteger(0);
        AtomicReference<String> first = MockServer.capture();
        AtomicReference<String> second = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            int call = n.incrementAndGet();
            if (call == 1) {
                first.set(h.get("idempotency-key"));
                return new MockServer.Response(503, "{}");
            }
            second.set(h.get("idempotency-key"));
            return new MockServer.Response(200, "{\"id\":\"pi_1\",\"object\":\"payment_intent\"}");
        })) {
            Wire wire = s.client(3);
            wire.paymentIntents().create(PaymentIntentCreateParams.create().amount(1));
            assertEquals(first.get(), second.get());
            assertNotNull(first.get());
        }
    }

    @Test
    void honorsCallerSuppliedIdempotencyKey() throws Exception {
        AtomicReference<String> key = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            key.set(h.get("idempotency-key"));
            return new MockServer.Response(200, "{\"id\":\"pi_1\",\"object\":\"payment_intent\"}");
        })) {
            Wire wire = s.client();
            wire.paymentIntents().create(PaymentIntentCreateParams.create().amount(1).idempotencyKey("my-key"));
            assertEquals("my-key", key.get());
        }
    }

    @Test
    void networkFailureThrowsConnectionException() {
        // Nothing listening on this port; connection fails.
        Wire wire = new Wire("sk_test_123", WireOptions.builder()
                .baseUrl("http://127.0.0.1:1")
                .maxRetries(0)
                .backoff(java.time.Duration.ofMillis(1))
                .build());
        assertThrows(WireConnectionException.class, () -> wire.paymentIntents().retrieve("pi_1"));
    }
}
