package com.silverpop.engage.augmentation.plugin;

import android.content.Context;

import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/3/14.
 */
public interface UBFAugmentationPlugin {

    /**
     * Injects the Android Context into the plugin.
     * @param context
     */
    public void setContext(Context context);

    /**
     * Informs in the required data to Augment the UBF event is currently available or not.
     *
     * @return
     *  true if yes and false otherwise.
     */
    public boolean isSupplementalDataReady();

    /**
     * Do down stream events rely on this augmentation data. If yes the do not continue
     * processing other augmentors until this augmentation has completed.
     *
     * @return
     *      True if must wait until this processing completes and false otherwise.
     */
    public boolean processSyncronously();

    /**
     * Augments the UBF event with the data.
     *
     * @param ubf
     * @return
     */
    public UBF process(UBF ubf);
}
