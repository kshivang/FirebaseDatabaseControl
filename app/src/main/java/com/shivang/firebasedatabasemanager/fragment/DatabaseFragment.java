/*
 *   Copyright (C) 2017  Shivang<shivang.iitk@gmail.com>
 *
 *   This file is part of Firebase Database Manager.
 *
 *       Firebase Database Manager is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       Firebase Database Manager is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with Firebase Database Manager.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shivang.firebasedatabasemanager.fragment;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shivang.firebasedatabasemanager.misc.JsonCreator;
import com.shivang.firebasedatabasemanager.misc.NodeModel;
import com.shivang.firebasedatabasemanager.R;
import com.shivang.firebasedatabasemanager.activity.DatabaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.PATCH;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static com.shivang.firebasedatabasemanager.misc.JsonCreator.onValueDelete;

/**
 * Created by kshivang on 14/10/16.
 * Contributors may mention there name below
 * Shivang
 *
 *
 * This Fragment hold a single instance of Database
 */

public class DatabaseFragment extends Fragment{

    private static final String FLAG_ARG = "arg";
//    private static final String TAG = "Database";

    public static DatabaseFragment newInstance(String base) {
        DatabaseFragment fragment = new DatabaseFragment();
        Bundle args = new Bundle();
        args.putString(FLAG_ARG, base);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;

    private final static int FLAG_STRING = 0, FLAG_INT = 1,
            FLAG_BOOLEAN = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_database, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getActivity().findViewById(R.id.bt_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionClick();
            }
        });

        return rootView;
    }

    public void onActionClick() {
        final View dialogView = View.inflate(getContext(), R.layout.dialog_action, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Action on current node")
                .setView(dialogView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        dialogView.findViewById(R.id.bt_patch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                JsonCreator.onAdd(getContext(), PATCH);
            }
        });
        dialogView.findViewById(R.id.bt_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                JsonCreator.onAdd(getContext(), POST);
            }
        });
        dialogView.findViewById(R.id.bt_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Action")
                        .setMessage("If you post here all data in current node will be lost and" +
                                " new data will be created. \n\n Note: If you want to add or" +
                                " update current data set choose patch instead.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                JsonCreator.onAdd(getContext(), PUT);
                            }
                        })
                        .setNeutralButton("Patch", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                JsonCreator.onAdd(getContext(), PATCH);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
        dialogView.findViewById(R.id.bt_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure, " +
                                "you want to completely delete the current node ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                JsonCreator.onValueDelete(null, getContext());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
        dialogView.findViewById(R.id.bt_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (currentDatabase.toString() != null
                        && !currentDatabase.toString().equals(""))
                    JsonCreator.exportFile(currentDatabase.toString(), getContext());
                else
                    Toast.makeText(getContext(), "There is no data to export!",
                            Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        new JsonParse().execute(getArguments().getString(FLAG_ARG));
    }

    private JSONObject currentDatabase;

    private class JsonParse extends AsyncTask<String, Void, List<Map<String, String>>> {
        protected List<Map<String, String>> doInBackground(String... params) {
            try {
                currentDatabase = new JSONObject(params[0]);
                List<Map<String, String>> response = new ArrayList<>();
                if (currentDatabase.length() > 0) {
                    for (int i = 0; i < currentDatabase.length(); i++) {
                        String key = currentDatabase.names().getString(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("key", key);
                        map.put("value", currentDatabase.get(key).toString());
                        response.add(map);
                    }
                }
                return response;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<Map<String, String>> response) {
            DatabaseRecyclerAdapter adapter = new DatabaseRecyclerAdapter(response);
            mRecyclerView.setAdapter(adapter);
            if (response != null && !(response.size() == 0 || response.isEmpty()))
                ((DatabaseActivity) getContext()).onFragmentSet(false);
            else
                ((DatabaseActivity) getContext()).onFragmentSet(true);
        }
    }

    private class DatabaseRecyclerAdapter
            extends RecyclerView.Adapter<DatabaseRecyclerAdapter.CustomViewHolder> {

        List<Map<String, String>> mKeyValueList;

        DatabaseRecyclerAdapter(List<Map<String, String>> keyValue) {
            this.mKeyValueList = keyValue;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.list_key_data, viewGroup, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CustomViewHolder customViewHolder, int position) {
            final Map<String, String> keyValue = mKeyValueList.get(position);

            customViewHolder.tvKey.setText(keyValue.get("key"));

            if (keyValue.get("value").contains("{")) {
                customViewHolder.tvValue.setVisibility(View.GONE);
                customViewHolder.tvValueTitle.setVisibility(View.GONE);
                customViewHolder.btEdit.setText(R.string.open);
            } else {
                customViewHolder.tvValue.setVisibility(View.VISIBLE);
                customViewHolder.tvValueTitle.setVisibility(View.VISIBLE);
                customViewHolder.btEdit.setText(R.string.edit);
                customViewHolder.tvValue.setText(String.format(" %s", keyValue.get("value")));
            }


            customViewHolder.ivExpandViewToggle.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (customViewHolder.llExpandView.getVisibility() == View.VISIBLE) {
                                customViewHolder.llExpandView.setVisibility(View.GONE);
                                customViewHolder.ivExpandViewToggle.setImageDrawable(
                                        ContextCompat.getDrawable(getContext(),
                                                R.drawable.ic_more_vert));
                            } else {
                                customViewHolder.llExpandView.setVisibility(View.VISIBLE);
                                customViewHolder.ivExpandViewToggle.setImageDrawable(
                                        ContextCompat.getDrawable(getContext(),
                                                R.drawable.ic_arrow_drop_up));
                            }
                        }
                    });

            customViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String value = keyValue.get("value");
                    String key = keyValue.get("key");

                    if (value.contains("{")) {
                        ((DatabaseActivity) getContext())
                                .onForwardTransverse(key);
                    } else {
                        customViewHolder.llExpandView.setVisibility(View.VISIBLE);
                        customViewHolder.ivExpandViewToggle.setImageDrawable(
                                ContextCompat.getDrawable(getContext(),
                                        R.drawable.ic_arrow_drop_up));
                    }
                }
            });

            customViewHolder.btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Are sure you want to delete this node?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    onValueDelete(keyValue.get("key"), getContext());
                                }
                            })
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            });

            customViewHolder.btEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String value = keyValue.get("value");
                    String key = keyValue.get("key");

                    if (keyValue.get("value").contains("{")) {
                        ((DatabaseActivity) getContext())
                                .onForwardTransverse(key);
                    } else {
                        NodeModel args = new NodeModel();
                        ArrayList<String> keys = new ArrayList<>();
                        keys.add(key);

                        args.setKeys(keys);
                        args.setKey(key);

                        if (value.contains(Boolean.FALSE.toString()) ||
                                value.contains(Boolean.TRUE.toString())) {
                            JsonCreator.onValueType(args, FLAG_BOOLEAN, getContext(), PATCH);
                        } else if (TextUtils.isDigitsOnly(value) ||
                                (value.contains("-") &&
                                        TextUtils.isDigitsOnly(value.substring(1)))) {
                            JsonCreator.onValueType(args, FLAG_INT, getContext(), PATCH);
                        } else {
                            JsonCreator.onValueType(args, FLAG_STRING, getContext(), PATCH);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (null != mKeyValueList ? mKeyValueList.size() : 0);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView tvKey, tvValue, tvValueTitle;
            LinearLayout llExpandView;
            ImageView ivExpandViewToggle;
            Button btDelete, btEdit;
            CardView cardView;

            CustomViewHolder(View view) {
                super(view);
                tvKey = (TextView) view.findViewById(R.id.tv_key);
                tvValue = (TextView) view.findViewById(R.id.tv_value);
                tvValueTitle = (TextView) view.findViewById(R.id.tv_value_title);
                llExpandView = (LinearLayout) view.findViewById(R.id.expand_view);
                ivExpandViewToggle = (ImageView) view.findViewById(R.id.expand_toggle);
                btDelete = (Button)  view.findViewById(R.id.bt_delete);
                btEdit = (Button) view.findViewById(R.id.bt_edit);
                cardView = (CardView) view.findViewById(R.id.card);
            }
        }
    }
}
