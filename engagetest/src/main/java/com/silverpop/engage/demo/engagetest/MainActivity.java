package com.silverpop.engage.demo.engagetest;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import com.silverpop.engage.demo.engagetest.fragment.EngageConfigFragment;
import com.silverpop.engage.demo.engagetest.fragment.NetworkLogFragment;
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

        mTabHost.addTab(mTabHost.newTabSpec("EngageConfig").setIndicator("EngageConfig"),
                EngageConfigFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("Network Log").setIndicator("Network Log"),
                NetworkLogFragment.class, null);
    }
}