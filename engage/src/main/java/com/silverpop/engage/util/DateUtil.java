package com.silverpop.engage.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Lindsay Thurmond on 1/12/15.
 */
public class DateUtil {

    public static String toGmtString(Date date){
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sd.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sd.format(date);
    }

}
