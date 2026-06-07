package mn.wire;

import com.google.gson.annotations.SerializedName;

/** A Charge resource. Amounts are in minor units (MNT integer). */
public class Charge implements HasId {

    public String id;
    public String object;

    @SerializedName("payment_intent")
    public String paymentIntent;

    public String operator;

    @SerializedName("operator_charge_id")
    public String operatorChargeId;

    public String status;
    public long amount;
    public long fee;

    @SerializedName("amount_refunded")
    public long amountRefunded;

    @SerializedName("failure_code")
    public String failureCode;

    @SerializedName("failure_message")
    public String failureMessage;

    public boolean livemode;
    public long created;

    @Override
    public String getId() {
        return id;
    }
}
