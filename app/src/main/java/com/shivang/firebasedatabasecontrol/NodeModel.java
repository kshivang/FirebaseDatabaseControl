package com.shivang.firebasedatabasecontrol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kshivang on 19/10/16.
 *
 */

public class NodeModel{

    private NodeModel parentNode;
    private HashMap<String, Object> values;
    private ArrayList<String> keys;
    private String key;


    public NodeModel getParent() {
        return parentNode;
    }

    public void setParent(NodeModel parentNode) {
        this.parentNode = parentNode;
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public Object getValue() {
        return values;
    }

    public Object child(String key) {
        return values.get(key);
    }

    public void setValues(Object value) {
        setValues(key, value);
    }

    public void setValues(String key, Object value) {
        if (values == null) {
            values = new HashMap<>();
        }
        values.put(key, value);
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
