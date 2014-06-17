package com.silverpop.engage.domain;

import android.app.Notification;
import android.test.AndroidTestCase;

import com.silverpop.engage.domain.UBF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class UBFTests
        extends AndroidTestCase {

    private static final Map<String, Boolean> REQ_FIELDS = new HashMap<String, Boolean>();

    static {
        REQ_FIELDS.put("Device Name", Boolean.TRUE);
        REQ_FIELDS.put("Device Version", Boolean.TRUE);
        REQ_FIELDS.put("OS Name", Boolean.TRUE);
        REQ_FIELDS.put("OS Version", Boolean.TRUE);
        REQ_FIELDS.put("App Name", Boolean.TRUE);
        REQ_FIELDS.put("App Version", Boolean.TRUE);
        REQ_FIELDS.put("Device Id", Boolean.TRUE);
        REQ_FIELDS.put("Primary User Id", Boolean.FALSE);
        REQ_FIELDS.put("Anonymous Id", Boolean.FALSE);
    }

    public void testUBFCreation() {

        final Map<String, Object> eventParams = null;

        UBF ubf = new UBF(getContext(), UBF.INSTALLED, eventParams);
        assertTrue(ubf != null);
        assertTrue(ubf.getCode() == UBF.INSTALLED);
        assertTrue(ubf.getParams() == null);
        assertTrue(ubf.getCoreTemplate() != null && ubf.getCoreTemplate().size() > 0);
    }

    
    public void testValidCoreTemplate() {
        UBF ubf = new UBF(getContext(), UBF.INSTALLED, null);
        assertTrue(ubf.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(isCoreTemplateValid(ubf));
    }

    
    public void testCreateEventWithCode() {
        assertTrue(false);
    }

    public void testUBFJSONSerialization() throws JSONException {

        ArrayList<UBF> events = createTestNullParamUBFEvents();
        for (UBF ubf : events) {
            JSONObject obj = ubf.toJSONObject();

            assertTrue(obj != null);
            assertTrue(obj.has("eventTypeCode"));
            assertTrue(obj.has("eventTimestamp"));
            assertTrue(obj.has("attributes"));

            JSONArray atts = obj.getJSONArray("attributes");
            assertTrue(atts.length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        }
    }

    
    public void testUBFJSONDeSerialization() {
        assertTrue(false);
    }

    
    public void testUBFInstalled() throws JSONException {
        UBF installed = UBF.installed(getContext(), null);
        assertTrue(installed != null);
        assertTrue(installed.getCode() == UBF.INSTALLED);
        //assertTrue(installed.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(installed.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(installed.getEventTimestamp() != null);
        assertTrue(installed.toJSONObject() != null && installed.toJSONObject().length() > 0);
        assertTrue(installed.toJSONString() != null && installed.toJSONString().length() > 0
            && installed.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(installed.getAttributes(), "Last Campaign"));
    }

    
    public void testUBFSessionStarted() throws JSONException {
        UBF sessionStarted = UBF.sessionStarted(getContext(), null, "UnitTestCampaignName");
        assertTrue(sessionStarted != null);
        assertTrue(sessionStarted.getCode() == UBF.SESSION_STARTED);
        //assertTrue(sessionStarted.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(sessionStarted.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(sessionStarted.getEventTimestamp() != null);
        assertTrue(sessionStarted.toJSONObject() != null && sessionStarted.toJSONObject().length() > 0);
        assertTrue(sessionStarted.toJSONString() != null && sessionStarted.toJSONString().length() > 0
                && sessionStarted.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(sessionStarted.getAttributes(), "Campaign Name"));
    }

    
    public void testUBFSessionEnded() throws JSONException {
        UBF sessionEnded = UBF.sessionEnded(getContext(), null);
        assertTrue(sessionEnded != null);
        assertTrue(sessionEnded.getCode() == UBF.SESSION_ENDED);
        //assertTrue(sessionEnded.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(sessionEnded.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(sessionEnded.getEventTimestamp() != null);
        assertTrue(sessionEnded.toJSONObject() != null && sessionEnded.toJSONObject().length() > 0);
        assertTrue(sessionEnded.toJSONString() != null && sessionEnded.toJSONString().length() > 0
                && sessionEnded.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(sessionEnded.getAttributes(), "Campaign Name"));
    }

    
    public void testUBFGoalAbandoned() throws JSONException {
        UBF goalAbandoned = UBF.goalAbandoned(getContext(), "Unit Test Goal", null);
        assertTrue(goalAbandoned != null);
        assertTrue(goalAbandoned.getCode() == UBF.GOAL_ABANDONED);
        //assertTrue(goalAbandoned.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(goalAbandoned.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(goalAbandoned.getEventTimestamp() != null);
        assertTrue(goalAbandoned.toJSONObject() != null && goalAbandoned.toJSONObject().length() > 0);
        assertTrue(goalAbandoned.toJSONString() != null && goalAbandoned.toJSONString().length() > 0
                && goalAbandoned.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(goalAbandoned.getAttributes(), "Goal Name"));
        //assertTrue(JSONArrayContainsName(goalAbandoned.getAttributes(), "Campaign Name"));
    }

    
    public void testUBFGoalCompleted() throws JSONException {
        UBF goalCompleted = UBF.goalCompleted(getContext(), "Unit Test Goal", null);
        assertTrue(goalCompleted != null);
        assertTrue(goalCompleted.getCode() == UBF.GOAL_COMPLETED);
        //assertTrue(goalCompleted.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(goalCompleted.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(goalCompleted.getEventTimestamp() != null);
        assertTrue(goalCompleted.toJSONObject() != null && goalCompleted.toJSONObject().length() > 0);
        assertTrue(goalCompleted.toJSONString() != null && goalCompleted.toJSONString().length() > 0
                && goalCompleted.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(goalCompleted.getAttributes(), "Goal Name"));
        //assertTrue(JSONArrayContainsName(goalCompleted.getAttributes(), "Campaign Name"));
    }

    
    public void testUBFNamedEvent() throws JSONException {
        UBF namedEvent = UBF.namedEvent(getContext(), "Unit Test Goal", null);
        assertTrue(namedEvent != null);
        assertTrue(namedEvent.getCode() == UBF.NAMED_EVENT);
        //assertTrue(namedEvent.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(namedEvent.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(namedEvent.getEventTimestamp() != null);
        assertTrue(namedEvent.toJSONObject() != null && namedEvent.toJSONObject().length() > 0);
        assertTrue(namedEvent.toJSONString() != null && namedEvent.toJSONString().length() > 0
                && namedEvent.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(namedEvent.getAttributes(), "Event Name"));
        //assertTrue(JSONArrayContainsName(namedEvent.getAttributes(), "Campaign Name"));
    }

    
    public void testUBFReceivedLocalNotification() throws JSONException {
        Notification notification = new Notification();
        UBF localNotification = UBF.receivedNotification(getContext(), notification, null);
        assertTrue(localNotification != null);
        assertTrue(localNotification.getCode() == UBF.RECEIVED_NOTIFICATION);
        //assertTrue(localNotification.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(localNotification.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(localNotification.getEventTimestamp() != null);
        assertTrue(localNotification.toJSONObject() != null && localNotification.toJSONObject().length() > 0);
        assertTrue(localNotification.toJSONString() != null && localNotification.toJSONString().length() > 0
                && localNotification.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(localNotification.getAttributes(), "Call To Action"));
        //assertTrue(JSONArrayContainsName(localNotification.getAttributes(), "Campaign Name"));
        //assertTrue(JSONArrayContainsName(localNotification.getAttributes(), "Displayed Message"));
    }

    
    public void testUBFReceivedPushNotification() throws JSONException {
        Notification notification = new Notification();
        UBF remoteNotification = UBF.receivedNotification(getContext(), notification, null);
        assertTrue(remoteNotification != null);
        assertTrue(remoteNotification.getCode() == UBF.RECEIVED_NOTIFICATION);
        //assertTrue(remoteNotification.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(remoteNotification.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(remoteNotification.getEventTimestamp() != null);
        assertTrue(remoteNotification.toJSONObject() != null && remoteNotification.toJSONObject().length() > 0);
        assertTrue(remoteNotification.toJSONString() != null && remoteNotification.toJSONString().length() > 0
                && remoteNotification.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(remoteNotification.getAttributes(), "Call To Action"));
        //assertTrue(JSONArrayContainsName(remoteNotification.getAttributes(), "Campaign Name"));
        //assertTrue(JSONArrayContainsName(remoteNotification.getAttributes(), "Displayed Message"));
    }

    
    public void testUBfOpenedNotification() throws JSONException {
        Notification notification = new Notification();
        UBF openedNotification = UBF.openedNotification(getContext(), notification, null);
        assertTrue(openedNotification != null);
        assertTrue(openedNotification.getCode() == UBF.OPENED_NOTIFICATION);
        //assertTrue(openedNotification.getAttributes().length() >= UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(openedNotification.getCoreTemplate().size() == UBF.EXPECTED_CORE_TEMPLATE_SIZE);
        assertTrue(openedNotification.getEventTimestamp() != null);
        assertTrue(openedNotification.toJSONObject() != null && openedNotification.toJSONObject().length() > 0);
        assertTrue(openedNotification.toJSONString() != null && openedNotification.toJSONString().length() > 0
                && openedNotification.toJSONString().contains("{"));
        //assertTrue(JSONArrayContainsName(openedNotification.getAttributes(), "Call To Action"));
        //assertTrue(JSONArrayContainsName(openedNotification.getAttributes(), "Campaign Name"));
        //assertTrue(JSONArrayContainsName(openedNotification.getAttributes(), "Displayed Message"));
    }

    
    public void testUBFDelimitedTags() throws JSONException {
        Map<String, Object> existingParams = new HashMap<String, Object>();
        existingParams.put("Something", "Else");

        String[] tags = new String[]{"UNIT", "TEST", "ENGAGE"};
        Map<String, Object> newParams = UBF.addDelimitedTagsToParams(getContext(), existingParams, tags);
        assertTrue(newParams.containsKey("Something"));
        assertTrue(newParams.containsKey("Tags"));
        assertTrue(((String) newParams.get("Tags")).equals("UNIT,TEST,ENGAGE"));

        existingParams = new HashMap<String, Object>();
        existingParams.put("Something", "Else");
        existingParams.put("Tags", "BEFORE");

        tags = new String[]{"UNIT", "TEST", "ENGAGE"};
        newParams = UBF.addDelimitedTagsToParams(getContext(), existingParams, tags);
        assertTrue(newParams.containsKey("Something"));
        assertTrue(newParams.containsKey("Tags"));
        assertTrue(((String) newParams.get("Tags")).equals("BEFORE,UNIT,TEST,ENGAGE"));
    }

    
    public void testUBFLocation() throws JSONException {
        assertTrue(false);
    }


    /**
     * Creates some test UBF events with NULL parameters.
     *
     * @return
     *      ArrayList of test UBF events.
     */
    private ArrayList<UBF> createTestNullParamUBFEvents() {
        ArrayList<UBF> events = new ArrayList<UBF>();
        events.add(UBF.installed(getContext(), null));
        events.add(UBF.sessionStarted(getContext(), null, "UnitTestCampaignName"));
        events.add(UBF.sessionEnded(getContext(), null));
        events.add(UBF.goalAbandoned(getContext(), "UnitTestGoal", null));
        events.add(UBF.goalCompleted(getContext(), "UnitTestGoal", null));
        events.add(UBF.namedEvent(getContext(), "UnitTestGoal", null));
        return events;
    }

    /**
     * Examines the Core template values and makes sure that the ones which are required
     * are present.
     *
     * @param ubf
     *
     * @return
     *      True if the Core Template is valid and false otherwise.
     */
    private boolean isCoreTemplateValid(UBF ubf) {
        boolean valid = true;
        if (ubf != null) {
            for (Map.Entry<String, Object> entry : ubf.getCoreTemplate().entrySet()) {
                String entryString = (String) entry.getValue();
                Boolean required = REQ_FIELDS.get(entry.getKey());

                if (entryString != null && entryString.length() > 0 && required) {
                    valid = false;
                    break;
                }
            }
        } else {
            valid = false;
        }
        return valid;
    }

    /**
     * Determines if a JSONArray contains a particular name field.
     *
     * @param array
     * @param name
     * @return
     * @throws JSONException
     */
    private boolean JSONArrayContainsName(JSONArray array, String name) throws JSONException {
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.get("name").equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

}
