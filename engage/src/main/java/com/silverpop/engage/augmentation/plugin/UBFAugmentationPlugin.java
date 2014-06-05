package com.silverpop.engage.augmentation.plugin;

import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/3/14.
 */
public interface UBFAugmentationPlugin {

    public boolean isSupplementalDataReady();

    public UBF process(UBF ubf);
}
