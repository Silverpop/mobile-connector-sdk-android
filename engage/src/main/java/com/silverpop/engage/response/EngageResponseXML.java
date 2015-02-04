package com.silverpop.engage.response;

import android.text.TextUtils;
import android.util.Log;
import com.silverpop.engage.exception.XMLResponseParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Parses the response from XMLAPI requests into usable components.
 * <p/>
 * Created by jeremydyer on 5/19/14.
 */
public class EngageResponseXML {

    private static final String TAG = EngageResponseXML.class.getName();

    private String xml = null;
    private XMLAPIResponseNode root = null;

    public EngageResponseXML(String xml) {
        setXml(xml);
        this.parseXMLToTreeStructure(xml);
    }

    private void parseXMLToTreeStructure(String xml) {
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            //Remove unwanted xml Characters like \n
            xml = xml.replace("\n", "");

            xpp.setInput(new StringReader(xml));
            int eventType = xpp.getEventType();

            Stack<XMLAPIResponseNode> contextStack = new Stack<XMLAPIResponseNode>();
            XMLAPIResponseNode currentNode = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d(TAG, "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    currentNode = new XMLAPIResponseNode(xpp.getName());
                    contextStack.push(currentNode);
                } else if (eventType == XmlPullParser.END_TAG) {

                    if (contextStack.size() > 1) {
                        contextStack.pop(); //Must pop because current node is the last element on stack.
                        XMLAPIResponseNode parent = contextStack.pop();
                        parent.addChild(currentNode);
                        currentNode = parent;
                        contextStack.push(currentNode);
                    }

                } else if (eventType == XmlPullParser.TEXT) {
                    currentNode.setValue(xpp.getText());
                }
                eventType = xpp.next();
            }

            root = contextStack.pop();

        } catch (Exception ex) {
            Log.e(TAG, "Error parsing response xml tree structure: " + ex.getMessage(), ex);
        }
    }

    /**
     * Locates the value for a "." delimited keypath.
     *
     * @param keyPath "." delimited string to locate the value.
     * @return Value for the specified keypath.
     */
    public String valueForKeyPath(String keyPath) throws XMLResponseParseException {
        XMLAPIResponseNode foundNode = getNodeByPath(keyPath);
        if (foundNode != null) {
            //Now get the value from the current node.
            if (foundNode.isLeaf()) {
                return foundNode.getValue();
            } else {
                throw new XMLResponseParseException("KeyPath " + keyPath
                        + " does not describe a leaf element!", getXml(), keyPath);
            }
        }
        return null;
    }

    private XMLAPIResponseNode getNodeByPath(String keyPath) throws XMLResponseParseException {
        if (!TextUtils.isEmpty(keyPath)) {
            String[] pathComponents = keyPath.split("\\.");

            if (pathComponents.length > 0) {
                //Make sure that the first element matches the root name.
                if (root.getName().equalsIgnoreCase(pathComponents[0])) {
                    XMLAPIResponseNode currentNode = root;
                    for (int i = 1; i < pathComponents.length; i++) {
                        currentNode = currentNode.childByName(pathComponents[i]);
                        if (currentNode == null) {
                            throw new XMLResponseParseException("KeyPath " + keyPath
                                    + " does not match response!", getXml(), keyPath);
                        }
                    }
                    return currentNode;
                } else {
                    throw new XMLResponseParseException("KeyPath " + keyPath
                            + " does not match response!", getXml(), keyPath);
                }
            }
        }
        return null;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Map<String, String> getColumns() {
        Map<String, String> columns = new HashMap<String, String>();
        try {
            XMLAPIResponseNode columnsNode = getNodeByPath("envelope.body.result.columns");
            if (columnsNode != null) {
                List<XMLAPIResponseNode> childColumns = columnsNode.getChildren();
                if (childColumns != null) {
                    for (XMLAPIResponseNode childColumn : childColumns) {
                        XMLAPIResponseNode nameNode = childColumn.childByName("name");
                        if (nameNode != null) {
                            String name = nameNode.getValue();
                            if (!TextUtils.isEmpty(name)) {
                                String value = null;
                                XMLAPIResponseNode valueNode = childColumn.childByName("value");
                                if (valueNode != null) {
                                    value = valueNode.getValue();
                                }
                                columns.put(name, value);
                            }
                        }
                    }
                }
            }
        } catch (XMLResponseParseException e) {
            Log.e(TAG, "Error parsing columns from response: " + e.getMessage(), e);
        }

        return columns;
    }

    public String getColumnValue(String columnName) {
        Map<String, String> columns = getColumns();
        String columnValue = columns.get(columnName);
        return columnValue;
    }

    public boolean isSuccess() {
        boolean success = false;
        try {
            final String result = valueForKeyPath("envelope.body.result.success");
            success = "true".equalsIgnoreCase(result);

        } catch (XMLResponseParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return success;
    }

    /**
     * @param path
     * @return the value for the specified path or {@code null} if not found
     */
    public String getString(String path) {
        String stringValue = null;
        try {
            stringValue = valueForKeyPath(path);
        } catch (XMLResponseParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return stringValue;
    }

    /**
     * @param path
     * @return the Integer value for the specified path or {@code null} if not found
     */
    public Integer getInteger(String path) {
        String stringValue = getString(path);
        Integer intVal = null;
        if (!TextUtils.isEmpty(stringValue)) {
            try {
                intVal = Integer.valueOf(stringValue);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, nfe.getMessage(), nfe);
            }
        }
        return intVal;
    }

    public String getFaultString() {
        return getString("envelope.body.fault.faultstring");
    }

    public Integer getErrorId() {
        return getInteger("envelope.body.fault.detail.error.errorid");
    }
}
