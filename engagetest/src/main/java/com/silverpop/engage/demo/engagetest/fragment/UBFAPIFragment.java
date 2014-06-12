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
    private Button mReceivedNotification;
    private Button mOpenedNotification;

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

        return v;
    }
}
