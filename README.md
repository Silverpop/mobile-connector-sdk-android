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
upon application statup and has already been properly authenticated with the Engage API. Creating 
and submitting a manually created UBF event is shown below.

```java
Context context = getActivity();    //This could be differ depending on the location this snippet is invoked from.
Map<String, Object> params = new HashMap<String, Object>(); //Place any desired parameters in this map
UBF namedGoalStarted = UBF.namedEvent(context, "sampleNamedEvent", params);
UBFManager.get().postEvent(namedGoalStarted);
```

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
* Primary User Id
* Anonymous Id

##### UBF Events and their Type Codes

* INSTALLED - 12
* SESSION_STARTED - 13;
* SESSION_ENDED - 14
* GOAL_ABANDONED - 15
* GOAL_COMPLETED - 16
* NAMED_EVENT - 17
* RECEIVED_NOTIFICATION - 48
* OPENED_NOTIFICATION 49

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

Brief introduction about XMLAPI.

#### <a name="XMLAPIObject"/>XMLAPI Object/Helper

#### <a name="XMLAPIManager"/>XMLAPIManager

The XMLAPIManager manages posting XMLAPI messages to the Engage web services. A XMLAPIManager global instance
is automatically created for you by ```java com.silverpop.engage.EngageApplication ```. XMLAPIManager
manages network availability and will cache requests when the network is not reachable executing them
once the network is accessible again. All XMLAPI requests made to Engage should be made through
this XMLAPIManager.

```java
    /**
     * Post an XMLAPI request to Engage
     *
     * @param api
     *      XMLAPI operation desired.
     *
     * @param successTask
     *      AsyncTask to execute on successful result.
     *
     * @param failureTask
     *      AsyncTask to execute on failure
     */
    public void postXMLAPI(XMLAPI api,
                           AsyncTask<EngageResponseXML, Void, Object> successTask,
                           AsyncTask<VolleyError, Void, Object> failureTask) {
        Response.Listener<String> successListener = successListenerForXMLAPI(api, successTask);
        Response.ErrorListener errorListener = null;
        xmlapiClient.postResource(api, successListener, errorListener);
    }
```

##### Creating an anonymous user

```objective-c
// Conveniently calls addRecipient and stores anonymousId within EngageConfig
[[XMLAPIManager sharedInstance] createAnonymousUserToList:ENGAGE_LIST_ID success:^(ResultDictionary *ERXML) {
    if ([[ERXML valueForShortPath:@"SUCCESS"] boolValue]) {
        NSLog(@"SUCCESS");
    }
    else {
        NSLog(@"%@",[ERXML valueForShortPath:@"Fault.FaultString"]);
    }
} failure:^(NSError *error) {
    NSLog(@"SERVICE FAIL");
}];
```

##### Identifying a registered user

```objective-c

XMLAPI *selectRecipientData = [XMLAPI selectRecipientData:@"somebody@somedomain.com" list:ENGAGE_LIST_ID];

[[XMLAPIManager sharedInstance] postXMLAPI:selectRecipientData success:^(ResultDictionary *ERXML) {
        if ([[ERXML valueForShortPath:@"SUCCESS"] boolValue]) {
            NSLog(@"SUCCESS");
            // VERY IMPORTANT!!!
            // Universal Behaviors reads this value
            [EngageConfig storePrimaryUserId:[ERXML valueForShortPath:@"RecipientId"]];
        }
        else {
            NSLog(@"%@",[ERXML valueForShortPath:@"Fault.FaultString"]);
        }
    } failure:^(NSError *error) {
        NSLog(@"SERVICE FAIL");
    }];
```

##### Convert anonymous user to registered user

```objective-c
// Conveniently links anonymous user record with the primary user record according to the mergeColumn
[[XMLAPIManager sharedInstance] updateAnonymousToPrimaryUser:[EngageConfig primaryUserId]
                                                   list:ENGAGE_LIST_ID
                                      primaryUserColumn:@"CONTACT_ID"
                                            mergeColumn:@"MERGE_CONTACT_ID"
                                                success:^(ResultDictionary *ERXML) {
                                                    if ([[ERXML valueForShortPath:@"SUCCESS"] boolValue]) {
                                                        NSLog(@"SUCCESS");
                                                    }
                                                    else {
                                                        NSLog(@"%@",[ERXML valueForShortPath:@"Fault.FaultString"]);
                                                    }
                                                } failure:^(NSError *error) {
                                                    NSLog(@"SERVICE FAIL");
                                                }];
```














