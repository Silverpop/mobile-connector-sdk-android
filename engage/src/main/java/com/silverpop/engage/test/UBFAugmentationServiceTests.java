package com.silverpop.engage.test;

import android.test.AndroidTestCase;

import com.silverpop.engage.augmentation.UBFAugmentationService;
import com.silverpop.engage.augmentation.impl.UBFAugmentationServiceImpl;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.domain.UBF;

/**
 * Created by jeremydyer on 6/3/14.
 */
public class UBFAugmentationServiceTests
        extends AndroidTestCase {

    public void testAugmentation() throws InterruptedException {
        UBFAugmentationService service = UBFAugmentationServiceImpl.get(getContext());

        UBF installed = UBF.installed(getContext(), null);
        service.augmentUBFEvent(installed, installed.toEngageEvent(), 2);

        Thread.sleep(1500000);
    }

}