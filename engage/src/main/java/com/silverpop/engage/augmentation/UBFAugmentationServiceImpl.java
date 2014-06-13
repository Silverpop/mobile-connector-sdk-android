package com.silverpop.engage.augmentation;

import android.content.Context;
import android.util.Log;

import com.silverpop.engage.augmentation.plugin.UBFAugmentationPlugin;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.network.UBFClient;
import com.silverpop.engage.store.EngageLocalEventStore;
import com.silverpop.engage.util.TimedAsyncTask;

import java.util.ArrayList;

/**
 * Created by jeremydyer on 6/3/14.
 */
public class UBFAugmentationServiceImpl
    implements UBFAugmentationService {

    private static final String TAG = UBFAugmentationServiceImpl.class.getName();

    private static UBFAugmentationServiceImpl augmentationService = null;
    private Context mAppContext = null;
    private EngageLocalEventStore engageLocalEventStore = null;
    private int maxCacheSize = 3;
    private UBFClient ubfClient = null;

    private final ArrayList<UBFAugmentationPlugin> plugins = new ArrayList<UBFAugmentationPlugin>();

    @Override
    public int augmentorsCount() {
        if (plugins == null) {
            return 0 ;
        }
        return plugins.size();
    }

    private UBFAugmentationServiceImpl(Context context) {
        mAppContext = context;
        engageLocalEventStore = EngageLocalEventStore.get(context);
        maxCacheSize = EngageConfigManager.get(context).ubfEventCacheSize();
        ubfClient = UBFClient.get(context);

        //Adds the Augmentation plugins
        String[] augmentationPlugins = EngageConfigManager.get(context).augmentationPluginClasses();
        for (String augmentationClass : augmentationPlugins) {
            try {
                Class augClazz = Class.forName(augmentationClass);
                UBFAugmentationPlugin augmentationPlugin = (UBFAugmentationPlugin) augClazz.newInstance();
                augmentationPlugin.setContext(context);
                plugins.add(augmentationPlugin);
            } catch (Exception ex) {
                Log.w(TAG, "Unable to initialize Pluggable Augmentation class '"
                        + augmentationClass + "' : " + ex.getMessage());
            }
        }
    }

    public static UBFAugmentationServiceImpl get(Context context ){
        if (augmentationService == null) {
            augmentationService = new UBFAugmentationServiceImpl(context);
        }
        return augmentationService;
    }


    public void augmentUBFEvent(final UBF ubfEvent, final EngageEvent engageEvent, final long expirationSeconds) {

        if (ubfEvent != null && engageEvent != null) {
            final TimedAsyncTask<Void, Void, Void> task = new TimedAsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... objects) {

                    ArrayList<UBFAugmentationPlugin> notProcessedPlugins = new ArrayList<UBFAugmentationPlugin>();
                    for (UBFAugmentationPlugin plugin : plugins) {
                        notProcessedPlugins.add(plugin);
                    }

                    int index = 0;
                    UBF mutEvent = ubfEvent;
                    while (!isExpired() && notProcessedPlugins.size() > 0) {
                        if (index >= notProcessedPlugins.size()) {
                            index = 0;
                        }

                        UBFAugmentationPlugin plugin = notProcessedPlugins.get(index);
                        if (plugin.isSupplementalDataReady()) {
                            mutEvent = plugin.process(mutEvent);
                            notProcessedPlugins.remove(plugin);
                            //Index does not need to be updated since the list size has decreased by one.
                        } else if (!plugin.processSyncronously()) {
                            index++;
                        }
                        //Else we don't wait to update the index because we must wait until complete or timeout.
                    }

                    engageEvent.setEventJson(mutEvent.toJSONString());

                    //Save the event with the appropriate status.
                    if (notProcessedPlugins.size() == 0) {
                        //Save the event with a ready to post state.
                        engageEvent.setEventStatus(EngageEvent.READY_TO_POST);
                    } else {
                        //Save the event with an expired state.
                        engageEvent.setEventStatus(EngageEvent.EXPIRED);
                    }

                    engageLocalEventStore.saveUBFEvent(engageEvent);

                    if (engageLocalEventStore.countEventsReadyToPost() >= maxCacheSize) {
                        Log.d(TAG, "Local UBF cache limit has been reached. Posting events to Silverpop");
                        ubfClient.postUBFEngageEvents(null, null);
                    }

                    return null;
                }
            };
            task.setExpiresInSeconds(expirationSeconds);
            task.execute();
        }
    }

}
