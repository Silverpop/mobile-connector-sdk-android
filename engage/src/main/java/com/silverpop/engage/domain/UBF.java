package com.silverpop.engage.domain;

import android.app.Notification;
import android.content.Context;
import android.util.Log;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class UBF
    implements JSONable {

    public static final int EXPECTED_CORE_TEMPLATE_SIZE = 9;

    public static final int INSTALLED = 12;
    public static final int SESSION_STARTED = 13;
    public static final int SESSION_ENDED = 14;
    public static final int GOAL_ABANDONED = 15;
    public static final int GOAL_COMPLETED = 16;
    public static final int NAMED_EVENT = 17;
    public static final int RECEIVED_NOTIFICATION = 48;
    public static final int OPENED_NOTIFICATION = 49;

    private int code;
    private Date eventTimestamp;

    private Map<String, Object> params;
    private Map<String, Object> coreTemplate;

    private static final SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        rfc3339.setTimeZone(utc);
    }

    public UBF(Context context, int code, Map<String, Object> params) {
        setCode(code);
        setParams(params);
        setEventTimestamp(new Date());
        setCoreTemplate(initCoreTemplate(context));
    }

    private Map<String, Object> initCoreTemplate(Context context) {
        Map<String, Object> core = new HashMap<String, Object>();

        core.put("Device Name", EngageConfig.deviceName());
        core.put("Device Version", EngageConfig.deviceVersion());
        core.put("OS Name", EngageConfig.osName(context));
        core.put("OS Version", EngageConfig.osVersion(context));
        core.put("App Name", EngageConfig.appName(context));
        core.put("App Version", EngageConfig.appVersion(context));
        core.put("Device Id", EngageConfig.deviceId(context));
        core.put("Primary User Id", EngageConfig.primaryUserId(context));
        core.put("Anonymous Id", EngageConfig.anonymousUserId(context));

        return core;
    }

    private JSONArray initAttributes(Map<String, Object> params) {
        JSONArray jsonArray = new JSONArray();

        //Adds the core template values.
        if (getCoreTemplate() != null) {
            for (Map.Entry<String, Object> entry : getCoreTemplate().entrySet()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", entry.getKey());
                    obj.put("value", entry.getValue() == null ? "" : entry.getValue());
                    jsonArray.put(obj);
                } catch (JSONException jex) {
                    Log.e(this.getClass().getName(), jex.getMessage());
                }
            }
        }

        //Add any extra params
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", entry.getKey());
                    obj.put("value", entry.getValue() == null ? "" : entry.getValue());
                    jsonArray.put(obj);
                } catch (JSONException jex) {
                    Log.e(this.getClass().getName(), jex.getMessage());
                }
            }
        }

        return jsonArray;
    }

    public static UBF createUBFEvent(Context context, int eventCode, Map<String, Object> params) {
        return new UBF(context, eventCode, params);
    }

    public static UBF installed(Context context, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);
        if (!params.containsKey(cm.ubfLastCampaignFieldName())) {
            params.put(cm.ubfLastCampaignFieldName(), EngageConfig.lastCampaign(context));
        }
        return new UBF(context, INSTALLED, params);
    }

    public static UBF sessionStarted(Context context, Map<String, Object> params, String campaignName) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);
        if (campaignName != null && campaignName.length() > 0) {
            params.put(cm.ubfCurrentCampaignFieldName(), campaignName);
        } else {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        return new UBF(context, SESSION_STARTED, params);
    }

    public static UBF sessionEnded(Context context, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);
        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        return new UBF(context, SESSION_ENDED, params);
    }

    public static UBF goalAbandoned(Context context, String goalName, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);
        if (!params.containsKey(cm.ubfGoalNameFieldName())) {
            params.put(cm.ubfGoalNameFieldName(), goalName);
        }
        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        return new UBF(context, GOAL_ABANDONED, params);
    }

    public static UBF goalCompleted(Context context, String goalName, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);
        if (!params.containsKey(cm.ubfGoalNameFieldName())) {
            params.put(cm.ubfGoalNameFieldName(), goalName);
        }
        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        return new UBF(context, GOAL_COMPLETED, params);
    }

    public static UBF namedEvent(Context context, String eventName, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);

        if (!params.containsKey(cm.ubfEventNameFieldName())) {
            params.put(cm.ubfEventNameFieldName(), eventName);
        }
        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        return new UBF(context, NAMED_EVENT, params);
    }

    public static UBF receivedNotification(Context context, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);

        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        if (!params.containsKey(cm.ubfCallToActionFieldName())) {
            params.put(cm.ubfCallToActionFieldName(), params.get(cm.ubfCallToActionFieldName()));   //User must provide the Call To Action.
        }

        UBF ubf = new UBF(context, RECEIVED_NOTIFICATION, params);
        return ubf;
    }

    public static UBF receivedNotification(Context context, Notification notification, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);

        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        if (!params.containsKey(cm.ubfCallToActionFieldName())) {
            params.put(cm.ubfCallToActionFieldName(), params.get(cm.ubfCallToActionFieldName()));   //User must provide the Call To Action.
        }
        if (notification.tickerText != null) {
            if (!params.containsKey(cm.ubfDisplayedMessageFieldName())) {
                params.put(cm.ubfDisplayedMessageFieldName(), notification.tickerText.toString());
            }
        } else {
            if (!params.containsKey(cm.ubfDisplayedMessageFieldName())) {
                params.put(cm.ubfDisplayedMessageFieldName(), "");
            }
        }

        UBF ubf = new UBF(context, RECEIVED_NOTIFICATION, params);
        return ubf;
    }

    public static UBF deepLinkOpened(Context context, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);

        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        if (!params.containsKey(cm.ubfCallToActionFieldName())) {
            params.put(cm.ubfCallToActionFieldName(), params.get(cm.ubfCallToActionFieldName()));   //User must provide the Call To Action.
        }

        UBF ubf = new UBF(context, OPENED_NOTIFICATION, params);
        return ubf;
    }

    public static UBF openedNotification(Context context, Notification notification, Map<String, Object> params) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        params = populateEventCommonParams(context, params);

        if (!params.containsKey(cm.ubfCurrentCampaignFieldName())) {
            params.put(cm.ubfCurrentCampaignFieldName(), EngageConfig.currentCampaign(context));
        }
        if (!params.containsKey(cm.ubfCallToActionFieldName())) {
            params.put(cm.ubfCallToActionFieldName(), params.get(cm.ubfCallToActionFieldName()));   //User must provide the Call To Action.
        }
        if (notification.tickerText != null) {
            if (!params.containsKey(cm.ubfDisplayedMessageFieldName())) {
                params.put(cm.ubfDisplayedMessageFieldName(), notification.tickerText.toString());
            }
        } else {
            if (!params.containsKey(cm.ubfDisplayedMessageFieldName())) {
                params.put(cm.ubfDisplayedMessageFieldName(), "");
            }
        }

        UBF ubf = new UBF(context, OPENED_NOTIFICATION, params);
        return ubf;
    }

    private static Map<String, Object> populateEventCommonParams(Context context, Map<String, Object> params) {
        //Populates the tag parameters since all UBF events contain those fields.
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        return params;
    }

    public static Map<String, Object> addDelimitedTagsToParams(Context context, Map<String, Object> params, String[] tags) {
        EngageConfigManager cm = EngageConfigManager.get(context);
        if (params.containsKey(cm.ubfTagsFieldName())) {
            String param = (String) params.get(cm.ubfTagsFieldName());
            params.put(cm.ubfTagsFieldName(), convertToCommaDelimited(param, tags));
        } else {
            if (tags != null) {
                params.put(cm.ubfTagsFieldName(), convertToCommaDelimited(null, tags));
            } else {
                params.put(cm.ubfTagsFieldName(), "");
            }
        }
        return params;
    }

    private static String convertToCommaDelimited(String existingDelimitedString, String[] list) {
        StringBuffer ret = new StringBuffer("");
        if (existingDelimitedString != null) {
            ret.append(existingDelimitedString);
            if (!existingDelimitedString.endsWith(",")) {
                ret.append(",");
            }
        }
        for (int i = 0; list != null && i < list.length; i++) {
            ret.append(list[i]);
            if (i < list.length - 1) {
                ret.append(',');
            }
        }
        return ret.toString();
    }

    public void addParam(String key, Object value) {
        params.put(key, value);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("eventTypeCode", this.getCode());
            jo.put("eventTimestamp", rfc3339.format(this.getEventTimestamp()));
            jo.put("attributes", initAttributes(getParams()));
        } catch (JSONException jsonEx) {
            Log.e(this.getClass().getName(), jsonEx.getMessage());
        }
        return jo;
    }

    public String toJSONString() {
        JSONObject obj = this.toJSONObject();
        if (obj != null) {
            return obj.toString();
        } else {
            Log.w(this.getClass().getName(), "UBF JSONObject instance was null. Empty JSON string will be returned!");
            return "";
        }
    }

    public EngageEvent toEngageEvent() {
        return new EngageEvent(this);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getCoreTemplate() {
        return coreTemplate;
    }

    public void setCoreTemplate(Map<String, Object> coreTemplate) {
        this.coreTemplate = coreTemplate;
    }

    public Date getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Date eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
}
