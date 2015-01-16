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
 * database configuration settings manually when needed.  Also provides
 * sample code for making certain XMLAPI requests.
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
                .listId(getEngageConfigManager().engageListId())
//                .listId("29392")
                .build();
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

        // String columnName = "Custom Integration Test Id";
        // String columnName = "Mobile User Id";
        // String columnName = getEngageConfigManager().mergedRecipientIdColumnName();
//        String columnName = getEngageConfigManager().mergedDateColumnName();
        String columnName = "Custom Integration Test Id 2";

        int columnType = XMLAPIColumnType.TEXT_COLUMN.code();
//        int columnType = XMLAPIColumnType.TIMESTAMP.value();

        XMLAPI addListColumnXml = XMLAPI.builder()
                .operation(XMLAPIOperation.ADD_LIST_COLUMN)
                .listId(getEngageConfigManager().engageListId())
                .param(XMLAPIElement.COLUMN_NAME, columnName)
                .param(XMLAPIElement.COLUMN_TYPE, columnType)
                 //  .param(XMLAPIElement.DEFAULT, "")
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

    @Suppress
    public void testCreateAuditRecordTable() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        XMLAPI addListColumnXml = new XMLAPI(XMLAPIOperation.CREATE_TABLE) {
            // one way to hack the xml object ...you really shouldn't do this though... its not guaranteed to work forever
            @Override
            public String envelope() {
                return "<Envelope>\n" +
                        "\t<Body>\n" +
                        "\t\t<CreateTable>\n" +
                        "\t\t\t<TABLE_NAME>Audit Record Test</TABLE_NAME>\n" +
                        "\t\t\t<COLUMNS>\n" +
                        "\t\t\t\t<COLUMN>\n" +
                        "\t\t\t\t\t<NAME>Audit Record Id</NAME>\n" +
                        "\t\t\t\t\t<TYPE>TEXT</TYPE>\n" +
                        "\t\t\t\t\t<IS_REQUIRED>true</IS_REQUIRED>\n" +
                        "\t\t\t\t\t<KEY_COLUMN>true</KEY_COLUMN>\n" +
                        "\t\t\t\t</COLUMN>\n" +
                        "\t\t\t\t<COLUMN>\n" +
                        "\t\t\t\t\t<NAME>Old Recipient Id</NAME>\n" +
                        "\t\t\t\t\t<TYPE>TEXT</TYPE>\n" +
                        "\t\t\t\t\t<IS_REQUIRED>true</IS_REQUIRED>\n" +
                        "\t\t\t\t</COLUMN>\n" +
                        "\t\t\t\t<COLUMN>\n" +
                        "\t\t\t\t\t<NAME>New Recipient Id</NAME>\n" +
                        "\t\t\t\t\t<TYPE>TEXT</TYPE>\n" +
                        "\t\t\t\t\t<IS_REQUIRED>true</IS_REQUIRED>\n" +
                        "\t\t\t\t</COLUMN>\n" +
                        "\t\t\t\t<COLUMN>\n" +
                        "\t\t\t\t\t<NAME>Create Date</NAME>\n" +
                        "\t\t\t\t\t<TYPE>DATE_TIME</TYPE>\n" +
                        "\t\t\t\t\t<IS_REQUIRED>true</IS_REQUIRED>\n" +
                        "\t\t\t\t</COLUMN>\n" +
                        "\t\t\t</COLUMNS>\n" +
                        "\t\t</CreateTable>\n" +
                        "\t</Body>\n" +
                        "</Envelope>";
            }
        };

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
                Log.e(TAG, exception.getMessage(), exception);
                fail(exception.getMessage());
            }
        });

        signal.await(10, TimeUnit.SECONDS);
    }
}
