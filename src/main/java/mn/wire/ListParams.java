package mn.wire;

/** Parameters for list/auto-pagination calls. */
public class ListParams {
    private Integer limit;
    private String startingAfter;
    private String endingBefore;

    public static ListParams create() {
        return new ListParams();
    }

    /** Page size, 1–100. */
    public ListParams limit(int limit) {
        this.limit = limit;
        return this;
    }

    public ListParams startingAfter(String id) {
        this.startingAfter = id;
        return this;
    }

    public ListParams endingBefore(String id) {
        this.endingBefore = id;
        return this;
    }

    Integer limit() {
        return limit;
    }

    String startingAfter() {
        return startingAfter;
    }

    String endingBefore() {
        return endingBefore;
    }
}
