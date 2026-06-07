package mn.wire;

/** Operations on Events. */
public final class Events {

    private final Wire client;

    Events(Wire client) {
        this.client = client;
    }

    public Event retrieve(String id) {
        return client.request("GET", "/v1/events/" + Util.pathSegment(id), null, Event.class);
    }

    public AutoPagingIterable<Event> list() {
        return list(null);
    }

    public AutoPagingIterable<Event> list(ListParams params) {
        return new AutoPagingIterable<>(client, "/v1/events", Wire.listType(Event.class), params);
    }
}