#### Goal Completed
```objective-c
[[UBFManager sharedInstance] trackEvent:[UBF goalCompleted:@"LISTENED TO MVSTERMIND" params:nil]];
```

#### Goal Abandoned
```objective-c
[[UBFManager sharedInstance] trackEvent:[UBF goalAbandoned:@"LISTENED TO MVSTERMIND" params:nil]];
```

#### Named Event with params
```objective-c
[[UBFManager sharedInstance] trackEvent:[UBF namedEvent:@"PLAYER LOADED" params:@{ @"Event Source View" : @"HomeViewController", @"Event Tags" : @"MVSTERMIND,Underground" }]];
```

## Local Event Storage

UBF events are persisted to a local SQLite DB on the user's device. The event can have 1 of 4 status. NOT_POSTED, SUCCESSFULLY_POSTED, FAILED_POST, HOLD. 

*NOT_POSTED 
**UBF events that are ready to be sent to Engage but currently cannot due to network not being reachable or queue cache size not being met yet.
*SUCCESSFULLY_POSTED
**UBF events that have already been successfully posted to Engage. These events will be purged after the configurable amount of time has been reached.
*FAILED_POST
**UBF events that were attempted to be posted to Engage for the maximum number of retries. Once in this state no further attempts to post the UBF event will be made.
*HOLD
**UBF events in this state have been initially created but have still not had all of their data set by the augmentation service. UBF events that fail to be ran successfully through the augmentation service before their timeouts have been reached will be moved to the NOT_POSTED state and sent to Engage on the next flush. Providing timeouts helps ensure that the events do not become stuck in the HOLD state if certain external augmentation events are never received.

## UBF Event Augmentation Plugin Service



## EngageSDK Models

EngageSDK has 2 primary models that SDK users should concerns themselves with

### UBF

Utility class for generating JSON Universal Events that are posted to the UBFManager and ultimately sent to Engage. The class maintains a NSDictionary of attributes that are different depending on the event type that is created. Any NSDictionary values that you provide to the utility methods will take precedence over the values that the utility methods pull from the device.



### XMLAPI

Post an XMLAPI resource using a helper e.g. SelectRecipientData

```objective-c
// create a resource encapsulating your request to select by email address
XMLAPI *selectRecipientData = [XMLAPI selectRecipientData:@"somebody@somedomain.com" list:ENGAGE_LIST_ID];

[[XMLAPIManager sharedInstance] postXMLAPI:selectRecipientData success:^(ResultDictionary *ERXML) {
    // SUCCESS = TRUE
    if ([[ERXML valueForShortPath:@"SUCCESS"] boolValue]) {
        NSLog(@"SUCCESS");
    }
    // SUCCESS != TRUE
    // This is a specific XMLAPI failure, status 2xx
    else {
        NSLog(@"%@",[ERXML valueForShortPath:@"Fault.FaultString"]);
    }
} failure:^(NSError *error) {
    // This is a status > 400
    NSLog(@"SERVICE FAIL");
}];
```

## XMLAPI Resources

### Example 1

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

```objective-c
XMLAPI *selectRecipientData = [XMLAPI resourceNamed:@"SelectRecipientData"
                                             params:@{
                               @"LIST_ID" : @"45654",
                               @"EMAIL" : @"someone@adomain.com",
                               @"COLUMNS" : @{ @"Customer Id" : @"123-45-6789" } }];
```

or alternately:

```objective-c
XMLAPI *selectRecipientData = [XMLAPI resourceNamed:@"SelectRecipientData"];
[selectRecipientData addParams:@{ @"LIST_ID" : @"45654", @"EMAIL" : @"someone@adomain.com" }];
[selectRecipientData addColumns:@{ @"Customer Id" : @"123-45-6789" }];
```

### Example 2

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

```objective-c
XMLAPI *selectRecipientData = [XMLAPI resourceNamed:@"SelectRecipientData" params:@{@"RECIPIENT_ID" : @"702003"}];
```

### Example 3

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

```objective-c
XMLAPI *selectRecipientData = [XMLAPI selectRecipientData:@"someone@adomain.com" list:@"45654"];
```

## Deeplinking


## Configuration

