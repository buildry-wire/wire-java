package mn.wire;

import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

/**
 * Verifies inbound webhook signatures. The signature scheme is
 * {@code HMAC-SHA256(secret, "<t>.<rawBody>")} carried in the
 * {@code WirePayment-Signature: t=<unix>,v1=<hex>} header. Verification always runs on the RAW
 * request body, before any JSON parsing, and fails closed on any error.
 */
public final class Webhooks {

    /** The header carrying the webhook signature. */
    public static final String SIGNATURE_HEADER = "WirePayment-Signature";

    /** Default replay-protection tolerance in seconds. */
    public static final long DEFAULT_TOLERANCE_SECONDS = 300;

    private static final Gson GSON = new Gson();

    /** Verify using the default 300s tolerance and the current time. */
    public Event verify(String rawBody, String signatureHeader, String secret) {
        return verify(rawBody, signatureHeader, secret, DEFAULT_TOLERANCE_SECONDS);
    }

    /** Verify with an explicit tolerance, using the current time. */
    public Event verify(String rawBody, String signatureHeader, String secret, long toleranceSeconds) {
        return verifyAt(rawBody, signatureHeader, secret, toleranceSeconds, System.currentTimeMillis() / 1000L);
    }

    /**
     * Testable verification core taking an explicit {@code now} (unix seconds). Returns the parsed
     * {@link Event} on success; throws {@link SignatureVerificationException} otherwise.
     */
    public Event verifyAt(String rawBody, String signatureHeader, String secret, long toleranceSeconds, long now) {
        if (rawBody == null) {
            throw new SignatureVerificationException("missing body");
        }
        Parsed parsed = parseHeader(signatureHeader);
        if (parsed == null || parsed.v1 == null || parsed.v1.isEmpty()) {
            throw new SignatureVerificationException("malformed signature header");
        }
        if (Math.abs(now - parsed.t) > toleranceSeconds) {
            throw new SignatureVerificationException("timestamp outside tolerance");
        }

        byte[] bodyBytes = rawBody.getBytes(StandardCharsets.UTF_8);
        String expected = hmacHex(secret, parsed.t + ".", bodyBytes);

        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = parsed.v1.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length || !MessageDigest.isEqual(a, b)) {
            throw new SignatureVerificationException("signature mismatch");
        }
        return GSON.fromJson(rawBody, Event.class);
    }

    private static String hmacHex(String secret, String prefix, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(prefix.getBytes(StandardCharsets.UTF_8));
            mac.update(body);
            byte[] digest = mac.doFinal();
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte x : digest) {
                sb.append(Character.forDigit((x >> 4) & 0xF, 16));
                sb.append(Character.forDigit(x & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SignatureVerificationException("hmac error: " + e.getMessage());
        }
    }

    private static Parsed parseHeader(String header) {
        if (header == null) {
            return null;
        }
        Long t = null;
        String v1 = null;
        for (String part : header.split(",")) {
            String trimmed = part.trim();
            int eq = trimmed.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = trimmed.substring(0, eq);
            String val = trimmed.substring(eq + 1);
            if ("t".equals(key)) {
                try {
                    t = Long.parseLong(val);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if ("v1".equals(key)) {
                v1 = val;
            }
        }
        if (t == null) {
            return null;
        }
        Parsed p = new Parsed();
        p.t = t;
        p.v1 = v1;
        return p;
    }

    private static final class Parsed {
        long t;
        String v1;
    }
}
