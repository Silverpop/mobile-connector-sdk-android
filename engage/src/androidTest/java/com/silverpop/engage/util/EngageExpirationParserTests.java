package com.silverpop.engage.util;

import android.test.AndroidTestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jeremydyer on 6/2/14.
 */
public class EngageExpirationParserTests
    extends AndroidTestCase {

    private Date beforeDate;

    protected void setUp() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        beforeDate = cal.getTime();
    }

    public void testEngageExpirationParserFullString () {
        EngageExpirationParser parser = new EngageExpirationParser("1 day 7 hours 23 minutes 15 seconds", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 2);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 7);
        assertTrue(cal.get(Calendar.MINUTE) == 23);
        assertTrue(cal.get(Calendar.SECOND) == 15);
    }

    public void testEngageExpirationParserFullStringAbr () {
        EngageExpirationParser parser = new EngageExpirationParser("1 d 7 h 23 m 15 s", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 2);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 7);
        assertTrue(cal.get(Calendar.MINUTE) == 23);
        assertTrue(cal.get(Calendar.SECOND) == 15);
    }

    public void testEngageExpirationParserDayOnly () {
        EngageExpirationParser parser = new EngageExpirationParser("1 d", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 2);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 0);
        assertTrue(cal.get(Calendar.MINUTE) == 0);
        assertTrue(cal.get(Calendar.SECOND) == 0);
    }

    public void testEngageExpirationParserHourOnly () {
        EngageExpirationParser parser = new EngageExpirationParser("14h", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 1);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 14);
        assertTrue(cal.get(Calendar.MINUTE) == 0);
        assertTrue(cal.get(Calendar.SECOND) == 0);
    }

    public void testEngageExpirationParserMinuteOnly () {
        EngageExpirationParser parser = new EngageExpirationParser("12m", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 1);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 0);
        assertTrue(cal.get(Calendar.MINUTE) == 12);
        assertTrue(cal.get(Calendar.SECOND) == 0);
    }

    public void testEngageExpirationParserSecondOnly () {
        EngageExpirationParser parser = new EngageExpirationParser("47 secs", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 1);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 0);
        assertTrue(cal.get(Calendar.MINUTE) == 0);
        assertTrue(cal.get(Calendar.SECOND) == 47);
    }

    public void testEngageExpirationParserMinuteRollover () {
        EngageExpirationParser parser = new EngageExpirationParser("67 mins 47s", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 0);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 1);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 1);
        assertTrue(cal.get(Calendar.MINUTE) == 7);
        assertTrue(cal.get(Calendar.SECOND) == 47);
    }

    public void testEngageExpirationParserExpiresAtDate () {
        EngageExpirationParser parser = new EngageExpirationParser("2014/02/14 16:25:43 ", beforeDate);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(parser.expirationDate());

        assertTrue(cal.get(Calendar.YEAR) == 2014);
        assertTrue(cal.get(Calendar.MONTH) == 1);
        assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 14);
        assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 16);
        assertTrue(cal.get(Calendar.MINUTE) == 25);
        assertTrue(cal.get(Calendar.SECOND) == 43);
    }


}
