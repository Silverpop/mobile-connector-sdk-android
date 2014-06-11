package com.silverpop.engage.demo.engagetest.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.silverpop.engage.demo.engagetest.R;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class UBFAPIFragment
    extends Fragment {

    private static final String TAG = UBFAPIFragment.class.getName();

    private Button mInstalled;
    private Button mSessionStart;
    private Button mSessionEnd;
    private Button mGoalStart;
    private Button mGoalAbandoned;
    private Button mNamedEvent;
    private Button mReceivedNotification;
    private Button mOpenedNotification;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.ubfapi_view, container, false);



        return v;
    }
}
