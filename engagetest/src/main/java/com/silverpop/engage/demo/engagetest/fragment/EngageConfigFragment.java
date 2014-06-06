package com.silverpop.engage.demo.engagetest.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.demo.engagetest.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class EngageConfigFragment
        extends ListFragment {

    private static final String TAG = EngageConfigFragment.class.getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        Context context = getActivity().getApplicationContext();
        Map<String, String> m = new HashMap<String, String>();
        m.put("Device ID", EngageConfig.deviceId(context));
        m.put("Device Name", EngageConfig.deviceName());
        m.put("Device Version", EngageConfig.deviceVersion());
        m.put("Anonymous User ID", EngageConfig.anonymousUserId(context));
        m.put("Primary User ID", EngageConfig.primaryUserId(context));
        m.put("OS Name", EngageConfig.osName(context));
        m.put("OS Version", EngageConfig.osVersion(context));
        m.put("App Name", EngageConfig.appName(context));
        m.put("App Version", EngageConfig.appVersion(context));
        m.put("Current Campaign", EngageConfig.currentCampaign(context));
        m.put("Last Campaign", EngageConfig.lastCampaign(context));
        if (EngageConfig.currentLocationCache() != null) {
            m.put("Current Location", "Lat :" + EngageConfig.currentLocationCache().getLatitude()
                    + " Long: " + EngageConfig.currentLocationCache().getLongitude());
        }
        if (EngageConfig.currentAddressCache() != null) {
            m.put("Location Address", EngageConfig.currentAddressCache().toString());
        }

        KeyValueListAdaptor adaptor = new KeyValueListAdaptor(getActivity(), m);
        setListAdapter(adaptor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.engageconfig_view, container, false);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data
        Log.d(TAG, "ListItem was clicked!");
    }


    private class KeyValueListAdaptor
            extends BaseAdapter {

        private Context context = null;
        private Map<String, String> data = null;
        private Map<Integer, String> positionToKeyMap = null;
        private LayoutInflater layoutInflater;

        public KeyValueListAdaptor(Context context, Map<String, String> values) {
            //super();
            this.context = context;
            this.data = values;
            this.layoutInflater = LayoutInflater.from(context);

            positionToKeyMap = new HashMap<Integer, String>();
            Iterator<Map.Entry<String, String>> itr = values.entrySet().iterator();
            Integer position = 0;
            while (itr.hasNext()) {
                Map.Entry<String, String> ent = itr.next();
                positionToKeyMap.put(position, ent.getKey());
                position++;
            }
        }

        @Override
        public int getCount() {
            return this.data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            View v = layoutInflater.inflate(R.layout.key_value_listviewitem, null);

            String key = positionToKeyMap.get(position);

            TextView keyText =(TextView)v.findViewById(R.id.keyValueKey);
            keyText.setText(key);

            TextView value =(TextView)v.findViewById(R.id.keyValueValue);
            value.setText(data.get(key));

            return v;
        }
    }
}