package com.silverpop.engage;

import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import com.silverpop.BaseAndroidTest;
import com.silverpop.engage.domain.*;
import com.silverpop.engage.response.EngageResponseXML;
import com.silverpop.engage.response.handler.XMLAPIResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Not a real test class.  Created so you can easily check the
 * database configuration settings manually when needed.
 * <p/>
 * Created by Lindsay Thurmond on 1/12/15.
 */
public class ManualConfigUtil_IT extends BaseAndroidTest {

    private static final String TAG = ManualConfigUtil_IT.class.getName();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clearSharedPreferences();
        initManagers();
        // wait a few seconds to allow silverpop auth
        TimeUnit.SECONDS.sleep(5);
    }

    @Suppress
    public void testGetDatabaseMeta() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        XMLAPI getListMetaDataXml = XMLAPI.builder().operation(XMLAPIOperation.GET_LIST_META_DATA)
                .listId(getEngageConfigManager().engageListId()).build();
        getXMLAPIManager().postXMLAPI(getListMetaDataXml, new XMLAPIResponseHandler() {
            @Override
            public void onSuccess(EngageResponseXML response) {
                // test only exists so you can manually check this xml value
                String xml = response.getXml();
                Log.i(TAG, xml);

                signal.countDown();
            }

            @Override
            public void onFailure(Exception exception) {
                fail(exception.getMessage());
            }
        });

        signal.await(5, TimeUnit.SECONDS);
    }

    @Suppress
    public void testGetLists() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        XMLAPI getLists = XMLAPI.builder()
                .operation(XMLAPIOperation.GET_LISTS)
                .param(XMLAPIElement.VISIBILITY, 0)
                .param(XMLAPIElement.LIST_TYPE, XMLAPIListType.DATABASES.toString())
                .build();
        getXMLAPIManager().postXMLAPI(getLists, new XMLAPIResponseHandler() {
            @Override
            public void onSuccess(EngageResponseXML response) {
                // test only exists so you can manually check this xml value
                String xml = response.getXml();
                Log.i(TAG, xml);

                if (!response.isSuccess()) {
                    fail(response.getFaultString());
                }

                signal.countDown();
            }

            @Override
            public void onFailure(Exception exception) {
                fail(exception.getMessage());
            }
        });

        signal.await(5, TimeUnit.SECONDS);
    }

    @Suppress
    public void testCreateDatabaseColumn() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

//        String columnName = "Custom Integration Test Id";
        String columnName = "Mobile User Id";
        int columnType = XMLAPIColumnType.TEXT_COLUMN.value();

        XMLAPI addListColumnXml = XMLAPI.builder()
                .operation(XMLAPIOperation.ADD_LIST_COLUMN)
                .listId(getEngageConfigManager().engageListId())
                .param(XMLAPIElement.COLUMN_NAME, columnName)
                .param(XMLAPIElement.COLUMN_TYPE, columnType)
                .param(XMLAPIElement.DEFAULT, "")
                .build();

        getXMLAPIManager().postXMLAPI(addListColumnXml, new XMLAPIResponseHandler() {
            @Override
            public void onSuccess(EngageResponseXML response) {
                String xml = response.getXml();
                Log.i(TAG, xml);

                if (!response.isSuccess()) {
                    fail(response.getFaultString());
                }

                signal.countDown();
            }

            @Override
            public void onFailure(Exception exception) {
                fail(exception.getMessage());
            }
        });

        signal.await(10, TimeUnit.SECONDS);
    }
}
