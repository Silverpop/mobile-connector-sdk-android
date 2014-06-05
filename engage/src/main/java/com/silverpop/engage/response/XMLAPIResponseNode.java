package com.silverpop.engage.response;

import java.util.ArrayList;

/**
 * Created by jeremydyer on 5/23/14.
 */
public class XMLAPIResponseNode {

    private String name;
    private String value;
    private ArrayList<XMLAPIResponseNode> children = new ArrayList<XMLAPIResponseNode>();

    public XMLAPIResponseNode(String name) {
        setName(name);
    }

    public boolean isLeaf() {
        if (children.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public XMLAPIResponseNode childByName(String name) {
        for (XMLAPIResponseNode child : children) {
            if (child.getName().equalsIgnoreCase(name)) {
                return child;
            }
        }
        return null;
    }

    public void addChild(XMLAPIResponseNode child) {
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ArrayList<XMLAPIResponseNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<XMLAPIResponseNode> children) {
        this.children = children;
    }
}
