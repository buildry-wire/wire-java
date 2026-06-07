package mn.wire;

/** Operations on Charges. */
public final class Charges {

    private final Wire client;

    Charges(Wire client) {
        this.client = client;
    }

    public Charge retrieve(String id) {
        return client.request("GET", "/v1/charges/" + Util.pathSegment(id), null, Charge.class);
    }

    public AutoPagingIterable<Charge> list() {
        return list(null);
    }

    public AutoPagingIterable<Charge> list(ListParams params) {
        return new AutoPagingIterable<>(client, "/v1/charges", Wire.listType(Charge.class), params);
    }
}
