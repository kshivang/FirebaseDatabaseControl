package com.shivang.firebasedatabasecontrol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kshivang on 19/10/16.
 *
 */

class NodeModel{

    private NodeModel parentNode;
    private HashMap<String, Object> values;
    private ArrayList<String> keys;
    private String key;


    NodeModel getParent() {
        return parentNode;
    }

    void setParent(NodeModel parentNode) {
        this.parentNode = parentNode;
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public Object getValue() {
        return values;
    }

    Object child(String key) {
        if (values.containsKey(key))
            return values.get(key);
        else
            return null;
    }

    void setValues(Object value) {
        setValues(key, value);
    }

    void setValues(String key, Object value) {
        if (values == null) {
            values = new HashMap<>();
        }
        values.put(key, value);
    }

    ArrayList<String> getKeys() {
        return keys;
    }

    void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }
}
