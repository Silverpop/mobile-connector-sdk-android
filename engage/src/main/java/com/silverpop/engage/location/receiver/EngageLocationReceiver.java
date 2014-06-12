package com.silverpop.engage.location.receiver;

import android.content.Context;
import android.content.Intent;

/**
 * Created by jeremydyer on 6/2/14.
 */
public interface EngageLocationReceiver {

    /**
     * Invoked when a location update BroadcastMessage is received.
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent);

}
