/*
 * Copyright (C) 2014 by MobileDeepLinking.org
 *
 * Permission is hereby granted, free of charge, to any
 * person obtaining a copy of this software and
 * associated documentation files (the "Software"), to
 * deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.silverpop.engage.deeplinking;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.mobiledeeplinking.android.Constants;
import org.mobiledeeplinking.android.DeeplinkMatcher;
import org.mobiledeeplinking.android.Handler;
import org.mobiledeeplinking.android.HandlerExecutor;
import org.mobiledeeplinking.android.IntentBuilder;
import org.mobiledeeplinking.android.MDLLog;
import org.mobiledeeplinking.android.MobileDeepLinkingConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EngageDeepLinkManager extends Activity
{
    private static final String TAG = EngageDeepLinkManager.class.getName();
    public static final String DEFAULT_HANDLER_NAME = "engageDefaultHandler";

    private static Map<String, Handler> handlers = null;
    private static MobileDeepLinkingConfig config = null;

    public EngageDeepLinkManager()
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Read JSON file and then route to the appropriate place.
        if (config == null)
        {
            config = getConfiguration();
            MDLLog.loggingEnabled = config.getLogging();
        }

        try
        {
            routeUsingUrl(this.getIntent().getData());
        }
        catch (JSONException e)
        {
            MDLLog.e("MobileDeepLinking", "Error parsing JSON!", e);
            throw new RuntimeException();
        }
    }

    public static void registerHandler(String name, org.mobiledeeplinking.android.Handler handler)
    {
        if (handlers == null)
        {
            handlers = new HashMap<String, Handler>();
        }
        handlers.put(name, handler);
    }

    private void routeUsingUrl(Uri deeplink) throws JSONException
    {
        // base case
        if (TextUtils.isEmpty(deeplink.getHost()) && (TextUtils.isEmpty(deeplink.getPath())))
        {
            MDLLog.e("MobileDeepLinking", "No Routes Match.");
            routeToDefault(deeplink);
            return;
        }

        Iterator<String> keys = config.getRoutes().keys();
        while (keys.hasNext())
        {
            String route = keys.next();
            JSONObject routeOptions = (JSONObject) config.getRoutes().get(route);
            try
            {
                Map<String, String> routeParameters = new HashMap<String, String>();
                routeParameters = DeeplinkMatcher.match(route, routeOptions, routeParameters, deeplink);
                if (routeParameters != null)
                {
                    handleRoute(routeOptions, routeParameters);
                    return;
                }
            }
            catch (JSONException e)
            {
                MDLLog.e("MobileDeepLinking", "Error parsing JSON!", e);
                break;
            }
            catch (Exception e)
            {
                MDLLog.e("MobileDeepLinking", "Error matching and handling route", e);
                break;
            }
        }

        // deeplink trimmer
        routeUsingUrl(trimDeeplink(deeplink));
    }

    private void routeToDefault(Uri deeplink) throws JSONException
    {
        MDLLog.d("MobileDeepLinking", "Routing to Default Route.");
        handleRoute(config.getDefaultRoute(), parseQueryParameters(deeplink));
    }

    private Map<String, String> parseQueryParameters(Uri deeplink) {
        if (deeplink != null) {
            Map<String, String> params = new HashMap<String, String>();

            String queryString = deeplink.getQuery();
            if (queryString != null) {
                String[] queryComponents = queryString.split("&");
                if (queryComponents != null && queryComponents.length > 0) {
                    for (String queryComponent : queryComponents) {
                        String[] queryParts = queryComponent.split("=");
                        if (queryParts != null && queryParts.length == 2) {
                            params.put(queryParts[0], queryParts[1]);
                        }
                    }
                }
            }

            return params;
        } else {
            return null;
        }
    }

    Uri trimDeeplink(Uri deeplink)
    {
        String host = deeplink.getHost();
        List<String> pathSegments = new LinkedList<String>(deeplink.getPathSegments());
        if (pathSegments.isEmpty())
        {
            // trim off host
            if (!TextUtils.isEmpty(host))
            {
                host = null;
            }
        }

        for (int i = pathSegments.size() - 1; i >= 0; i--)
        {
            // remove trailing slashes
            if (pathSegments.get(i).equals("/"))
            {
                pathSegments.remove(i);
            } else
            {
                pathSegments.remove(i);
                break;
            }
        }

        String pathString = "";
        for (int i = 0; i < pathSegments.size(); i++)
        {
            pathString += "/";
            pathString += pathSegments.get(i);
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(deeplink.getScheme());
        builder.path(pathString);
        builder.query(deeplink.getQuery());

        return builder.build();
    }

    private void handleRoute(JSONObject routeOptions, Map<String, String> routeParameters) throws JSONException
    {
        try {
            HandlerExecutor.executeHandlers(routeOptions, routeParameters, handlers);
            if (routeOptions.getString(Constants.CLASS_JSON_NAME) == null) {
                IntentBuilder.buildAndFireIntent(routeOptions, routeParameters, this);
            } else {
                Log.w(TAG, "No Activity class defined. Application cannot be opened but " +
                        "Engage deep link processing still happened!");
            }
        } catch (Exception ex) {
            Log.w(TAG, "No Activity class defined. Application cannot be opened but Engage " +
                    "deep link processing still happened!");
        }
    }

    private MobileDeepLinkingConfig getConfiguration()
    {
        try
        {
            String jsonString = readConfigFile();
            JSONObject json = new JSONObject(jsonString);
            return new MobileDeepLinkingConfig(json);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String readConfigFile() throws IOException
    {
        Resources resources = this.getApplicationContext().getResources();
        AssetManager assetManager = resources.getAssets();

        InputStream inputStream = assetManager.open("MobileDeepLinkingConfigDefault.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null)
        {
            sb.append(line + "\n");
        }
        return sb.toString();
    }
}
