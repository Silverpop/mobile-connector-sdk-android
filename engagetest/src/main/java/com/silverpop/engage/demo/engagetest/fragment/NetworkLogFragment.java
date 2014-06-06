package com.silverpop.engage.demo.engagetest.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.silverpop.engage.demo.engagetest.R;

/**
 * Created by jeremydyer on 6/5/14.
 */
public class NetworkLogFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedPreferences) {
        super.onCreate(savedPreferences);
//        Cursor mCursor = getNetworkLogs();
//        CursorLoader cursorLoader = new CursorLoader(getActivity());
//
//        //startManagingCursor(mCursor);
//        // now create a new list adapter bound to the cursor.
//        // SimpleListAdapter is designed for binding to a Cursor.
//        ListAdapter adapter = new SimpleCursorAdapter(this, // Context.
//                android.R.layout.two_line_list_item,
//                // Specify the row template
//                // to use (here, two
//                // columns bound to the
//                // two retrieved cursor
//                // rows).
//                mCursor, // Pass in the cursor to bind to.
//                // Array of cursor columns to bind to.
//                new String[] { ContactsContract.Contacts._ID,
//                        ContactsContract.Contacts.DISPLAY_NAME },
//                // Parallel array of which template objects to bind to those
//                // columns.
//                new int[] { android.R.id.text1, android.R.id.text2 });
//
//        // Bind to our new adapter.
//        setListAdapter(adapter);
    }

//    private Cursor getNetworkLogs() {
//
////        // Run query
////        Uri uri = ContactsContract.Contacts.CONTENT_URI;
////        String[] projection = new String[] { ContactsContract.Contacts._ID,
////                ContactsContract.Contacts.DISPLAY_NAME };
////        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
////                + ("1") + "'";
////        String[] selectionArgs = null;
////        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
////                + " COLLATE LOCALIZED ASC";
////
////        return managedQuery(uri, projection, selection, selectionArgs,
////                sortOrder);
//    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.networklog_view, container, false);
        return v;
    }
}
