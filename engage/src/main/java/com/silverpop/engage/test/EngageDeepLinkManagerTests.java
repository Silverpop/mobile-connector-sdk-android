package com.silverpop.engage.test;

import android.test.AndroidTestCase;

import com.silverpop.engage.deeplinking.EngageDeepLinkManager;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by jeremydyer on 5/19/14.
 */
public class EngageDeepLinkManagerTests
    extends AndroidTestCase {

//    public void testEngageDeepLinkManagerParseQueryParams() throws URISyntaxException, UnsupportedEncodingException {
//        EngageDeepLinkManager manager = EngageDeepLinkManager.get(getContext());
//        String url = "MakeAndBuild://test/5?CurrentCampaign=Jeremy";
//        url = URLEncoder.encode(url, "UTF-8");
//        Map<String, String> queryParams = manager.parseURLQueryParams(new URI(url));
//        assertTrue(queryParams.size() == 1);
//        assertTrue(queryParams.get("CurrentCampaign").equals("Jeremy"));
//    }
//
//    public void testSpaceInKey() throws URISyntaxException, UnsupportedEncodingException {
//        EngageDeepLinkManager manager = EngageDeepLinkManager.get(getContext());
//        String url = "MakeAndBuild://test/5?Current Campaign=Jeremy";
//        url = URLEncoder.encode(url, "UTF-8");
//        Map<String, String> queryParams = manager.parseURLQueryParams(new URI(url));
//        assertTrue(queryParams.size() == 1);
//        assertTrue(queryParams.get("Current Campaign").equals("Jeremy"));
//    }
//
//    public void testSpaceInValue() throws URISyntaxException, UnsupportedEncodingException {
//        EngageDeepLinkManager manager = EngageDeepLinkManager.get(getContext());
//        String url = "MakeAndBuild://test/5?CurrentCampaign=Jeremy 2";
//        url = URLEncoder.encode(url, "UTF-8");
//        Map<String, String> queryParams = manager.parseURLQueryParams(new URI(url));
//        assertTrue(queryParams.size() == 1);
//        assertTrue(queryParams.get("CurrentCampaign").equals("Jeremy 2"));
//    }
//
//    public void testSpaceInBoth() throws URISyntaxException, UnsupportedEncodingException {
//        EngageDeepLinkManager manager = EngageDeepLinkManager.get(getContext());
//        String url = "MakeAndBuild://test/5?Current Campaign=Jeremy 2";
//        url = URLEncoder.encode(url, "UTF-8");
//        Map<String, String> queryParams = manager.parseURLQueryParams(new URI(url));
//        assertTrue(queryParams.size() == 1);
//        assertTrue(queryParams.get("Current Campaign").equals("Jeremy 2"));
//    }
//
//    public void testEngageDeepLinkManagerParseRouteParams() {
//        assertTrue(false);
//    }
//
//    public void testEngageDeepLinkManagerQueryParams() {
//        assertTrue(false);
//    }
//
//    public void testEngageDeepLinkManagerPostSilverpopHandler() {
//        assertTrue(false);
//    }
//
//    public void testEngageDeepLinkManagerNoParameters() {
//        assertTrue(false);
//    }
//
//    public void testEngageDeepLinkManagerCurveballsUrls() {
//        assertTrue(false);
//    }
}
