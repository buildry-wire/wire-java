package mn.wire;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/** A webhook/event resource. */
public class Event implements HasId {

    public String id;
    public String object;
    public String type;

    @SerializedName("api_version")
    public String apiVersion;

    public Map<String, Object> data;

    public boolean livemode;
    public long created;

    @Override
    public String getId() {
        return id;
    }
}
