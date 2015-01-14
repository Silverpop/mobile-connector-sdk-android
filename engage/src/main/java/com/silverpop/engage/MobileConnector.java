package com.silverpop.engage;

import com.silverpop.engage.recipient.CheckIdentityHandler;
import com.silverpop.engage.recipient.SetupRecipientHandler;

import java.util.Map;

/**
 * Created by Lindsay Thurmond on 1/12/15.
 */
public interface MobileConnector {

    void setupRecipient(SetupRecipientHandler setupRecipientHandler);

    void checkIdentity(Map<String, String> idFieldNamesToValues, CheckIdentityHandler identityHandler);
}
