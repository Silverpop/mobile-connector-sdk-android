package com.silverpop.engage.recipient;

/**
 * Created by Lindsay Thurmond on 1/13/15.
 */
public class CheckIdentityResult {

    private String recipientId;

    private String mobileUserId;

    private String mergedRecipientId;

    public CheckIdentityResult() {
    }

    public CheckIdentityResult(String recipientId, String mobileUserId) {
        this.recipientId = recipientId;
        this.mobileUserId = mobileUserId;
    }

    public CheckIdentityResult(String recipientId, String mergedRecipientId, String mobileUserId) {
        this.recipientId = recipientId;
        this.mobileUserId = mobileUserId;
        this.mergedRecipientId = mergedRecipientId;
    }

    /**
     * @return recipient id that is currently configured in the Engage SDK.
     * It should match {@link com.silverpop.engage.config.EngageConfig#recipientId(android.content.Context)}
     */
    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    /**
     *
     * @return  mobile user id that is currently configured in the Engage SDK.
     * It should match {@link com.silverpop.engage.config.EngageConfig#mobileUserId(android.content.Context)}
     */
    public String getMobileUserId() {
        return mobileUserId;
    }

    public void setMobileUserId(String mobileUserId) {
        this.mobileUserId = mobileUserId;
    }

    /**
     * @return recipient id of the merged recipient no longer being used by the Engage SDK
     * if recipients were merge, otherwise {@code null}
     */
    public String getMergedRecipientId() {
        return mergedRecipientId;
    }

    public void setMergedRecipientId(String mergedRecipientId) {
        this.mergedRecipientId = mergedRecipientId;
    }
}
