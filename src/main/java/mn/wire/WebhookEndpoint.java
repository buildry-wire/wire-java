package mn.wire;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** A WebhookEndpoint resource. The {@code secret} is only returned on creation. */
public class WebhookEndpoint implements HasId {

    public String id;
    public String object;
    public String url;

    @SerializedName("enabled_events")
    public List<String> enabledEvents;

    public String status;
    public String secret;
    public boolean livemode;
    public long created;

    @Override
    public String getId() {
        return id;
    }
}
