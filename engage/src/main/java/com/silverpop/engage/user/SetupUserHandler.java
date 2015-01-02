package com.silverpop.engage.user;

/**
 * Created by Lindsay Thurmond on 1/2/15.
 */
public interface SetupUserHandler {

    public void onCreateSuccess(String recipientId);

    public void onCreateFailure(String error);

}
