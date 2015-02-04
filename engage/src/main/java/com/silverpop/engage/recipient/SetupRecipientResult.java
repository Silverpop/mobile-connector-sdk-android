package com.silverpop.engage.recipient;

/**
 * Created by Lindsay Thurmond on 1/13/15.
 */
public class SetupRecipientResult {

    private String recipientId;

    public SetupRecipientResult(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}
