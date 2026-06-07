package mn.wire;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class WebhooksTest {

    private static final Gson GSON = new Gson();

    /** Runs every shared conformance vector; all MUST produce the expected verdict. */
    @TestFactory
    List<DynamicTest> conformanceVectors() throws Exception {
        JsonObject root;
        try (InputStream in = WebhooksTest.class.getResourceAsStream("/webhook-signatures.json")) {
            assertNotNull(in, "webhook-signatures.json must be on the test classpath");
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            root = GSON.fromJson(json, JsonObject.class);
        }
        String secret = root.get("secret").getAsString();
        long now = root.get("now").getAsLong();
        long tolerance = root.get("tolerance_seconds").getAsLong();

        Webhooks w = new Webhooks();
        List<DynamicTest> tests = new ArrayList<>();
        for (var el : root.getAsJsonArray("cases")) {
            JsonObject c = el.getAsJsonObject();
            String name = c.get("name").getAsString();
            String body = c.get("body").getAsString();
            String header = c.get("header").getAsString();
            boolean valid = c.get("valid").getAsBoolean();

            tests.add(dynamicTest(name, () -> {
                boolean ok = true;
                Event ev = null;
                try {
                    ev = w.verifyAt(body, header, secret, tolerance, now);
                } catch (SignatureVerificationException e) {
                    ok = false;
                }
                assertEquals(valid, ok, "verdict mismatch for vector: " + name);
                if (valid) {
                    assertNotNull(ev);
                    assertNotNull(ev.type);
                }
            }));
        }
        return tests;
    }

    @Test
    void rejectsMalformedHeader() {
        Webhooks w = new Webhooks();
        assertThrows(SignatureVerificationException.class,
                () -> w.verifyAt("{}", "garbage", "whsec_x", 300, 1700000300L));
    }

    @Test
    void rejectsOutsideTolerance() {
        Webhooks w = new Webhooks();
        assertThrows(SignatureVerificationException.class,
                () -> w.verifyAt("{}", "t=1,v1=deadbeef", "whsec_x", 300, 1700000300L));
    }

    @Test
    void roundTripsAGeneratedSignature() {
        Webhooks w = new Webhooks();
        String secret = "whsec_roundtrip";
        String body = "{\"id\":\"evt_x\",\"object\":\"event\",\"type\":\"payment_intent.succeeded\"}";
        long t = 1700000000L;
        String sig = hmacHex(secret, t + "." + body);
        Event ev = w.verifyAt(body, "t=" + t + ",v1=" + sig, secret, 300, t);
        assertEquals("payment_intent.succeeded", ev.type);
    }

    private static String hmacHex(String secret, String message) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] d = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
