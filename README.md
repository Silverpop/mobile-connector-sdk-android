Silverpop EngageSDK-Android
===========================

Silverpop Engage SDK for Android (a.k.a. the "Silverpop Mobile Connector")

## Engage Database wrapper for Android 

EngageSDK is a Engage API wrapper library for Android development.
The goal is to provide a library that is simple to setup and use for communicating remotely with 
our Silverpop Engage Database system.

## Features

EngageSDK is a wrapper for the Engage Database XMLAPI and JSON Universal Events. 
The SDK assists developers in interacting with both the XMLAPI and JSON Universal Events (UBF) web services. 
All interaction with the Engage web services require that you first establish a secure connection 
with Engage via the OAuth 2 credentials you receive from the Engage Portal. Although XMLAPI and UBF 
share certain components the SDK divides the interaction with each module into separate components 
namely UBFManager and XMLAPIManager. 

## Getting Started 
The first thing you will want to do is contact your Relationship Manager at Silverpop and ask for the "Silverpop Mobile Connector".  They will assist in getting your Engage account provisioned for Universal Behaviors -- the new flexible event tracking system that is the backbone of tracked mobile app behaviors.

Next, you can follow the instructions in this readme file, or as an additional offer, we've put together a short 10 minute tutorial that will walk you through the download, installation, and configuration process to get your app up and running.  [Click here](https://kb.silverpop.com/kb/engage/Silverpop_Mobile_Connector_-_***NEW***/Video_Tutorial%3A_Up_and_Running_in_10_mins!) to watch that video tutorial within our KnowledgeBase.

## Integrating Application with EngageSDK

### Installing SDK

The Android EngageSDK is packaged as an Android archivable resource (.aar). An .aar file is basically a 
regular jar file but contains other Android bundled resources such as a base Android.manifest file.
You can include the EngageSDK into a new or existing application by adding a gradle dependency of 

```
compile(group: 'com.silverpop', name: 'engage', version: '1.0.0', ext: 'aar')
```

Adding the dependency will result in the SDK code and all SDK dependencies being pulled into your project.
The SDK AndroidManifest.xml file will also be included in your project and merged into your application's AndroidManifest.xml
file at build time by the android build tools.

Once the SDK code is available for you in your project you need to make some adjustments so that your
application can properly notify the EngageSDK of important lifecycle events. To do that you will create
an application element in your AndroidManifest.xml file. The application element that you create can either
directly use the com.silverpop.engage.EngageApplication or create your own custom Application instance
that extends com.silverpop.engage.EngageApplication. When doing the later it is the responsibility of 
the developer to ensure that they invoke the super of each method they override otherwise certain
SDK functionality may not perform as expected. Creating a custom Application instance and defining 
that in your AndroidManifest.xml file might look like this. The custom Application class is
com.silverpop.engage.demo.engagetest.Application.

```xml
<application
        android:name="com.silverpop.engage.demo.engagetest.Application"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        .....
</application>
```

You must also provide your private credentials received from Silverpop inside the application element
as follows.

```xml
<meta-data android:name="ENGAGE_CLIENT_ID" android:value=${YOUR_ENGAGE_CLIENT_ID} />
<meta-data android:name="ENGAGE_CLIENT_SECRET_META" android:value=${YOUR_ENGAGE_CLIENT_SECRET} />
<meta-data android:name="ENGAGE_REFRESH_TOKEN" android:value=${YOUR_ENGAGE_REFRESH_TOKEN} />
<meta-data android:name="ENGAGE_HOST" android:value=${ENGAGE API URL} />
```

### Before You Release Your App

#### Important Note: Increase Token Limits for Production Apps

There are currently limits placed on the number of Access Tokens that can be generated per hour per 
instance of Engage.  This number is easily increased, however, before deploying your app publicly, 
you must contact your Relationship Manager or Sales Rep regarding your intention to use this connector 
and that you will need to have your OAuth Access Token rate limit increased.

### Universal Behaviors