The EngageSDK is configured via 2 plist files. One plist file (EngageConfigDefaults.plist) is provided in the SDK itself and in populated with the values from the Configuration Values table below to promote a turn key SDK approach. The second plist file is a file caused EngageConfig.plist that you (optionally) provide in the supporting files of your project. The EngageConfig.plist values you define always take precedence over the configuration values defined in the EngageConfigDefaults.plist files. It is recommended that you simply copy the EngageConfigDefaults.plist file and rename it to EngageConfig.plist in your project and change the configurations to their desired values.

### EngageConfigManager

The EngageSDK configuration values are stored in memory in a NSDictionary after the application starts up. Receiving those individual configuration values is managed via the EngageConfigManager. The manager queries the NSDictionary for the requested field. EngageConfigManager accepts constants defined in EngageConfig which provide more description names that point to the actual configuration values specified in the Configuration Values table below.

### <a name="Configuration"/> Configuration Values

The configuration 

|Configuration Name|Default Value|Meaning|Format|
|------------------|-------------|-------|------|
|LocalEventStore->expireLocalEventsAfterNumDays|30 days|Number of days before engage events are purged from local storage|Number|
|General->databaseListId|{YOUR_LIST_ID}|Engage Database ListID from Engage Portal|String|
|General->ubfEventCacheSize|3|Events to cache locally before batch post|Number|
|General->defaultCurrentCampaignExpiration|1 day|time before current campaign expires by default|EngageExpirationParser String|
|ParamFieldNames->ParamCampaignValidFor|CampaignValidFor|External event parameter name to parse Campaign valid from|String|
|ParamFieldNames->ParamCampaignExpiresAt|CampaignExpiresAt|External event parameter name to parse Campaign expires at from|String|
|ParamFieldNames->ParamCurrentCampaign|CurrentCampaign|External event parameter name to parse Current Campaign from|String|
|ParamFieldNames->ParamCallToAction|CallToAction|External event parameter name to parse Call To Action from|String|
|Session->sessionLifecycleExpiration|30 minutes|time local application session is valid for before triggering session ended event|EngageExpirationParser String|
|Networking->maxNumRetries|3|Number of times that an event is retried before it is finally marked as failed in the local event store and no more attempts are made|Number|
|UBFFieldNames->UBFSessionDurationFieldName|Session Duration|JSON Universal Event Session Duration field name|String|
|UBFFieldNames->UBFTagsFieldName|Tags|JSON Universal Event Tags field name|String|
|UBFFieldNames->UBFDisplayedMessageFieldName|Displayed Message|JSON Universal Event Displayed Message field name|String|
|UBFFieldNames->UBFCallToActionFieldName|Call To Action|JSON Universal Event Call To Action field name|String|
|UBFFieldNames->UBFEventNameFieldName|Event Name|JSON Universal Event name field name|String|
|UBFFieldNames->UBFGoalNameFieldName|Goal Name|JSON Universal Event goal field name|String|
|UBFFieldNames->UBFCurrentCampaignFieldName|Campaign Name|JSON Universal Event current campaign field name|String|
|UBFFieldNames->UBFLastCampaignFieldName|Last Campaign|JSON Universal Event last campaign field name|String|
|UBFFieldNames->UBFLocationAddressFieldName|Location Address|JSON Universal Event location address field name|String|
|UBFFieldNames->UBFLocationNameFieldName|Location Name|JSON Universal Event location name field name|String|
|UBFFieldNames->UBFLatitudeFieldName|Latitude|JSON Universal Event Latitude field name|String|
|UBFFieldNames->UBFLongitudeFieldName|Longitude|JSON Universal Event Longitude field name|String|
|LocationServices->lastKnownLocationDateFormat|yyyy'-'MM'-'dd|User last known location date format|String|
|LocationServices->lastKnownLocationTimestampColumn|Last Location Address Time|Engage DB column name for the last known location time|String|
|LocationServices->lastKnownLocationColumn|Last Location Address|Engage DB column name for the last known location|String|
|LocationServices->locationDistanceFilter|10|meters in location change before updated location information delegate is invoked|Number|
||LocationServices->locationPrecisionLevel|kCLLocationAccuracyBest|desired level of location accuracy|String|
|LocationServices->locationCacheLifespan|1 hr|lifespan of location coordinates before they are considered expired|EngageExpirationParser String|
|LocationServices->coordinatesPlacemarkTimeout|15 sec|timeout on acquiring CLPlacemark before event is posted without that information|EngageExpirationParser String|
|LocationServices->coordinatesAcquisitionTimeout|15 sec|timeout on acquiring CLLocation before event is posted without that information|EngageExpirationParser String|
|LocationServices->enabled|YES|Are Location services enabled for UBF events|Boolean|
|Augmentation->augmentationTimeout|15 sec|timeout for augmenting UBF events|EngageExpirationParser String|


