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
public class UBFAPIFragment
    extends Fragment {

    /** An array of items to display in ArrayList */
    String apple_versions[] = new String[]{
            "Mountain Lion",
            "Lion",
            "Snow Leopard",
            "Leopard",
            "Tiger",
            "Panther",
            "Jaguar",
            "Puma"
    };

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        /** Creating array adapter to set data in listview */
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_multiple_choice, apple_versions);
//
//        /** Setting the array adapter to the listview */
//        setListAdapter(adapter);
//
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }

    @Override
    public void onStart() {
        super.onStart();

//        /** Setting the multiselect choice mode for the listview */
//        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.ubfapi_view, container, false);
        return v;
    }
}
