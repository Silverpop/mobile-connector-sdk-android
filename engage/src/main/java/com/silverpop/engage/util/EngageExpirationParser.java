package com.silverpop.engage.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeremydyer on 6/2/14.
 */
public class EngageExpirationParser {

    private Date fromDate;
    private Date expiresAtDate;

    private int dayValue;
    private int hourValue;
    private int minuteValue;
    private int secondValue;

    private static final String VALID_FOR_REGEX_PATTERN = "(\\d+\\s*[d|h|m|s|D|H|M|S])";
    private static final String EXPIRATION_DATE_REGEX_PATTERN = "(\\d{4}/\\d{2}/\\d{2}\\s{1}\\d{2}:\\d{2}:\\d{2})";
    private static final String VALUE_REGEX_PATTERN = "(\\d+)";
    private static final String ENGAGE_DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";

    private static Pattern validForPattern;
    private static Pattern expiresAtPattern;
    private static Pattern valuePattern;
    private static SimpleDateFormat engageDateFormat;

    static {
        validForPattern = Pattern.compile(VALID_FOR_REGEX_PATTERN);
        expiresAtPattern = Pattern.compile(EXPIRATION_DATE_REGEX_PATTERN);
        valuePattern = Pattern.compile(VALUE_REGEX_PATTERN);
        engageDateFormat = new SimpleDateFormat(ENGAGE_DATE_PATTERN);
        engageDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public EngageExpirationParser(String expirationString, Date startFromDate) {

        fromDate = startFromDate;
        dayValue = -1;
        hourValue = -1;
        minuteValue = -1;
        secondValue = -1;
        expiresAtDate = null;

        Matcher match = validForPattern.matcher(expirationString);

        boolean foundMatch = false;
        while (match.find()) {
            foundMatch = true;
            match.reset();
            break;
        }

        if (!foundMatch) {
            match = expiresAtPattern.matcher(expirationString);
        }

        while (match.find()) {
            String result = match.group();

            if (result.contains("d")) {
                dayValue = valueFromRegexResult(result);
            } else if (result.contains("h")) {
                hourValue = valueFromRegexResult(result);
            } else if (result.contains("m")) {
                minuteValue = valueFromRegexResult(result);
            } else if (result.contains("s")) {
                secondValue = valueFromRegexResult(result);
            } else if (result.contains("/")) {
                try {
                    expiresAtDate = engageDateFormat.parse(result);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("EngageExpirationParser Regex result '" + result + "' does not match any pattern");
            }
        }

        //If the expiration date was not already set by the parameter than it is set here.
        if (expiresAtDate == null) {
            //Creates the actual expiration date.
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            if (fromDate != null) {
                cal.setTime(fromDate);
            }

            if (dayValue > -1) cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) + dayValue));
            if (hourValue > -1) cal.set(Calendar.HOUR_OF_DAY, (cal.get(Calendar.HOUR_OF_DAY) + hourValue));
            if (minuteValue > -1) cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE) + minuteValue));
            if (secondValue > -1) cal.set(Calendar.SECOND, (cal.get(Calendar.SECOND) + secondValue));
            expiresAtDate = cal.getTime();
        }
    }


    /**
     * Gets the value for the match.
     *
     * @param result
     *      Integer match value.
     *
     * @return
     *      Value for the date element.
     */
    private Integer valueFromRegexResult(String result) {
        Matcher match = valuePattern.matcher(result);
        match.find();
        return new Integer(match.group());
    }


    public long expirationTimeStamp() {
        return expiresAtDate.getTime();
    }

    public Date expirationDate() {
        return expiresAtDate;
    }

    public long secondsParsedFromExpiration() {
        long seconds = 0;
        if (dayValue > -1) seconds = dayValue * 86400;
        if (hourValue > -1) seconds = seconds + hourValue * 3600;
        if (minuteValue > -1) seconds = seconds + minuteValue * 60;
        if (secondValue > -1) seconds = seconds + secondValue;
        return seconds;
    }

    public long millisecondsParsedFromExpiration() {
        return (secondsParsedFromExpiration() * 1000);
    }
}