## EngageExpirationParser

EnagageSDK interacts with a wide array of dates and expiration times. Those values are pulled from both external parameters and internal configurations. To ensure that those values are most accurately interpretted a flexible format was created for the EngageSDK and a special format which will be referred to as the "EngageExpirationParser String". This "EngageExpirationParser String" value can accept any number of time based values and then provides several convenience methods for accessing specific units of time measurement from those parsed values. Units of time are measured from either a reference date that you provide when you create the object or otherwise the the time string is interpreted as a "valid for" value instead of a "expires at" value.

### EngageExpirationExamples

####Assume current date of 6/10/2014 00:00:00

|EngageExpirationParser String|Expiration Date|
|-----------------------------|---------------|
|1 day 15m|6/11/2014 00:15:00|
|15m1d0seconds|6/11/2014 00:15:00|
|65minutes|6/10/2014 01:05:00|
|3seconds|6/10/2014 00:00:03|


## Demo

EngageSDK includes a sample project within the Example subdirectory. In order to build the project, you must install the dependencies via CocoaPods. To do so:

    $ gem install cocoapods # If necessary
    $ git clone git@github.com:Silverpop/engage-sdk-ios.git
    $ cd engage-sdk-ios/Example
    $ pod install
    $ touch EngageSDKDemo/sample-config.h
    $ open EngageSDKDemo.xcworkspace

