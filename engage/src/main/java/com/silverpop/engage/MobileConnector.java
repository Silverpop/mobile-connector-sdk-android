package com.silverpop.engage;

import com.silverpop.engage.recipient.SetupRecipientHandler;

import java.util.Map;

/**
 * Created by Lindsay Thurmond on 1/12/15.
 */
public interface MobileConnector {


    void setupRecipient(SetupRecipientHandler setupRecipientHandler);

    //[Lindsay Thurmond:1/12/15] TODO: test me
    void checkIdentity(Map<String, String> idFieldNamesToValues, IdentityHandler identityHandler);
}
