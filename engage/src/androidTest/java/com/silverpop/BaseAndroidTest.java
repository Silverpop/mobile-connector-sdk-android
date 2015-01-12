package com.silverpop;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;
import com.silverpop.engage.MobileConnectorManager;
import com.silverpop.engage.UBFManager;
import com.silverpop.engage.XMLAPIManager;
import com.silverpop.engage.config.EngageConfig;
import com.silverpop.engage.config.EngageConfigManager;
import com.silverpop.engage.network.AuthenticationHandler;
import com.silverpop.engage.network.Credential;
import com.silverpop.engage.network.EngageConnectionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Lindsay Thurmond on 1/2/15.
 */
public class BaseAndroidTest extends AndroidTestCase {

    private Context testContext;

    /**
     * @return The {@link Context} of the test project.
     */
    protected Context testContext() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (testContext == null) {
            Method getTestContext = AndroidTestCase.class.getMethod("getTestContext");
            testContext = (Context) getTestContext.invoke(this);
        }
        return testContext;
    }

    protected Credential getCredential() throws PackageManager.NameNotFoundException {
//        ApplicationInfo appInfo = getContext().getPackageManager().getApplicationInfo(
//                getContext().getPackageName(), PackageManager.GET_META_DATA);
//        Bundle bundle = appInfo.metaData;

//        String clientId = bundle.getString(EngageApplication.CLIENT_ID_META);
//        String clientSecret = bundle.getString(EngageApplication.CLIENT_SECRET_META);
//        String refreshToken = bundle.getString(EngageApplication.REFRESH_TOKEN_META);
//        String host = bundle.getString(EngageApplication.HOST);

        //[Lindsay Thurmond:1/12/15] TODO: read these properties from file
        String clientId = "02eb567b-3674-4c48-8418-dbf17e0194fc";
        String clientSecret = "9c650c5b-bcb8-4eb3-bf0a-cc8ad9f41580";
        String refreshToken = "676476e8-2d1f-45f9-9460-a2489640f41a";
        String host = "https://apipilot.silverpop.com/";

        return new Credential(clientId, clientSecret, refreshToken, host);
    }

    protected void initManagers() throws Exception {

        Credential credential = getCredential();

        EngageConnectionManager.init(getContext(), credential.getClientId(), credential.getClientSecret(),
                credential.getRefreshToken(), credential.getHost(),
                new AuthenticationHandler() {
                    @Override
                    public void onSuccess(String response) {
                    }

                    @Override
                    public void onFailure(Exception exception) {
                    }
                });

        // init singletons
        XMLAPIManager.init(getContext());
        UBFManager.init(getContext());
        MobileConnectorManager.init(getContext());
    }

    protected void clearSharedPreferences() throws Exception {
        SharedPreferences settings = testContext().getSharedPreferences(
                EngageConfig.SharedProperties.ENGAGE_CONFIG_PREF_ID.toString(), Context.MODE_PRIVATE);
        settings.edit().clear().commit();
//        EngageConfig.storeMobileUserId(getContext(), null);
//        EngageConfig.storeAnonymousUserId(getContext(), null);
    }

    protected EngageConfigManager getEngageConfigManager() {
        return EngageConfigManager.get(getContext());
    }

    protected XMLAPIManager getXMLAPIManager() {
        return XMLAPIManager.get();
    }

    protected UBFManager getUBFManager() {
        return UBFManager.get();
    }
}
