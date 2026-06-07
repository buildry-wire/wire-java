package mn.wire;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class Util {

    private Util() {
    }

    /** Percent-encodes a path segment so resource ids are safe in the URL. */
    static String pathSegment(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }
}
