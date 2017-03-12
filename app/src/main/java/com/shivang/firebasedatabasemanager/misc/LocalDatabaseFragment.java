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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shivang.firebasedatabasemanager.R;
import com.shivang.firebasedatabasemanager.activity.LocalDatabaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kshivang on 14/10/16.
 * Contributors may mention there name below
 * Shivang
 *
 *
 * This Fragment hold a single instance of Database
 */

public class LocalDatabaseFragment extends Fragment{

    private static final String FLAG_ARG = "arg";
    private static final String FLAG_FILES = "files";
//    private static final String TAG = "Database";

    public static LocalDatabaseFragment newInstance(String base) {
        LocalDatabaseFragment fragment = new LocalDatabaseFragment();
        Bundle args = new Bundle();
        args.putString(FLAG_ARG, base);
        fragment.setArguments(args);
        return fragment;
    }

    public static LocalDatabaseFragment newInstance(ArrayList<String> base) {
        LocalDatabaseFragment fragment = new LocalDatabaseFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(FLAG_FILES, base);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
//    private AppController appController;

    //    private int type  = -1;
    private final static int FLAG_STRING = 0, FLAG_INT = 1,
            FLAG_BOOLEAN = 2;

    private JsonCreator jsonCreator;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_database, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jsonCreator = JsonCreator.onCreate(getContext());

        getActivity().findViewById(R.id.bt_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (getArguments().getStringArrayList(FLAG_FILES) == null) {
            new JsonParse().execute(getArguments().getString(FLAG_ARG));
        } else {

        }
    }

    private class JsonParse extends AsyncTask<String, Void, NodeModel>{

        private NodeModel parse(JSONObject object){
            try {
                if (object.length() > 0) {
                    NodeModel response = new NodeModel();
                    ArrayList<String> keys = new ArrayList<>();
                    response.setKeys(keys);
                    for (int i = 0; i < object.length(); i++) {
                        String key = object.names().getString(i);
                        keys.add(key);
                        Object value = object.get(key);
                        if (value.getClass() == JSONObject.class) {
                            value = parse((JSONObject) value);
                        }
                        response.setValues(key, value);
                    }
                    return response;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected NodeModel doInBackground(String... params) {

            try {
                JSONObject currentDatabase = new JSONObject(params[0]);
                return parse(currentDatabase);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(NodeModel response) {

            if (response != null) {
                DatabaseRecyclerAdapter adapter = DatabaseRecyclerAdapter
                        .newInstance(response, getContext(), new DatabaseRecyclerAdapter.Callback() {
                            @Override
                            void onKeysDelete(ArrayList<String> updatedKeys) {

                            }

                            @Override
                            void onClickItem(String key) {

                            }

                            @Override
                            void onEditValue(NodeModel currentNode, Class mClass) {

                            }
                        });
                mRecyclerView.setAdapter(adapter);

                ((LocalDatabaseActivity) getContext()).onFragmentSet(response.getKeys().isEmpty());
            }
            else
                ((LocalDatabaseActivity) getContext()).onFragmentSet(true);
        }
    }
}
