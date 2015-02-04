package com.silverpop.engage;

import android.content.Context;
import com.silverpop.engage.config.EngageConfigManager;

/**
 * Created by Lindsay Thurmond on 1/5/15.
 */
public abstract class BaseManager {

    private Context context;

    protected BaseManager(Context context) {
        this.context = context;
    }

    protected  EngageConfigManager getEngageConfigManager() {
        return EngageConfigManager.get(context);
    }

    protected XMLAPIManager getXMLAPIManager() {
        return XMLAPIManager.get();
    }

    protected UBFManager getUBFManager() {
        return UBFManager.get();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
