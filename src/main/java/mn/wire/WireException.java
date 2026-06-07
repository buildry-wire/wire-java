package mn.wire;

/**
 * A typed error returned by the Wire API, decoded from the {@code {"error": {...}}} envelope.
 *
 * <p>Carries the structured fields the API returns: {@code type}, {@code code}, {@code param},
 * {@code requestId}, {@code docUrl}, {@code operatorDeclineCode} and the HTTP {@code statusCode}.
 * The API key is never included in the message or any field.
 */
public class WireException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String type;
    private final String code;
    private final String param;
    private final String requestId;
    private final String docUrl;
    private final String operatorDeclineCode;
    private final Integer statusCode;

    WireException(String message, Builder b) {
        super(message);
        this.type = b.type != null ? b.type : "api_error";
        this.code = b.code;
        this.param = b.param;
        this.requestId = b.requestId;
        this.docUrl = b.docUrl;
        this.operatorDeclineCode = b.operatorDeclineCode;
        this.statusCode = b.statusCode;
    }

    /** Error category, e.g. {@code invalid_request_error}, {@code api_error}. Never null. */
    public String getType() {
        return type;
    }

    /** Machine-readable error code, e.g. {@code amount_invalid}. May be null. */
    public String getCode() {
        return code;
    }

    /** The request parameter the error relates to, if any. */
    public String getParam() {
        return param;
    }

    /** The {@code request_id} for support/debugging. Always preserved when present. */
    public String getRequestId() {
        return requestId;
    }

    /** A documentation URL the API may include for this error. */
    public String getDocUrl() {
        return docUrl;
    }

    /** Operator-specific decline code for declined charges, if any. */
    public String getOperatorDeclineCode() {
        return operatorDeclineCode;
    }

    /** HTTP status code that produced this error, or null for non-HTTP failures. */
    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "WireException: " + getMessage()
                + " (type=" + type
                + ", code=" + code
                + ", status=" + statusCode
                + ", request_id=" + requestId + ")";
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private String type;
        private String code;
        private String param;
        private String requestId;
        private String docUrl;
        private String operatorDeclineCode;
        private Integer statusCode;

        Builder type(String v) {
            this.type = v;
            return this;
        }

        Builder code(String v) {
            this.code = v;
            return this;
        }

        Builder param(String v) {
            this.param = v;
            return this;
        }

        Builder requestId(String v) {
            this.requestId = v;
            return this;
        }

        Builder docUrl(String v) {
            this.docUrl = v;
            return this;
        }

        Builder operatorDeclineCode(String v) {
            this.operatorDeclineCode = v;
            return this;
        }

        Builder statusCode(Integer v) {
            this.statusCode = v;
            return this;
        }

        WireException build(String message) {
            return new WireException(message, this);
        }
    }
}
