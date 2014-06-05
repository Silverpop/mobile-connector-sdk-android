package com.silverpop.engage.demo.engagetest.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.silverpop.engage.demo.engagetest.R;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class EngageConfigFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.engageconfig_view, container, false);
        return v;
    }
}