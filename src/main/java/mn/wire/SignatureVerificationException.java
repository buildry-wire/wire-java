package mn.wire;

/** Thrown when a webhook signature does not verify (parse failure, mismatch, or outside tolerance). */
public class SignatureVerificationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SignatureVerificationException(String message) {
        super(message);
    }
}
