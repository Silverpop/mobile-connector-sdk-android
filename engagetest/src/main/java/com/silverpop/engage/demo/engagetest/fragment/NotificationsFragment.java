package com.silverpop.engage.demo.engagetest.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.silverpop.engage.UBFManager;
import com.silverpop.engage.demo.engagetest.EngageApplication;
import com.silverpop.engage.demo.engagetest.EngageNotificationReceiver;
import com.silverpop.engage.demo.engagetest.R;

/**
 * Created by jeremydyer on 6/6/14.
 */
public class NotificationsFragment
    extends Fragment {

    int YOURAPP_NOTIFICATION_ID = 1234567890;

    Button mMockRemoteNotificationButton;
    Button mMockLocalNotificationButton;
    Button mMockNotificationCurrentCampaignButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notifications_view, container, false);

        mMockRemoteNotificationButton = (Button)v.findViewById(R.id.mockRemoteNotificationButton);
        mMockRemoteNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Notification notification = new Notification(R.drawable.abc_ic_search, "EngageSDK-Demo Testing Notification", System.currentTimeMillis());
                Intent intent = new Intent(getActivity(), EngageNotificationReceiver.class);
                notification.setLatestEventInfo(getActivity(), "contentTitle", "contentText",
                        PendingIntent.getActivity(getActivity(), 1, intent, 0));

                NotificationManager notificationManager =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);


                notificationManager.notify(YOURAPP_NOTIFICATION_ID, notification);
                EngageApplication application = (EngageApplication) getActivity().getApplication();
                UBFManager ubfManager = application.getUbfManager();
                ubfManager.handleNotificationReceivedEvents(getActivity(), notification, null);
            }
        });

        mMockLocalNotificationButton = (Button)v.findViewById(R.id.mockLocalNotificationButton);

        mMockNotificationCurrentCampaignButton = (Button)v.findViewById(R.id.mockLocalNotificationCurrentCampaignButton);

        return v;
    }
}
