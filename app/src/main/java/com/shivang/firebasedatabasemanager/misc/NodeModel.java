/*
 * Copyright (C) 2016  Shivang
 *
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

package com.shivang.firebasedatabasemanager.misc;

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


    @SuppressWarnings("WeakerAccess")
    public NodeModel getParent() {
        return parentNode;
    }

    @SuppressWarnings("WeakerAccess")
    public void setParent(NodeModel parentNode) {
        this.parentNode = parentNode;
    }

    @SuppressWarnings("unused")
    public HashMap<String, Object> getValues() {
        return values;
    }

    @SuppressWarnings("unused")
    public Object getValue() {
        return values;
    }

    @SuppressWarnings("WeakerAccess")
    public Object child(String key) {
        if (values.containsKey(key))
            return values.get(key);
        else
            return null;
    }

    @SuppressWarnings("WeakerAccess")
    public void setValues(Object value) {
        setValues(key, value);
    }

    @SuppressWarnings("WeakerAccess")
    public void setValues(String key, Object value) {
        if (values == null) {
            values = new HashMap<>();
        }
        values.put(key, value);
    }

    @SuppressWarnings("WeakerAccess")
    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    @SuppressWarnings("WeakerAccess")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
