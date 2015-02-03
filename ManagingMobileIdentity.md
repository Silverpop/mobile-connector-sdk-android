# Android Managing Mobile Identity Script

## Create Project
1. Open Android Studio
2. New Project
3. Name application : ```EngageIdentityDemo```
4. Set company domain : ```silverpop.com```
5. Choose project location
6. Click next
7. Make sure the box for 'Phone and Tablet' is checked
8. Set Minimum SDK to 19 or lower > Next
9. Name activity to your liking - I'm keeping defaults
10. Finish


## IDE Setup
1. Turn on autoscroll to/from source (This step is optional, but it makes the demo nicer because you can see where the open files live in the project tree as we access them)
2. Uncheck 'Compact Empty Middle Packages' (Also optional, but it makes it easier to see the directory structure)
3. Switch the Project tool window from 'Android' to 'Project'


## Add Engage SDK Dependency
1. The EngageSDK isn't in Maven yet so for now we'll manually add the dependency
1. In the Project tool window, right click the ```app``` folder > New Directory > name ```aars``` > Ok
1. Copy/paste the ```engage-1.1.0.aar``` into new ```aars``` directory (you can either build this yourself or have Silverpop provide it for you)
1. Open ```build.grade``` from Gradle tool window and add the following
```groovy
repositories {
    flatDir {
        dirs 'aars'
    }
}

dependencies {
    // use this once maven support is added
    //compile(group: 'com.silverpop', name: 'engage', version: '1.1.0', ext: 'aar')

    compile 'com.silverpop:engage:1.1.0@aar'
}
```
1. Sync Gradle to pull in your new changes
2. 
## Create Custom Application
1. Right click the ```engagedemo``` package >  New > Java Class
1. Name the new class ```Application``` > Ok
1. Modify the ```Application``` class to extend ```EngageApplication```
```java
package com.silverpop.engagedemo;

import com.silverpop.engage.EngageApplication;

public class Application extends EngageApplication {
}
```
1. Open ```AndroidManifest.xml```
1. Add an attribute to the ```application``` xml with the full class name of your new ```Application.java``` class
```xml
android:name="com.silverpop.engagedemo.Application"
```


## Use Silverpop Engage Web Interface to Setup Credentials
1. Oragnization Settings > Application Account Access > Add Application
2. Name Application > Ok
3. You'll see your new Client Id and Client Secret
4. Add Account Access to setup your user.  Once setup you'll get an email with your Refresh Token.

## Add Credentials
1. Open ```AndroidManifest.xml```
1. In the ```application``` xml block add your credentials
```xml
<meta-data android:name="ENGAGE_CLIENT_ID" android:value="02eb567b-3674-4c48-8418-dbf17e0194fc" />
<meta-data android:name="ENGAGE_CLIENT_SECRET_META" android:value="9c650c5b-bcb8-4eb3-bf0a-cc8ad9f41580" />
<meta-data android:name="ENGAGE_REFRESH_TOKEN" android:value="676476e8-2d1f-45f9-9460-a2489640f41a" />
<meta-data android:name="ENGAGE_HOST" android:value="https://apipilot.silverpop.com/" />
```
1. Note that you should never save your credentials in your application code and we are only doing this for demo purposes to get you up and running.  Ideally you'd hide your credentials behind a webservice interface so app users can't decompile the code to reveal your tokens.


## Setup UI
1. Open ```fragment_main.xml```
2. In the Design tab set the ```id``` of the Hello World ```TextView``` to ```currentConfigView```
3. Set the ```minLines``` property to 5
4. Set the ```layoutWidth``` to ```match_parent```
3. Change to Text tab and verify you changed the correct settings
4. Change the 'Hello World' text to say 'Current Config:'


## Add Functionality
1. Open the ```Application``` class back up to add our new functionality.
2. Override the ```onCreate()`` method and add the following:
```java
@Override
public void onCreate() {
    super.onCreate();

    MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
        @Override
        public void onSuccess(SetupRecipientResult setupRecipientResult) {
            Log.i("application", "Identity Success");
        }

        @Override
        public void onFailure(SetupRecipientFailure setupRecipientFailure) {
            Log.i("application", "Identity Failure");
        }
    });
    
}
```
3. Open ```MainActivity.java``` and update it with the following
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment())
                .commit();
    }

    updateConfigLabel();
}

private void updateConfigLabel() {
    String recipientId = EngageConfig.recipientId(getApplicationContext());
    String mobileUserId = EngageConfig.mobileUserId(getApplicationContext());

    TextView configView = (TextView)findViewById(R.id.currentConfigView);
    configView.setText(String.format("Current Config:\nRecipient Id:\n%s\nMobile User Id:\n%s", recipientId, mobileUserId));
}
```
