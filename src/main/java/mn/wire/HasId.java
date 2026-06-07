package mn.wire;

/** Implemented by list-able resources so the auto-paginator can read the cursor id. */
public interface HasId {
    String getId();
}
