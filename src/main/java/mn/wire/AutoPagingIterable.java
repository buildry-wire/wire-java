package mn.wire;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lazily iterates every item across all pages of a list endpoint, following {@code has_more} via
 * the {@code starting_after} cursor. Pages are fetched on demand as iteration proceeds.
 */
public final class AutoPagingIterable<T extends HasId> implements Iterable<T> {

    private final Wire client;
    private final String path;
    private final Type listType;
    private final Integer limit;
    private final String initialStartingAfter;

    AutoPagingIterable(Wire client, String path, Type listType, ListParams params) {
        this.client = client;
        this.path = path;
        this.listType = listType;
        this.limit = params != null ? params.limit() : null;
        this.initialStartingAfter = params != null ? params.startingAfter() : null;
    }

    @Override
    public Iterator<T> iterator() {
        return new PageIterator();
    }

    private final class PageIterator implements Iterator<T> {
        private List<T> current = null;
        private int index = 0;
        private String after = initialStartingAfter;
        private boolean hasMore = true;
        private boolean done = false;

        @Override
        public boolean hasNext() {
            advanceIfNeeded();
            return current != null && index < current.size();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T item = current.get(index++);
            after = item.getId();
            return item;
        }

        private void advanceIfNeeded() {
            while (!done && (current == null || index >= current.size())) {
                if (!hasMore) {
                    done = true;
                    return;
                }
                fetchPage();
            }
        }

        private void fetchPage() {
            Wire.RequestOptions opts = new Wire.RequestOptions();
            opts.query = new java.util.LinkedHashMap<>();
            if (limit != null && limit > 0) {
                opts.query.put("limit", limit);
            }
            if (after != null && !after.isEmpty()) {
                opts.query.put("starting_after", after);
            }
            WireList<T> page = client.request("GET", path, opts, listType);
            current = page.data;
            index = 0;
            hasMore = page.hasMore && current != null && !current.isEmpty();
            if (current == null || current.isEmpty()) {
                hasMore = false;
                done = true;
            }
        }
    }
}
