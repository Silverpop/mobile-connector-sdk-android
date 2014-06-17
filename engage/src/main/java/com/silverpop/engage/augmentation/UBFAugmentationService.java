package com.silverpop.engage.augmentation;

import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/3/14.
 */
public interface UBFAugmentationService {

    /**
     * Augments a UBF object by processing it through all of the "plugins" configured to run
     * in the SDK environment.
     *
     * @param ubfEvent
     *      UBF event object.
     *
     * @param engageEvent
     *      EngageEvent object
     *
     * @param expirationSeconds
     *      Seconds before the augmentation process times out and the
     *      event is sent without the Augmented data.
     */
    public void augmentUBFEvent(UBF ubfEvent, EngageEvent engageEvent, long expirationSeconds);
}
