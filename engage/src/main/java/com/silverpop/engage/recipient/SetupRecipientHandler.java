package com.silverpop.engage.recipient;

import com.silverpop.engage.Handler;

/**
 * Created by Lindsay Thurmond on 1/2/15.
 */
public interface SetupRecipientHandler extends Handler<SetupRecipientResult, Exception> {

    public void onSuccess(SetupRecipientResult result);

    public void onFailure(Exception exception);

}
