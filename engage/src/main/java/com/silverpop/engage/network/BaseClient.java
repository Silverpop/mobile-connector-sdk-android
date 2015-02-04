package com.silverpop.engage.network;

import android.content.Context;

/**
 * Created by Lindsay Thurmond on 1/5/15.
 */
public abstract class BaseClient {

    private Context context;

    protected BaseClient(Context context) {
        this.context = context;
    }

    protected EngageConnectionManager connectionManager() {
        return EngageConnectionManager.get();
    }

    protected Context getContext() {
        return this.context;
    }

}
