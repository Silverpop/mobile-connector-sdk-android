package com.silverpop.engage.recipient;

/**
 * Created by Lindsay Thurmond on 1/13/15.
 */
public class CheckIdentityResult {

    private String recipientId;

    private String mobileUserId;

    public CheckIdentityResult() {
    }

    public CheckIdentityResult(String recipientId, String mobileUserId) {
        this.recipientId = recipientId;
        this.mobileUserId = mobileUserId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMobileUserId() {
        return mobileUserId;
    }

    public void setMobileUserId(String mobileUserId) {
        this.mobileUserId = mobileUserId;
    }
}
