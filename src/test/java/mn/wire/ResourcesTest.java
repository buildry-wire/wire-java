package mn.wire;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesTest {

    @Test
    void createsAPaymentIntent() throws Exception {
        AtomicReference<String> method = MockServer.capture();
        AtomicReference<String> path = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            method.set(m);
            path.set(p);
            return new MockServer.Response(200,
                    "{\"id\":\"pi_1\",\"object\":\"payment_intent\",\"amount\":50000,\"status\":\"requires_payment_method\"}");
        })) {
            Wire wire = s.client();
            PaymentIntent pi = wire.paymentIntents().create(
                    PaymentIntentCreateParams.create().amount(50000).currency("MNT")
                            .allowedOperators(List.of("sandbox")));
            assertEquals("POST", method.get());
            assertEquals("/v1/payment_intents", path.get());
            assertEquals("pi_1", pi.id);
            assertEquals("requires_payment_method", pi.status);
        }
    }

    @Test
    void confirmsAndCancels() throws Exception {
        AtomicReference<String> lastPath = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            lastPath.set(p);
            return new MockServer.Response(200, "{\"id\":\"pi_1\",\"object\":\"payment_intent\",\"status\":\"succeeded\"}");
        })) {
            Wire wire = s.client();
            PaymentIntent c = wire.paymentIntents().confirm("pi_1",
                    PaymentIntentConfirmParams.create().returnUrl("https://example.com/return"));
            assertEquals("succeeded", c.status);
            assertTrue(lastPath.get().endsWith("/confirm"));

            wire.paymentIntents().cancel("pi_1");
            assertTrue(lastPath.get().endsWith("/cancel"));
        }
    }

    @Test
    void autoPaginatesList() throws Exception {
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            if (!p.contains("starting_after")) {
                return new MockServer.Response(200,
                        "{\"object\":\"list\",\"has_more\":true,\"data\":[{\"id\":\"ch_1\",\"object\":\"charge\"}]}");
            }
            return new MockServer.Response(200,
                    "{\"object\":\"list\",\"has_more\":false,\"data\":[{\"id\":\"ch_2\",\"object\":\"charge\"}]}");
        })) {
            Wire wire = s.client();
            List<String> ids = new ArrayList<>();
            for (Charge ch : wire.charges().list(ListParams.create().limit(1))) {
                ids.add(ch.id);
            }
            assertEquals(List.of("ch_1", "ch_2"), ids);
        }
    }

    @Test
    void listStopsOnEmptyPage() throws Exception {
        try (MockServer s = MockServer.start((m, p, h, b) ->
                new MockServer.Response(200, "{\"object\":\"list\",\"has_more\":true,\"data\":[]}"))) {
            Wire wire = s.client();
            int count = 0;
            for (Event ignored : wire.events().list()) {
                count++;
            }
            assertEquals(0, count);
        }
    }

    @Test
    void retrievesCharge() throws Exception {
        try (MockServer s = MockServer.start((m, p, h, b) ->
                new MockServer.Response(200, "{\"id\":\"ch_1\",\"object\":\"charge\",\"amount\":100,\"fee\":3}"))) {
            Wire wire = s.client();
            Charge ch = wire.charges().retrieve("ch_1");
            assertEquals("ch_1", ch.id);
            assertEquals(100, ch.amount);
            assertEquals(3, ch.fee);
        }
    }

    @Test
    void webhookEndpointCrud() throws Exception {
        AtomicReference<String> lastMethod = MockServer.capture();
        try (MockServer s = MockServer.start((m, p, h, b) -> {
            lastMethod.set(m);
            if ("DELETE".equals(m)) {
                return new MockServer.Response(200, "{\"id\":\"we_1\",\"object\":\"webhook_endpoint\",\"deleted\":true}");
            }
            return new MockServer.Response(200,
                    "{\"id\":\"we_1\",\"object\":\"webhook_endpoint\",\"url\":\"https://example.com/wh\",\"status\":\"enabled\"}");
        })) {
            Wire wire = s.client();
            WebhookEndpoint we = wire.webhookEndpoints().create(
                    WebhookEndpointCreateParams.create().url("https://example.com/wh")
                            .enabledEvents(List.of("payment_intent.succeeded")));
            assertEquals("we_1", we.id);

            WebhookEndpoint upd = wire.webhookEndpoints().update("we_1",
                    WebhookEndpointUpdateParams.create().status("disabled"));
            assertEquals("we_1", upd.id);

            Deleted d = wire.webhookEndpoints().delete("we_1");
            assertEquals("DELETE", lastMethod.get());
            assertTrue(d.deleted);
        }
    }
}
