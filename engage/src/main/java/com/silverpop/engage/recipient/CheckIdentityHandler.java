package com.silverpop.engage.recipient;

import com.silverpop.engage.Handler;

/**
 * Created by Lindsay Thurmond on 1/6/15.
 */
public interface CheckIdentityHandler extends Handler<CheckIdentityResult, CheckIdentityFailure> {

    public void onSuccess(CheckIdentityResult result);

    public void onFailure(CheckIdentityFailure failure);

}
