package com.silverpop.engage.demo.engagetest.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.silverpop.engage.UBFManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.demo.engagetest.R;
import com.silverpop.engage.domain.UBF;
import com.silverpop.engage.domain.XMLAPI;
import com.silverpop.engage.exception.XMLResponseParseException;
import com.silverpop.engage.response.EngageResponseXML;

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.ubfapi_view, container, false);

        mInstalled = (Button)v.findViewById(R.id.ubfInstalledButton);
        mInstalled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF installed = UBF.installed(getActivity(), null);
                UBFManager.get().postEvent(installed);
            }
        });

        mSessionStart = (Button)v.findViewById(R.id.ubfSessionStarted);
        mSessionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF sessionStarted = UBF.sessionStarted(getActivity(), null, EngageConfig.currentCampaign(getActivity()));
                UBFManager.get().postEvent(sessionStarted);
            }
        });

        mSessionEnd = (Button)v.findViewById(R.id.ubfSessionEnded);
        mSessionEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF sessionEnded = UBF.sessionEnded(getActivity(), null);
                UBFManager.get().postEvent(sessionEnded);
            }
        });

        mGoalStart = (Button)v.findViewById(R.id.ubfGoalStarted);
        mGoalStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF namedGoal = UBF.goalCompleted(getActivity(), "EngageSDK-Demo Goal Name", null);
                UBFManager.get().postEvent(namedGoal);
            }
        });

        mGoalAbandoned = (Button)v.findViewById(R.id.ubfGoalAbandoned);
        mGoalAbandoned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF goalAban = UBF.goalAbandoned(getActivity(), "EngageSDK-Demo Goal Name", null);
                UBFManager.get().postEvent(goalAban);
            }
        });

        mNamedEvent = (Button)v.findViewById(R.id.ubfNamedEvent);
        mNamedEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF namedEvent = UBF.namedEvent(getActivity(), "EngageSDK-Demo Testing Goal Name", null);
                UBFManager.get().postEvent(namedEvent);
            }
        });

        mNamedEvent = (Button)v.findViewById(R.id.ubfNamedEvent);
        mNamedEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UBF namedEvent = UBF.namedEvent(getActivity(), "EngageSDK-Demo Testing Goal Name", null);
                UBFManager.get().postEvent(namedEvent);
            }
        });

        return v;
    }
}
