package com.silverpop.engage.augmentation;

import com.silverpop.engage.domain.EngageEvent;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/3/14.
 */
public interface UBFAugmentationService {

    public int augmentorsCount();

    public void augmentUBFEvent(UBF ubfEvent, EngageEvent engageEvent, long expirationSeconds);
}
