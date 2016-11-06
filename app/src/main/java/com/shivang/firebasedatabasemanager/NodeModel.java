/*
 * This file is part of Firebase Database Manager.
 *
 *     Firebase Database Manager is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Firebase Database Manager is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Firebase Database Manager.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shivang.firebasedatabasemanager;

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