Open the EngageSDKDemo/sample-config.h file and paste the `#define` code from [Environment Setup](#environment-setup) below.

Once installation has finished, you can build and run the EngageSDKDemo project within your simulator or iPhone device.

Once you understand how the Demo project is configured via CocoaPods and implemented using the EngageSDK, you are ready to integrate the EngageSDK with your new or existing Xcode iPhone project.

## Getting Started 
The first thing you will want to do is contact your Relationship Manager at Silverpop and ask for the "Silverpop Mobile Connector".  They will assist in getting your Engage account provisioned for Universal Behaviors -- the new flexible event tracking system that is the backbone of tracked mobile app behaviors.

Next, you can follow the instructions in this readme file, or as an additional offer, we've put together a short 10 minute tutorial that will walk you through the download, installation, and configuration process to get your app up and running.  [Click here](https://kb.silverpop.com/kb/engage/Silverpop_Mobile_Connector_-_***NEW***/Video_Tutorial%3A_Up_and_Running_in_10_mins!) to watch that video tutorial within our KnowledgeBase.


CocoaPods clones the EngageSDK files from github and creates an Xcode workspace configured with all dependencies (AFNetworking, AFOAuth2) and linking your existing project to a 'Pods' project that organizes and manages your dependencies and builds them as static libraries linked into your project.

Open the Xcode workspace and import the public headers of the EngageSDK library by adding the following line to your code:

### Sessions

EngageSDK implements predefined Session events for Universal Behaviors. Sessions are configured to timeout if a user leaves your app for at least 5 minutes. At the end of the Session, duration is computed excluding any portion of inactivity.

#### Notifications
Both local and push notifications require that the user of the SDK enable their application for subscribing and listening for the notifications. These hooks for the notifications are defined inside your application's UIApplicationDelegage (AppDelegate) implementation class. Full reference for those hooks can be found [here] (https://developer.apple.com/library/ios/documentation/uikit/reference/uiapplicationdelegate_protocol/Reference/Reference.html#//apple_ref/occ/intfm/UIApplicationDelegate). Examples of using the local and push notification hooks are found below.

#### Local Notification Received
```objective-c
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    [[UBFManager sharedInstance] handleLocalNotificationReceivedEvents:notification withParams:nil];
}
```

#### Push Notification Received
```objective-c
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)pushNotification 
{
    [[UBFManager sharedInstance] handlePushNotificationReceivedEvents:pushNotification];
}
```

#### Application Opened by clicking Notification - Application AppDelegate class
```objective-c
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    if (launchOptions != nil) {
        // Launched from push notification or local notification
        NSDictionary *notification = nil;
        if ([launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]) {
            notification = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
        } else if ([launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey]) {
            notification = [launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
        } else {
            //Other application logic
        }
        
        [[UBFManager sharedInstance] handleNotificationOpenedEvents:notification];
    }
}
```

#### Application Opened by clicking external DeepLink - Application AppDelegate class snippet
```objective-c
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
    NSURL *ubfEventUid = [[UBFManager sharedInstance] handleExternalURLOpenedEvents:url];
}
```

#### DeepLink Configuration
Deep linking is handled in the EngageSDK by leveraging the [MobileDeepLinking](http://mobiledeeplinking.org) library. **You must create a MobileDeepLinkingConfig.json file** in your application. MobileDeepLinkingConfig.json is the definition of how EngageSDK will parse the parameters from the DeepLinks presented to your app and ultimately are sent as part of UBF events. Complete configuration can be found on the [MobileDeepLinking website](http://mobiledeeplinking.org). At a minimum you must define a "handler" to handle the parsing of the URLs. The EngageSDK handler is named "postSilverpop" and a sample configuration is found below. At a minimum you should include the "defaultRoute" section from the sample below to your MobileDeepLinkingConfig.json file. You can also register your own custom handlers with MobileDeepLinking and add them to the list of handlers in the configuration file.

```json
{
    "logging": "true",
    "defaultRoute": {
        "handlers": [
            "postSilverpop"
        ]
    },
    "routes": {
        "test/:testId": {
            "handlers": [
                         "postSilverpop"
                         ],
            "routeParameters": {
                "testId": {
                    "required": "true",
                    "regex": "[0-9]"
                },
                "CurrentCampaign": {
                    "required": "false"
                },
                "utmSource": {
                    "required": "false"
                }
            }
        },
        "campaign/:CurrentCampaign": {
            "handlers": [
                "postSilverpop"
            ],
            "routeParameters": {
                "CurrentCampaign": {
                    "required": "true"
                },
                "CampaignEndTimeStamp": {
                    "required": "false"
                }
            }
        }
    }
}
```

#### Current Campaigns
If you noticed the configuration value above has a parameter with a value of "CurrentCampaign". The #define macro of ```#define CURRENT_CAMPAIGN_PARAM_NAME @"CurrentCampaign"``` also has a default value of "CurrentCampaign". When a URL is opened and the UBFManager is invoked the CURRENT_CAMPAIGN_PARAM_NAME value is used to search the parameters for a match. If a match is found then the value of that parameter is set as the "Campaign Name" for all subsequent UBF events that are posted to Engage. Campaigns have a default expiration time of 86400 seconds (1 day) after they are set via opened url of push notification. If that value is not desirable you may also supply a ```objective-c #define CAMPAIGN_EXTERNAL_EXPIRATION_DATETIME_PARAM @"CampaignEndTimeStamp"``` value which is a standard linux timestamp for when you want the campaign specified to expire. [Here](http://www.timestampgenerator.com) is a handy timestamp tool for calculating those values. **Timestamps should be GMT**


#### CurrentCampaign and CampaignEndTimeStamp Deeplink Examples
Below are some deep link examples assuming that your application is configured to open for a URL containing a host value of "Silverpop".

```objective-c
Silverpop://campaign/TestCurrentCampaign?CampaignEndTimeStamp=1419465600    //Campaign Name set to "TestCurrentCampaign" and Expires on December 25th 2014 at 12AM
Silverpop://campaign/TestCurrentCampaign   //Campaign Name set to "TestCurrentCampaign" and Expires 1 Day after the URL is opened in the application
Silverpop://campaign/TestCurrentCampaign?CampaignEndTimeStamp=30931200    //Campaign Name set to "TestCurrentCampaign" and Expires on December 25th 1970 at 12AM. So campaign is never activated
```


### Posting events to Universal Behaviors service

Events are cached and sent in larger batches for efficiency. The timing of the automated dispatches varies but usually occur when the app is sent to the background. If you would like to control when events are posted, you can tell the UBFClient to post any cached events.

#### Manually post all events in cache
```objective-c
[[UBFManager sharedInstance] postEventCache];
```

### Further Questions, Issues, or Comments?
We have setup a [forum on our Silverpop Community](http://community.silverpop.com/t5/Silverpop-Mobile-Connector/bd-p/Mobile) for fostering collaboration in both sharing success stories and tackling problems together.  We invite you to share your thoughts, questions, and stories there.
