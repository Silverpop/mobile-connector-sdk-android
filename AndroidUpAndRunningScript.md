#Android Up And Running in Under 10 Mins Script

_Note these instructions were written using Android Studio 1.0.2_

## Create Project
1. Open Android Studio (for the video open this ahead of time so you can close the last project you had open and start with a clean screen)
2. You'll need the Android 19 SDK installed, so if you haven't done that already you should do it now.  Tools > Android > SDK Manager.  *Note this will take awhile to download and install.
1. File > New Project
1. Name application : ```EngageSetup```
1. Set Company Domain: ```silverpop.com```
1. Choose project location
1. Click Next
1. Make sure the box for 'Phone and Tablet' is checked
1. Set Minimum SDK to 19 or lower > Next
1. Select 'Blank Activity with Fragment' (you can choose something different, but I'm picking this one as a very simple demo) > Next
1. Name activity to your liking - I’m keeping the defaults
1. Finish


## IDE Setup
1. Turn on autoscroll to/from source (This step is optional, but it makes the demo nicer because you can see where the open files live in the project tree as we access them)
2. Uncheck 'Compact Empty Middle Packages' (Also optional, but it makes it easier to see the directory structure)
1. Switch the Project tool window from 'Android' to 'Project'


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

## Customize Label to Show Connection Status
1. Open ```activity_main.xml```
1. Click on the 'Hello World' text in the preview window
1. Make sure you are on the 'Design' tab at the bottom
2. In the Properties window on the right set the ```id``` property to ```connectionView```
1. Switch to the Text view at the bottom
1. Click through to the ```strings.xml``` and replace the hello_world line with the following:
```xml
<string name="connection_status">Connection Status</string>
```
1. Go back to ```activity_main.xml```
1. Change the TextView to use the ```connection_status``` string:
```xml
android:text="@string/connection_status"
```
1. Now we're going to add code that will poll for the connection status and update the connection status text view with the current status
1. Open ```MainActivity.java```
1. Add a status handler field to the class
```java
//android.os.Handler
private Handler statusHandler;
```
1. In the ```onCreate``` method initialize the handler and start monitoring the status
```java
if (statusHandler == null) {
    statusHandler = new Handler();
}
startMonitoringStatus();
```
1. Add methods to control the ```Handler```
```java
Runnable statusChecker = new Runnable() {
    @Override
    public void run() {
        TextView connectionView = (TextView) findViewById(R.id.connectionView);

        // check if authenticated
        if (EngageConnectionManager.get().isAuthenticated()) {
            connectionView.setText("Connection Successful!");
        } else {
            connectionView.setText("Not Connected");
        }

        statusHandler.postDelayed(statusChecker, 2000); // every two seconds
    }
};

void startMonitoringStatus() {
    statusChecker.run();
}

void stopMonitoringStatus() {
    statusHandler.removeCallbacks(statusChecker);
}
```
1. Override the ```onStart()``` and ```onStop()``` methods.
```java
@Override
protected void onStart() {
    super.onStart();
    
    startMonitoringStatus();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    
    stopMonitoringStatus();
}
```
1. For reference the full class looks like this:
```java
package com.silverpop.engagedemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.silverpop.engage.network.EngageConnectionManager;


public class MainActivity extends Activity {

    private Handler statusHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (statusHandler == null) {
            statusHandler = new Handler();
        }
        startMonitoringStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();

        startMonitoringStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopMonitoringStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            TextView connectionView = (TextView) findViewById(R.id.connectionView);

            // check if authenticated
            if (EngageConnectionManager.get().isAuthenticated()) {
                connectionView.setText("Connection Successful!");
            } else {
                connectionView.setText("Not Connected");
            }

            statusHandler.postDelayed(statusChecker, 2000); // every two seconds
        }
    };

    void startMonitoringStatus() {
        statusChecker.run();
    }

    void stopMonitoringStatus() {
        statusHandler.removeCallbacks(statusChecker);
    }

}
```

## Run App
1. In the Run Configurations click the run button for ```app```
1. Pick the emulator or Android device of your choice.  If you haven't set one up yet you can click the '...' button next the 'Android virtual device' field in the 'Choose Device' dialog that opens up.  Or you can set these up under Tools > Android > AVD Manager before running the app.  Additional instructions can be found [here](http://developer.android.com/training/basics/firstapp/running-app.html).
1. The app will start up and the text will change to "Connection Successful!" once the application has successfully authenticated with the Silverpop APIs (NOTE: The Android emulator takes several minutes to start up so we may want to consider editing some of that wait time out of the video)
