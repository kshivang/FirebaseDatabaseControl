package com.shivang.firebasedatabasecontrol;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.PATCH;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static com.shivang.firebasedatabasecontrol.JsonCreator.onValueDelete;

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
//        appController = AppController.getInstance(getContext());
        jsonCreator = JsonCreator.onCreate(getContext());

        getActivity().findViewById(R.id.bt_put).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionClick();
            }
        });
        getActivity().findViewById(R.id.bt_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().finish();
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
                jsonCreator.onAdd(PATCH);
            }
        });
        dialogView.findViewById(R.id.bt_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                jsonCreator.onAdd(POST);
            }
        });
        dialogView.findViewById(R.id.bt_put).setOnClickListener(new View.OnClickListener() {
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
                                jsonCreator.onAdd(PUT);
                            }
                        })
                        .setNeutralButton("Patch", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                jsonCreator.onAdd(PATCH);
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
                JsonCreator.exportFile(currentDatabase.toString(), getContext());
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
            if (response != null)
                ((DatabaseActivity) getContext()).onFragmentSet(response.isEmpty());
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
                    onValueDelete(keyValue.get("key"), getContext());
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
