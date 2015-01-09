package com.silverpop.engage.recipient;

/**
 * Created by Lindsay Thurmond on 1/2/15.
 */
public interface SetupRecipientHandler {

    public void onSuccess(String recipientId);

    public void onFailure(Exception error);

}
