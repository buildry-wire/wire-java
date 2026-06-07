package mn.wire;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/** A PaymentIntent resource. Amounts are in minor units (MNT integer). */
public class PaymentIntent implements HasId {

    public String id;
    public String object;
    public long amount;
    public String currency;
    public String status;

    @SerializedName("client_secret")
    public String clientSecret;

    @SerializedName("automatic_operator")
    public boolean automaticOperator;

    @SerializedName("allowed_operators")
    public List<String> allowedOperators;

    @SerializedName("selected_operator")
    public String selectedOperator;

    @SerializedName("next_action")
    public Map<String, Object> nextAction;

    public Map<String, String> metadata;

    public boolean livemode;

    public long created;

    @SerializedName("expires_at")
    public Long expiresAt;

    @Override
    public String getId() {
        return id;
    }
}
