package com.silverpop.engage.demo.engagetest;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.util.Log;

import com.silverpop.engage.demo.engagetest.fragment.EngageConfigFragment;
import com.silverpop.engage.demo.engagetest.fragment.NotificationsFragment;
import com.silverpop.engage.demo.engagetest.fragment.UBFAPIFragment;
import com.silverpop.engage.demo.engagetest.fragment.XMLAPIFragment;


public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getName();
    private FragmentTabHost mTabHost;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTabHost = (FragmentTabHost)findViewById(R.id.mainTabHost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.mainTabHost);

        mTabHost.addTab(mTabHost.newTabSpec("XML API").setIndicator("XML API"),
                XMLAPIFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("UBF Client").setIndicator("UBF Client"),
                UBFAPIFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("Notifications").setIndicator("Notifications"),
                NotificationsFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("EngageConfig").setIndicator("EngageConfig"),
                EngageConfigFragment.class, null);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String tabId = bundle.getString("tabId");
                Log.d(TAG, "TabID : " + tabId);

                if (tabId != null) {
                    if (tabId.equalsIgnoreCase("xml")) {
                        mTabHost.setCurrentTab(0);
                    } else if (tabId.equalsIgnoreCase("ubf")) {
                        mTabHost.setCurrentTab(1);
                    } else if (tabId.equalsIgnoreCase("notifications")) {
                        mTabHost.setCurrentTab(2);
                    } else if (tabId.equalsIgnoreCase("config")) {
                        mTabHost.setCurrentTab(3);
                    }
                }
            }
        }
    }
}