package com.silverpop.engage.response;

import com.silverpop.engage.exception.XMLResponseParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Parses the response from XMLAPI requests into usable components.
 *
 * Created by jeremydyer on 5/19/14.
 */
public class EngageResponseXML {

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

            xpp.setInput( new StringReader(xml));
            int eventType = xpp.getEventType();

            Stack<XMLAPIResponseNode> contextStack = new Stack<XMLAPIResponseNode>();
            XMLAPIResponseNode currentNode = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    currentNode = new XMLAPIResponseNode(xpp.getName());
                    contextStack.push(currentNode);
                } else if(eventType == XmlPullParser.END_TAG) {

                    if (contextStack.size() > 1) {
                        contextStack.pop(); //Must pop because current node is the last element on stack.
                        XMLAPIResponseNode parent = contextStack.pop();
                        parent.addChild(currentNode);
                        currentNode = parent;
                        contextStack.push(currentNode);
                    }

                } else if(eventType == XmlPullParser.TEXT) {
                    currentNode.setValue(xpp.getText());
                }
                eventType = xpp.next();
            }

            root = contextStack.pop();

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Locates the value for a "." delimited keypath.
     *
     * @param keyPath
     *  "." delimited string to locate the value.
     *
     * @return
     *      Value for the specified keypath.
     */
    public String valueForKeyPath(String keyPath) throws XMLResponseParseException {
        if (keyPath != null && keyPath.length() > 0) {
            String[] pathComponents = keyPath.split("\\.");

            if (pathComponents != null && pathComponents.length > 0) {
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

                    //Now get the value from the current node.
                    if (currentNode.isLeaf()) {
                        return currentNode.getValue();
                    } else {
                        throw new XMLResponseParseException("KeyPath " + keyPath
                                + " does not describe a leaf element!", getXml(), keyPath);
                    }

                } else {
                    throw new XMLResponseParseException("KeyPath " + keyPath
                            + " does not match response!", getXml(), keyPath);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
