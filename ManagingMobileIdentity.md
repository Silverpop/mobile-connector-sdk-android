# Android Managing Mobile Identity Script

## Setup Before Video
The following steps are covered in the Android Up and Running Demo so we can go ahead and set them up before starting the new video.


### Create Project
1. Open Android Studio
2. New Project
3. Name application : ```EngageIdentityDemo```
4. Set company domain : ```silverpop.com```
5. Choose project location
6. Click next
7. Make sure the box for 'Phone and Tablet' is checked
8. Set Minimum SDK to 19 or lower > Next
9. Select 'Blank Activity'
9. Name activity to your liking - I'm keeping defaults
10. Finish


### IDE Setup
1. Turn on autoscroll to/from source (This step is optional, but it makes the demo nicer because you can see where the open files live in the project tree as we access them)
2. Uncheck 'Compact Empty Middle Packages' (Also optional, but it makes it easier to see the directory structure)
3. Switch the Project tool window from 'Android' to 'Project'


### Add Engage SDK Dependency
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
 

### Create Custom Application
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


### Add Credentials
1. Open ```AndroidManifest.xml```
1. In the ```application``` xml block add your credentials
```xml
<meta-data android:name="ENGAGE_CLIENT_ID" android:value="02eb567b-3674-4c48-8418-dbf17e0194fc" />
<meta-data android:name="ENGAGE_CLIENT_SECRET_META" android:value="9c650c5b-bcb8-4eb3-bf0a-cc8ad9f41580" />
<meta-data android:name="ENGAGE_REFRESH_TOKEN" android:value="676476e8-2d1f-45f9-9460-a2489640f41a" />
<meta-data android:name="ENGAGE_HOST" android:value="https://apipilot.silverpop.com/" />
```

## Video Starts Here

### Intro Script
Today we are going to demonstrate configuring mobile idenities using the Android Engage SDK.  Before watching this video we are making the assumption that you've already watched the Android Up And Running video so we're going to jump right in.

I've already configured my new Android Studio project with the needed configuration settings (show them).

But before we can add the new functionality there are a few things you need to Setup on the silverpop side.  The first is that you'll need to configure your recipient lists with columns for 
- Mobile User Id
- Merged Recipient Id
- Merged Date

You also have the option for creating a separate AuditRecord table if you'd prefer to track recipient merge history there.  You'll need to set that up with columns for 
- Audit Record Id
- Old Recipient Id
- New Recipient Id
- Create Date
if you wish to use that.  If you any help with this configuration please contact Silverpop support.

You'll also need to have your recipient table pre-configured with any custom id columns you wish - facebook_id, etc.

My recipient table is currently setup with a custom id column called "Custom Integration Test Id" so that's what I'm going to use for the demo.


### Check List of Already Setup
1. Added engage-1.1.0.aar
2. Gradle Config
3. Custom ```Application``` that extends ```EngageApplication```
4. ```AndroidManifest.xml``` is configured with custom ```Application```
5. ```AndroidManifest.xml``` is configured with credentials
6. ```EngageConfig.json``` is added to ```assets``` directory

### Setup UI
1. Open ```activity_main.xml```
2. In the Design tab set the ```id``` of the Hello World ```TextView``` to ```currentConfigView```
3. Set the ```minLines``` property to 5
4. Set the ```layoutWidth``` to ```match_parent```
3. Change to Text tab and verify you changed the correct settings
4. Change the 'Hello World' text to say 'Current Config:'
5. Add 'Setup Recipient' button with ```setupRecipientBtn``` as the id
6. Add 'Check Identity' button with ```checkIdentityBtn``` as the id


### Add Functionality
3. Open ```MainActivity.java``` and update it with the following
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button setupRecipientBtn = (Button)findViewById(R.id.setupRecipientBtn);
    setupRecipientBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MobileIdentityManager.get().setupRecipient(new SetupRecipientHandler() {
                @Override
                public void onSuccess(SetupRecipientResult setupRecipientResult) {
                    updateConfigStatus();
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).showToast();
                }

                @Override
                public void onFailure(SetupRecipientFailure setupRecipientFailure) {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).showToast();
                }
            });
        }
    });

    Button checkIdentityBtn = (Button)findViewById(R.id.checkIdentityBtn);
    checkIdentityBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String, String> ids = new HashMap<String, String>();
            ids.put("Custom Integration Test Id", "09890809809");
            MobileIdentityManager.get().checkIdentity(ids, new CheckIdentityHandler() {
                @Override
                public void onSuccess(CheckIdentityResult checkIdentityResult) {
                    Toast.makeText(getApplicationContext(), "Check identity success", Toast.LENGTH_SHORT).showToast();
                }

                @Override
                public void onFailure(CheckIdentityFailure checkIdentityFailure) {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).showToast();
                }
            });
        }
    });
}

private void updateConfigStatus() {
    TextView config = (TextView)findViewById(R.id.configStatusText);
    config.setText(String.format("Config\nRecipient Id:\n%s\nMobile User Id\n%s",
            EngageConfig.recipientId(getApplicationContext()),
            EngageConfig.mobileUserId(getApplicationContext())));
}
```

### Run App
1. Click the run button and wait for the emulator to start
2. Click Setup recipient and wait for the config to change
3. Click Check Identity