Universal Events represent application events (ex. installed, session started, etc) or SDK consumer 
defined events (ex. named goal started, named goal abandoned, etc). Those events are stored locally
in a UBF object instance until they are ready to be delivered to Silverpop via a POST with the UBF
object instance JSON serialized payload. You should never attempt to directly create the JSON payload
but rather rely on the UBF class and its methods to manipulate the payload to your needs. More detailed
information on the UBF class can be found [here](#UBF).

#### Capturing UBF Events

UBF events are either captured directly by the SDK or manually by some business logic defined by
the SDK developer [UBF](#UBF). Events that are captured by the EngageSDK are automatically sent
to the Engage API. Manually created UBF events must also be manually submitted to the Engage API 
however. Submitting UBF events to the Engage API should always be performed through the UBFManager
global instance. This global instance has in created by the com.silverpop.engage.EngageApplication
upon application startup and has already been properly authenticated with the Engage API. Creating 
and submitting a manually created UBF event is shown below.

```java
Context context = getActivity();    //This could be differ depending on the location this snippet is invoked from.
Map<String, Object> params = new HashMap<String, Object>(); //Place any desired parameters in this map
UBF namedGoalStarted = UBF.namedEvent(context, "sampleNamedEvent", params);
UBFManager.get().postEvent(namedGoalStarted);
```

#### UBF Sessions

EngageSDK implements predefined Session events for Universal Behaviors. Sessions are configured to timeout 
if a user leaves your app for at least 5 minutes. At the end of the Session, duration is computed excluding 
any portion of inactivity.

#### UBFManager

The UBFManager is a global application singleton that should be used for posting all UBF events to the
Engage API. The UBFManager transparently handles network reachability, event persistence, and client authentication.
The UBFManager ensures that UBF events are durable and makes the best attempts it can to ensure that
UBF events make it to the Engage API. UBF events posted through the UBFManager are persisted in a local
SQLite database and network checks are made before the events are attempted to be posted. UBFManager
will also periodically re-attempt posting failed UBF events to Engage API until the maximum retry 
threshold is met. 

#### <a name="UBF"/> UBF Class

The UBF class represents a client side Universal Behavior JSON message intended for the Engage API.
The class also contains several static convenience methods for creating common UBF events. The UBF is 
broken down into 2 separate "containers" of data. There are the UBF Core Values that are contained
in every single UBF event and then the UBF Param values that are specific to each UBF of type.
Most UBF param values are populated automatically by the SDK but the SDK user can pass those values in
as a parameter and the values they provide will take precedence over the automatic params generated 
by the SDK. 

##### UBF Core Values

* Device Version
* OS Name
* OS Version
* App Name
* App Version
* Device Id
* Mobile User Id/Primary User Id
* Anonymous Id
* Recipient Id

##### UBF Events and their Type Codes

* INSTALLED - 12
* SESSION_STARTED - 13
* SESSION_ENDED - 14
* GOAL_ABANDONED - 15
* GOAL_COMPLETED - 16
* NAMED_EVENT - 17
* RECEIVED_NOTIFICATION - 48
* OPENED_NOTIFICATION - 49

#### <a nam="UBFAugmentation"/>UBF Augmentation

After a UBF event is created and sent the the UBFManager the UBF may also be further augmented with
data received from Android hardware or user defined external services. This functionality maintains
maximum SDK flexibility as it allows the user to define their own Augmentation plugins that augment
the UBF event before it is posted to the Engage API. The SDK by default uses this framework for augmenting
the UBF events with location coordinates and location name for example. Any plugin can be created by the user
by extending the ```com.silverpop.engage.augmentation.plugin.UBFAugmentationPlugin``` interface. Here is a 
simple example of a "weather UBF augmentation plugin"

```java
public class UBFWeatherAugmentationPlugin
    implements UBFAugmentationPlugin {

    private Context mContext;

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    /**
     * Returns false until the data needed to augment the UBF event is ready. For example this event
     * might continually return false until a web service call returns.
     */
    public boolean isSupplementalDataReady() {
        return true;
    }

    @Override
    /**
     * If true it means that the following plugins depend on the data that this augmentor injects
     * into the UBF. We can't continue processing those subsequent plugins until this plugin has
     * completed.
     */
    public boolean processSyncronously() {
        return true;
    }

    @Override
    public UBF process(UBF ubf) {
        FakeExternalWeatherService weatherService = new FakeExternalWeatherService();
        ubf.addParam("outside temperature fahrenheit", weatherService.getTemperatureInFahrenheit());
        ubf.addParam("outside temperature celsius", weatherService.getTemperatureInCelsius());
        return ubf; //UBF will now have temperature data when posting to Engage API
    }
}
```

To prevent UBF events from becoming stagnant or waiting indefinitely for augmentation data a [configurable](#Configuration)
augmentation timeout is placed for a single UBF augmentation. The same timeout applies if you have 1 plugin
or 1000 plugins so tuning to match your needs is expected. After the timeout is reached the UBF event
will be posted to Engage API in the same state as when it was handed off to the augmentation plugin service.

### <a name="XMLAPI"/>XMLAPI

The EngageSDK supports the Engage XMLAPI and also provides several convenience methods for developers.

#### <a name="XMLAPIObject"/>XMLAPI Object/Builder

XMLAPI requests are sent to Engage in XML format. Those XML message are built using the XMLAPI object
in the EngageSDK. 

##### Example 1

```xml
<Envelope>
    <Body>
        <SelectRecipientData>
            <LIST_ID>45654</LIST_ID>
            <EMAIL>someone@adomain.com</EMAIL>
            <COLUMN>
                <NAME>Customer Id</NAME>
                <VALUE>123-45-6789</VALUE>
            </COLUMN>
        </SelectRecipientData>
    </Body>
</Envelope>
```

is equivalent to:

```java
XMLAPI selectRecipientData = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

// XMLAPI top level parameters
selectRecipientData.addParam(XMLAPIElement.LIST_ID.toString(), "45654");
selectRecipientData.addParam(XMLAPIElement.EMAIL.toString(), "someone@adomain.com");

// XMLAPI NAME/VALUE columns
selectRecipientData.addColumn("Customer Id", "123-45-6789");
```
or you can use the XMLAPI Builder class to create the same XML as:

```java
XMLAPI selectRecipientData = XMLAPI.builder()
        .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
        .listId("45654")
        .email("someone@adomain.com")
        .column("Customer Id", "123-45-6789")
        .build();
```

or you can use a combination of the two ways:

```java
// use builder for initial creation
XMLAPI selectRecipientData = XMLAPI.builder()
        .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
        .listId("45654")
        .email("someone@adomain.com")
        .build();

// add additional properties later
selectRecipientData.addColumn("Customer Id", "123-45-6789");
```

##### Example 2

```xml
<Envelope>
    <Body>
        <SelectRecipientData>
            <LIST_ID>45654</LIST_ID>
            <RECIPIENT_ID>702003</RECIPIENT_ID>
        </SelectRecipientData>
    </Body>
</Envelope>
```

is equivalent to:

```java
XMLAPI selectRecipientData = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

// XMLAPI top level parameters
selectRecipientData.addParam(XMLAPIElement.LIST_ID.toString(), "45654");
selectRecipientData.addParam(XMLAPIElement.RECIPIENT_ID.toString(), "702003");
```

or using the builder:

```java
XMLAPI selectRecipientData = XMLAPI.builder() 
        .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
        .listId("45654")
        .recipientId("702003")
        .build();
```

##### Example 3

```xml
<Envelope>
    <Body>
        <SelectRecipientData>
            <LIST_ID>45654</LIST_ID>
            <EMAIL>someone@adomain.com</EMAIL>
        </SelectRecipientData>
    </Body>
</Envelope>
```

is equivalent to:

```java
XMLAPI selectRecipientData = new XMLAPI(XMLAPIOperation.SELECT_RECIPIENT_DATA);

// XMLAPI top level parameters.
selectRecipientData.addParam(XMLAPIElement.LIST_ID.toString(), "45654");
selectRecipientData.addParam(XMLAPIElement.EMAIL.toString(), "someone@adomain.com");
```

or using the builder:

```java
XMLAPI selectRecipientData = XMLAPI.builder()
        .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
        .listId("45654")
        .email("someone@adomain.com")
        .build();
```

#### <a name="XMLAPIManager"/>XMLAPIManager

The XMLAPIManager manages posting XMLAPI messages to the Engage web services. A XMLAPIManager global instance
is automatically created for you by ```java com.silverpop.engage.EngageApplication ```. XMLAPIManager
manages network availability and will cache requests when the network is not reachable executing them
once the network is accessible again. All XMLAPI requests made to Engage should be made through
this XMLAPIManager.

```java
/**
 * Post an XMLAPI request to Engage using a generic handler (as opposed to AsyncTasks) for the response.
 *
 * @param api             XMLAPI operation desired.
 * @param responseHandler functionality to run on success or failure of the request.
 */
public void postXMLAPI(XMLAPI api,
                       final XMLAPIResponseHandler responseHandler) {
    xmlapiClient.postResource(api, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            if (responseHandler != null) {
                responseHandler.onSuccess(new EngageResponseXML(response));
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (responseHandler != null) {
                responseHandler.onFailure(volleyError);
            }
        }
    });
}

/**
 * Post an XMLAPI request to Engage
 *
 * @param api         XMLAPI operation desired.
 * @param successTask AsyncTask to execute on successful result.
 * @param failureTask AsyncTask to execute on failure
 */
public void postXMLAPI(XMLAPI api,
                       final AsyncTask<EngageResponseXML, Void, Object> successTask,
                       final AsyncTask<VolleyError, Void, Object> failureTask) {

    Response.Listener<String> successListener = new Response.Listener<String>() {
        public void onResponse(String response) {
            //If null the user doesn't want anything special to happen.
            if (successTask != null) {

                //Perform the EngageSDK internal logic before passing processing off to user defined AsyncTask.
                EngageResponseXML responseXML = new EngageResponseXML(response);
                successTask.execute(responseXML);
            }
        }
    };
    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.getMessage(), error);

            //Call the SDK user defined method.
            if (failureTask != null) {
                failureTask.execute(error);
            }
        }
    };
    xmlapiClient.postResource(api, successListener, errorListener);
}
```

##### Identifying a registered user

```java
final XMLAPI selectRecipientData = XMLAPI.builder()
        .operation(XMLAPIOperation.SELECT_RECIPIENT_DATA)
        .listId("45654")
        .email("someone@adomain.com")
        .build();

// You can also provide null for the response handler if you don't need custom success/failure logic
XMLAPIManager.get().postXMLAPI(selectRecipientData,
        new SelectRecipientResponseHandler() {
            @Override
            public void onSelectRecipientSuccess(SelectRecipientResponse selectRecipientResponse) {
                //custom code here
                Toast.makeText(getActivity().getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(getActivity().getApplicationContext(), "Error selecting recipient: "
                        + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
```

### <a name="MobileIdentityManager"/>MobileIdentityManager

The ```MobileIdentityManager``` can be used to manage user identities.  It can auto create new user identities 
as well as merge existing identities if needed.  This functionality is intended to replace the 
manual process of creating an anonymous user.
 
In addition to the normal app security token configuration, the following setup must be configured prior to 
using the ```MobileIdentityManager``` methods.
- Recipient list should already be completed and the ```listId``` should be setup in the configuration.
- EngageConfig.json should be configured with the columns names representing the _Mobile User Id_, _Merged Recipient Id_, and _Merged Date_.  The EngageConfigDefault.json defines default values if you prefer to use those.
- The _Mobile User Id_, _Merged Recipient Id_, and _Merged Date_ columns must be created in the recipient list with names that match your EngageConfig.json settings
- Optional: Configure audit table (TODO - fill in these details when known)

##### Setup recipient identity
```java
/**
 * Checks if the mobile user id has been configured yet.  If not
 * and the {@code enableAutoAnonymousTracking} flag is set to true it is auto generated
 * using either the {@link com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator} or
 * the generator configured as the {@code mobileUserIdGeneratorClassName}.  If
 * {@code enableAutoAnonymousTracking} is {@code false} you are responsible for
 * manually setting the id using {@link com.silverpop.engage.config.EngageConfig#storeMobileUserId(android.content.Context, String)}.
 * <p/>
 * Once we have a mobile user id (generated or manually set) a new recipient is
 * created with the mobile user id.
 * <p/>
 * On successful completion of this method the EngageConfig will contain the
 * moible user id and new recipient id.
 *
 * @param setupRecipientHandler custom behavior to run on success and failure of this method
 */
public void setupRecipient(SetupRecipientHandler setupRecipientHandler)
```

##### Check identity and merge recipients
```java
/**
 * Checks for an existing recipient with all the specified ids.  If a matching recipient doesn't exist
 * the currently configured recipient is updated with the searched ids.  If an existing recipient
 * does exist the two recipients are merged and the engage app config is switched to the existing
 * recipient.
 * <p/>
 * When recipients are merged a history of the merged recipients is recorded using the
 * Mobile User Id, Merged Recipient Id, and Merged Date columns.
 *
 * WARNING: The merge process is not currently transactional.  If this method errors the data is likely to
 * be left in an inconsistent state.
 *
 * @param idFieldNamesToValues Map of column name to id value for that column.  Searches for an
 *                             existing recipient that contains ALL of the column values in this map.
 *                             <p/>
 *                             Examples:
 *                             - Key: facebook_id, Value: 100
 *                             - Key: twitter_id, Value: 9999
 * @param identityHandler      custom behavior to run on success and failure of this method
 */
public void checkIdentity(final Map<String, String> idFieldNamesToValues, final CheckIdentityHandler identityHandler)
```

### <a name="AnonymousMobileIdentityManager"/>AnonymousMobileIdentityManager

Before the ```MobileIdentityManager``` was available SDK users could create an anonymous user manually.
That functionality still exists but the logic has been moved to a central class which is now the ```AnonymousMobileIdentityManager```.

##### Creating an anonymous user

```java
// You can also provide null for parameters 2 & 3 if you don't need custom success/failure logic
AnonymousMobileIdentityManager.get().createAnonymousUserList("EngageDBListID",
        new AsyncTask<EngageResponseXML, Void, Object>() {
            @Override
            protected EngageResponseXML doInBackground(EngageResponseXML... engageResponseXMLs) {
                Log.d(TAG, "Successful response");
                return engageResponseXMLs[0];
            }
        }, new AsyncTask<VolleyError, Void, Object>() {
            @Override
            protected Object doInBackground(VolleyError... volleyErrors) {
                Log.e(TAG, "Failure is posting create anonymous user event to Silverpop");
                return volleyErrors[0];
            }
        });
```

Note: Using this method from the ```XMLAPIManager``` is now depreciated.  You should start using it from
the ```AnonymousMobileIdentityManager``` instead.


### Local Event Storage

UBF events are persisted to a local SQLite DB on the user's device. The event can have only 1 of 5 status. 
NOT_READY_TO_POST, READY_TO_POST, SUCCESSFULLY_POSTED, FAILED_POST, and EXPIRED. 

|Event Name|Description|
|------------------|-------------|
|NOT_READY_TO_POST|UBF events that are still awaiting augmentation to complete. UBF events will stay in this state until the augmentation successfully completes or the augmentation times out.|
|READY_TO_POST|UBF events that are ready to be sent to Engage on the next POST.|
|SUCCESSFULLY_POSTED|UBF events that have already been successfully posted to Engage. These events will be purged after the configurable amount of time has been reached.|
|FAILED_POST|UBF events that were attempted to be posted to Engage for the maximum number of retries. Once in this state no further attempts to post the UBF event will be made.|
|EXPIRED|UBF events in this state "timed out" during their augmentation. These events are considered "READY_TO_POST" and treated just like a "READY_TO_POST" UBF event when sent to Engage but are labeled differently just to differentiate them from the UBF events with successful augmentation.|


### Deeplinking

Deeplinking is achieved in EngageSDK through a custom wrapper around the [mobiledeeplinking](#http://mobiledeeplinking.org) library. 
Deeplinking is enabled in EngageSDK by adding the following snippet to your applications AndroidManifest.xml file.

```xml
<activity
    android:name="com.silverpop.engage.deeplinking.EngageDeepLinkManager"
    android:theme="@android:style/Theme.NoDisplay"
    android:noHistory="true">
        <intent-filter>
            <data android:scheme=${YOUR_APPLICATION_DEEP_LINK_SCHEMA_GOES_HERE}/>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
        </intent-filter>
</activity>
```

EngageSDK will examine all get parameters in the deep link by default. If you wish to enable REST style route
parameter examination you should follow the guide for mobiledeeplinking [here](#http://mobiledeeplinking.org).
EngageSDK also examines the deeplink for the [CurrentCampaign](#CurrentCampaign), [ValidFor](#ValidFor), and [ExpiresAt](#ExpiresAt) values as well.
If those values are encountered in the deeplink parameters they are consumed and processed by the SDK.

### <a name="Configuration"/>Configuration

The EngageSDK contains default configuration values in a EngageConfigDefaults.json file in the SDK .aar file. 
SDK users can change any of the values defined there be copying and pasting the file into their SDK project
and *changing the name to EngageConfig.json*. The user configurations will be loaded and merged with the
default configurations with the user defined values taking precedence.

#### EngageConfigManager

The EngageConfigManager is the in memory representation of the EngageConfigDefaults.json and optionally EngageConfig.json
files in the application. EngageConfigManager has several static helper methods that can be queried 
from your application to retrieve desired configuration values.

#### <a name="ConfigurationValues"/> Configuration Values

|Configuration Name|Default Value|Meaning|Format|
|------------------|-------------|-------|------|
|LocalEventStore->expireLocalEventsAfterNumDays|30 days|Number of days before engage events are purged from local storage|Number|
|General>databaseListId|{YOUR_LIST_ID}|Engage Database ListID from Engage Portal|String|
|General>ubfEventCacheSize|3|Events to cache locally before batch post|Number|
|General>defaultCurrentCampaignExpiration|1 day|time before current campaign expires by default|EngageExpirationParser String|
|General>deepLinkScheme|{YOUR_DEEP_LINK_SCHEME}|Application deeplink scheme|String|
|ParamFieldNames>ParamCampaignValidFor|CampaignValidFor|External event parameter name to parse Campaign valid from|String|
|ParamFieldNames>ParamCampaignExpiresAt|CampaignExpiresAt|External event parameter name to parse Campaign expires at from|String|
|ParamFieldNames>ParamCurrentCampaign|CurrentCampaign|External event parameter name to parse Current Campaign from|String|
|ParamFieldNames>ParamCallToAction|CallToAction|External event parameter name to parse Call To Action from|String|
|Session>sessionLifecycleExpiration|5 minutes|time local application session is valid for before triggering session ended event|EngageExpirationParser String|
|Networking>maxNumRetries|3|Number of times that an event is retried before it is finally marked as failed in the local event store and no more attempts are made|Number|
|Networking>secureConnection|true|true if underlying connection should be https(and it should be) false otherwise. False only available for debugging purposes.|Boolean|
|UBFFieldNames>UBFSessionDurationFieldName|Session Duration|JSON Universal Event Session Duration field name|String|
|UBFFieldNames>UBFTagsFieldName|Tags|JSON Universal Event Tags field name|String|
|UBFFieldNames>UBFDisplayedMessageFieldName|Displayed Message|JSON Universal Event Displayed Message field name|String|
|UBFFieldNames>UBFCallToActionFieldName|Call To Action|JSON Universal Event Call To Action field name|String|
|UBFFieldNames>UBFEventNameFieldName|Event Name|JSON Universal Event name field name|String|
|UBFFieldNames>UBFGoalNameFieldName|Goal Name|JSON Universal Event goal field name|String|
|UBFFieldNames>UBFCurrentCampaignFieldName|Campaign Name|JSON Universal Event current campaign field name|String|
|UBFFieldNames>UBFLastCampaignFieldName|Last Campaign|JSON Universal Event last campaign field name|String|
|UBFFieldNames>UBFLocationAddressFieldName|Location Address|JSON Universal Event location address field name|String|
|UBFFieldNames>UBFLocationNameFieldName|Location Name|JSON Universal Event location name field name|String|
|UBFFieldNames>UBFLatitudeFieldName|Latitude|JSON Universal Event Latitude field name|String|
|UBFFieldNames>UBFLongitudeFieldName|Longitude|JSON Universal Event Longitude field name|String|
|LocationServices>lastKnownLocationDateFormat|yyyy'-'MM'-'dd|User last known location date format|String|
|LocationServices>lastKnownLocationTimestampColumn|Last Location Address Time|Engage DB column name for the last known location time|String|
|LocationServices>lastKnownLocationColumn|Last Location Address|Engage DB column name for the last known location|String|
|LocationServices>locationDistanceFilter|10|meters in location change before updated location information delegate is invoked|Number|
|LocationServices>locationPrecisionLevel|kCLLocationAccuracyBest|desired level of location accuracy|String|
|LocationServices>locationCacheLifespan|1 hr|lifespan of location coordinates before they are considered expired|EngageExpirationParser String|
|LocationServices>coordinatesPlacemarkTimeout|15 sec|timeout on acquiring CLPlacemark before event is posted without that information|EngageExpirationParser String|
|LocationServices>coordinatesAcquisitionTimeout|15 sec|timeout on acquiring CLLocation before event is posted without that information|EngageExpirationParser String|
|LocationServices>enabled|YES|Are Location services enabled for UBF events|Boolean|
|PluggableServices>pluggableLocationManagerClassName|com.silverpop.engage.location.manager.plugin.EngageLocationManagerDefault|Java implementation that will pull the location information from the device|Java Class|
|Augmentation>augmentationTimeout|15 sec|timeout for augmenting UBF events|EngageExpirationParser String|
|Augmentation>ubfAugmentorClassNames||JSON Array of Java class names that should be used for augmenting|JSON Array of String Java Classnames|
|Recipient>enableAutoAnonymousTracking|true|If set to true it allows mobile user ids to be auto generated for recipients.  If set to false you are responsible for manually setting the mobile user id.|Boolean|
|Recipient>mobileUserIdGeneratorClassName|com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator|The class to use for auto generating mobile user ids if the ```enableAutoAnonymousTracking``` property is set to true.|Java Class|
|Recipient>mobileUserIdColumn|Mobile User Id|Column name to store the mobile user id in.|String|
|Recipient>mergedRecipientIdColumn|Merged Recipient Id|Column name to store the merged recipient id in.  The merged recipient id column is populated if needed during the check identity process.|String|
|Recipient>mergedDateColumn|Merged Date|Column name to store the merged date in. The merged recipient id column is populated if needed during the check identity process.|String|
|Recipient>mergeHistoryInMergedMarketingDatabase|true|If the audit history for merged recipients should be stored in the marketing database.|Boolean|
|AuditRecord>auditRecordPrimaryKeyColumnName|Audit Record Id|Only required if ```mergeHistoryInAuditRecordTable``` is set to ```true```.  The column name for the generated primary key in the audit record table.|String|
|AuditRecord>auditRecordPrimaryKeyGeneratorClassName|com.silverpop.engage.util.uuid.plugin.DefaultUUIDGenerator|Only required if ```mergeHistoryInAuditRecordTable``` is set to ```true```.  The class to use to generate primary keys for the audit record table.|Java Class|
|AuditRecord>oldRecipientIdColumnName|Old Recipient Id|Only required if ```mergeHistoryInAuditRecordTable``` is set to ```true```. When a recipient is merged during the check identity process, this is the column name for old recipient id.|String|
|AuditRecord>newRecipientIdColumnName|New Recipient Id|Only required if ```mergeHistoryInAuditRecordTable``` is set to ```true```. When a recipient is merged during the check identity process, this is the column name for assumed recipient id.|String|
|AuditRecord>createDateColumnName|Create Date|Only required if ```mergeHistoryInAuditRecordTable``` is set to ```true```. When a recipient is merged during the check identity process, this is the column name for the timestamp for when the merge occurred.|String|
|AuditRecord>mergeHistoryInAuditRecordTable|false|If the audit history for merged recipients should be stored in a separate audit record table.|Boolean|

### <a name="EngageExpirationParser"/>EngageExpirationParser

EnagageSDK interacts with a wide array of dates and expiration times. Those values are pulled from both external parameters 
and internal configurations. To ensure that those values are most accurately interpreted a flexible 
format was created for the EngageSDK and a special format which will be referred to as the 
"EngageExpirationParser String". This "EngageExpirationParser String" value can accept any number 
of time based values and then provides several convenience methods for accessing specific units of 
time measurement from those parsed values. Units of time are measured from either a reference date 
that you provide when you create the object or otherwise the the time string is interpreted as a 
"valid for" value instead of a "expires at" value.

#### EngageExpirationExamples

Assume current date of 6/10/2014 00:00:00

|EngageExpirationParser String|Expiration Date|
|-----------------------------|---------------|
|1 day 15m|6/11/2014 00:15:00|
|15m1d0seconds|6/11/2014 00:15:00|
|65minutes|6/10/2014 01:05:00|
|3seconds|6/10/2014 00:00:03|


### Notifications
Due to numerous Android notification handling approaches it is the responsibility of the developer to capture
the desired notifications and then proxy them to the following SDK methods.

#### Local Notification Received

```java
XMLAPIManager.get().handleNotificationReceivedEvents(Context context, Notification notification, Map<String, Object> params);
```

#### Remote Notification Received

```java
Intent remoteNotificationIntent = ....;
XMLAPIManager.get().handleRemoteNotification(remoteNotificationIntent);
```

### External Parameter Monitoring

Deeplink and notifications have the ability to contain information in their payload that can change the
behavior of the SDK in relation to the UBF events that it generates. Therefore deeplinks that are opened, 
local notifications, and remote notifications are examined for the following parameters and if present
the SDK will internal change state to support them.

#### <a name="CurrentCampaign"/>Current Campaigns

The Current Campaign of each UBF event may be manually changed by an external parameter received by the application.
That external notification is examined for a parameter matching the value defined in the Engage configuration
and if present parses that value and stores it in the device local storage so that it is present in subsequent
 posts to the server. Campaigns have a default expiration time of 86400 seconds (1 day) after they are set via opened url of push notification. 

#### CurrentCampaign and CampaignEndTimeStamp Deeplink Examples
Below are some deep link examples assuming that your application is configured to open for a URL containing a host value of "Silverpop".

```
//Campaign Name set to "TestCurrentCampaign" and Expires 4 hours from when the link is opened
Silverpop://campaign/TestCurrentCampaign?ParamCampaignValidFor=4hours

//Campaign Name set to "TestCurrentCampaign" and Expires 1 Day (default since none present) after the URL is opened in the application
Silverpop://campaign/TestCurrentCampaign   

//Campaign Name set to "TestCurrentCampaign" and Expires on August 8th 2014 at 7:23AM. Note URL MUST be escaped but wasn't here for demonstration purposes!
Silverpop://campaign/TestCurrentCampaign?ParamCampaignExpiresAt=2014/08/01 07:23:00
```

#### Posting events to Universal Behaviors service

Events are cached and sent in larger batches for efficiency. The timing of the automated dispatches 
varies but usually occur when the app is sent to the background. If you would like to control when 
events are posted, you can tell the UBFClient to post any cached events.

##### Manually post all events in cache
```java
XMLAPIManager.get().postEventCache();
```

### Further Questions, Issues, or Comments?
We have setup a [forum on our Silverpop Community](http://community.silverpop.com/t5/Silverpop-Mobile-Connector/bd-p/Mobile) for fostering collaboration in both sharing success stories and tackling problems together.  We invite you to share your thoughts, questions, and stories there.