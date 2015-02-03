package com.silverpop.engage.demo.engagetest;

import android.util.Log;
import com.silverpop.engage.EngageApplication;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

/**
 * Created by azuercher on 6/12/14.
 */
public class Application extends EngageApplication {

    public static final String ENGAGE_TEST_TAG = "engagetest";

    @Override
    public void onCreate(){
        UAirship.takeOff(this);
        PushManager.enablePush();
        String apid = PushManager.shared().getAPID();
        Log.e(ENGAGE_TEST_TAG, "My Application onCreate - App APID: " + apid);
        PushManager.shared().setIntentReceiver(PushReceiver.class);

        super.onCreate();

        // do any custom setup you need

    }

}
