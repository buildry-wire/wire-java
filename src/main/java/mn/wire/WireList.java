package mn.wire;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** A single page of a cursor-paginated list response. */
public class WireList<T> {
    public String object;
    public List<T> data;

    @SerializedName("has_more")
    public boolean hasMore;
}
