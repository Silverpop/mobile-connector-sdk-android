package com.silverpop.engage.util;

import android.os.*;

/**
 * Created by jeremydyer on 6/4/14.
 */
public abstract class TimedAsyncTask<Params, Progress, Result>
    extends AsyncTask<Params, Progress, Result> {

    private long expirationTimestamp = -1;

    public void setExpiresInSeconds(long expirationSeconds) {
        expirationTimestamp = System.currentTimeMillis() + (expirationSeconds * 1000);
    }

    public boolean isExpired() {
        if (expirationTimestamp > 0) {
            if (System.currentTimeMillis() >= expirationTimestamp) {
                return true;
            }
        }
        return false;
    }

    protected abstract Result doInBackground(Params... objects);
}
