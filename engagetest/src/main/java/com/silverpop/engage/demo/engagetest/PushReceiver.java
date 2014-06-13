package com.silverpop.engage.demo.engagetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.urbanairship.push.PushManager;

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (PushManager.ACTION_PUSH_RECEIVED.equals(intent.getAction())) {
            Log.e("engagetest", "recieved notification");
        } else if (PushManager.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.e("engagetest", "opened notification");
        }
    }
}
