package com.silverpop.engage.demo.engagetest;

import android.util.Log;

import com.silverpop.engage.EngageApplication;
import com.silverpop.engage.config.EngageConfig;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

/**
 * Created by azuercher on 6/12/14.
 */
public class Application extends EngageApplication {
    @Override
    public void onCreate(){
        UAirship.takeOff(this);
        PushManager.enablePush();
        String apid = PushManager.shared().getAPID();
        Log.e("engagetest", "My Application onCreate - App APID: " + apid);
        PushManager.shared().setIntentReceiver(PushReceiver.class);

        super.onCreate();

        EngageConfig.storePrimaryUserId(getApplicationContext(), "secret@test.com");
    }

}
