package com.silverpop.engage.util;

import android.test.AndroidTestCase;

import java.util.Date;

public class DateUtilTest extends AndroidTestCase {

    public void testToGmtString() throws Exception {

        String dateString = DateUtil.toGmtString(new Date());
        assertNotNull(dateString);
    }
}