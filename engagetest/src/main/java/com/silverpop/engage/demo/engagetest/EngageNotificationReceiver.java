package com.silverpop.engage.demo.engagetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jeremydyer on 6/6/14.
 */
public class EngageNotificationReceiver
    extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("something");
    }
}
