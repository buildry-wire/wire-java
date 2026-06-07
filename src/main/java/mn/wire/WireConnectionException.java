package mn.wire;

/**
 * Raised for network-level failures (connection errors and timeouts) after retries are exhausted,
 * as a distinct type from API errors ({@link WireException}). It is a subclass of
 * {@link WireException} so callers may catch either, but {@code type} is always {@code api_error}
 * with no status code.
 */
public class WireConnectionException extends WireException {

    private static final long serialVersionUID = 1L;

    WireConnectionException(String message) {
        super(message, WireException.builder().type("api_error"));
    }
}
