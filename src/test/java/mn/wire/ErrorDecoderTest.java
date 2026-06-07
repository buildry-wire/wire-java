package mn.wire;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorDecoderTest {

    @Test
    void parsesTheEnvelope() {
        String body = "{\"error\":{\"type\":\"invalid_request_error\",\"code\":\"amount_invalid\","
                + "\"message\":\"amount must be positive\",\"param\":\"amount\",\"request_id\":\"req_123\","
                + "\"doc_url\":\"https://docs.wire.mn/errors\",\"operator_decline_code\":\"insufficient_funds\"}}";
        WireException err = ErrorDecoder.decode(400, body);
        assertEquals(400, err.getStatusCode());
        assertEquals("invalid_request_error", err.getType());
        assertEquals("amount_invalid", err.getCode());
        assertEquals("amount", err.getParam());
        assertEquals("req_123", err.getRequestId());
        assertEquals("https://docs.wire.mn/errors", err.getDocUrl());
        assertEquals("insufficient_funds", err.getOperatorDeclineCode());
        assertTrue(err.getMessage().contains("amount must be positive"));
        assertTrue(err.toString().contains("req_123"));
    }

    @Test
    void fallsBackOnNonJson() {
        WireException err = ErrorDecoder.decode(500, "not json");
        assertEquals(500, err.getStatusCode());
        assertEquals("api_error", err.getType());
    }

    @Test
    void fallsBackOnMissingErrorField() {
        WireException err = ErrorDecoder.decode(503, "{\"message\":\"oops\"}");
        assertEquals(503, err.getStatusCode());
        assertEquals("api_error", err.getType());
    }
}
