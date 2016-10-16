package com.shivang.firebasedatabasecontrol;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kshivang on 14/10/16.
 *
 */

public class DatabaseFragment extends Fragment{

    private static final String FLAG_ARG = "arg";
    private static final String TAG = "Database";

    public static DatabaseFragment newInstance(String base) {
        DatabaseFragment fragment = new DatabaseFragment();
        Bundle args = new Bundle();
        args.putString(FLAG_ARG, base);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private AppController appController;

    private int type  = -1;
    private final static int FLAG_STRING = 0, FLAG_INT = 1,
            FLAG_BOOLEAN = 2, FLAG_JSON = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_database, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appController = AppController.getInstance(getContext());
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        new JsonParse().execute(getArguments().getString(FLAG_ARG));
    }

    private class JsonParse extends AsyncTask<String, Void, List<Map<String, String>>> {
        protected List<Map<String, String>> doInBackground(String... params) {
            try {
                JSONObject base = new JSONObject(params[0]);
                List<Map<String, String>> response = new ArrayList<>();
                if (base.length() > 0) {
                    for (int i = 0; i < base.length(); i++) {
                        String key = base.names().getString(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("key", key);
                        map.put("value", base.get(key).toString());
                        response.add(map);
                    }
                    return response;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<Map<String, String>> response) {
            DatabaseRecyclerAdapter adapter = new DatabaseRecyclerAdapter(response);
            mRecyclerView.setAdapter(adapter);
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
        public void onBindViewHolder(CustomViewHolder customViewHolder, int position) {
            final Map<String, String> keyValue = mKeyValueList.get(position);

            customViewHolder.tvKey.setText(keyValue.get("key"));
            if (keyValue.get("value").contains("{")) {
                customViewHolder.tvValue.setVisibility(View.GONE);
                customViewHolder.tvValueTitle.setVisibility(View.GONE);
            } else {
                customViewHolder.tvValue.setVisibility(View.VISIBLE);
                customViewHolder.tvValueTitle.setVisibility(View.VISIBLE);
                customViewHolder.tvValue.setText(String.format(" %s", keyValue.get("value")));
            }

            customViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String value = keyValue.get("value");
                    String key = keyValue.get("key");
                    if (value.contains("{")) {
                        ((DatabaseActivity) getActivity())
                                .onForwardTransverse(key);
                    } else {
                        if (value.contains(Boolean.FALSE.toString()) ||
                                value.contains(Boolean.TRUE.toString())) {
                            onValueType(FLAG_BOOLEAN, key);
                        } else if (TextUtils.isDigitsOnly(value) ||
                                (value.contains("-") &&
                                        TextUtils.isDigitsOnly(value.substring(1)))) {
                            onValueType(FLAG_INT, key);
                        } else
                            onValueType(FLAG_STRING, key);
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
            CardView cardView;

            CustomViewHolder(View view) {
                super(view);
                tvKey = (TextView) view.findViewById(R.id.tv_key);
                tvValue = (TextView) view.findViewById(R.id.tv_value);
                tvValueTitle = (TextView) view.findViewById(R.id.tv_value_title);
                cardView = (CardView) view.findViewById(R.id.card);
            }
        }
    }

    private void onValueType(int identifiedType, final String key) {
        type = identifiedType;
        new AlertDialog.Builder(getContext())
                .setTitle("Choose value Type")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSingleChoiceItems(R.array.value_type, identifiedType, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        type = which;
                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onValueChange(type, key);
                    }
                }).create().show();
    }

    private EditText input;
    private void onValueChange(int Flag, final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle("Choose value");

        switch (Flag){
            case FLAG_STRING:
                input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText()))
                            Toast.makeText(getContext(), "Empty string can't be set",
                                    Toast.LENGTH_SHORT).show();
                        else
                            onValuePatch(FLAG_STRING, key, input.getText().toString());
                    }
                });
                break;
            case FLAG_INT:
                input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputStr;
                        if (TextUtils.isEmpty(input.getText())) {
                            Toast.makeText(getContext(), "Empty integer can't be set",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            inputStr = input.getText().toString();
                        }
                        if (TextUtils.isDigitsOnly(inputStr) || ((inputStr.contains("-") ||
                                inputStr.contains("+")) &&
                                TextUtils.isDigitsOnly(inputStr.substring(1)))) {
                            onValuePatch(FLAG_INT, key, inputStr);
                        }
                    }
                });
                break;
            case FLAG_BOOLEAN:
                builder.setSingleChoiceItems(R.array.bool_value, -1, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onValuePatch(FLAG_BOOLEAN, key, String.valueOf(which));
                        dialog.dismiss();
                    }
                });
                break;
            case FLAG_JSON:
                TextView textView = new TextView(getContext());
                textView.setText(R.string.currently_unavailable);
                builder.setView(textView);
        }
        builder.create().show();
    }

    private void onValuePatch(int Flag, String key, String value) {

        JSONObject object;
        switch (Flag) {
            case FLAG_BOOLEAN:
                LinkedHashMap<String, Boolean> bMap = new LinkedHashMap<>();
                if (value.contains(String.valueOf(0))) {
                    bMap.put(key, true);
                } else {
                    bMap.put(key, false);
                }
                object = new JSONObject(bMap);
                break;
            case FLAG_INT:
                LinkedHashMap<String, Integer> iMap = new LinkedHashMap<>();
                iMap.put(key, Integer.valueOf(value));
                object = new JSONObject(iMap);
                break;
            case FLAG_STRING:
                LinkedHashMap<String, String> sMap = new LinkedHashMap<>();
                sMap.put(key, value);
                object = new JSONObject(sMap);
                break;
            default:
                sMap = new LinkedHashMap<>();
                sMap.put(key, value);
                object = new JSONObject(sMap);
        }

        onProgress();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH,
                ((DatabaseActivity) getActivity()).getCurrentURL(),
                object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Value changed!", Toast.LENGTH_SHORT).show();
                        ((DatabaseActivity)getActivity()).onRefresh();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "" + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };
        request.setShouldCache(false);
        appController.addToRequestQueue(request, TAG);
    }

    private ProgressDialog progressDialog;

    private void onProgress() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Changing value");
        progressDialog.show();
    }
}
