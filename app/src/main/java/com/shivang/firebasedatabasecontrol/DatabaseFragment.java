package com.shivang.firebasedatabasecontrol;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kshivang on 14/10/16.
 *
 */

public class DatabaseFragment extends Fragment{

    private static final String FLAG_ARG = "arg";

    public static DatabaseFragment newInstance(String base) {
        DatabaseFragment fragment = new DatabaseFragment();
        Bundle args = new Bundle();
        args.putString(FLAG_ARG, base);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_database, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

            customViewHolder.tvKey.setText(String.format("%s", keyValue.get("key")));
            if (!keyValue.get("value").contains("{")) {
                customViewHolder.tvValue.setVisibility(View.VISIBLE);
                customViewHolder.tvValue.setText(String.format("Value :   %s", keyValue.get("value")));
            } else {
                customViewHolder.tvValue.setVisibility(View.GONE);
            }

            customViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (keyValue.get("value").contains("{"))
                        ((DatabaseActivity) getActivity()).onForwardTransverse(keyValue.get("key"));
                    else {
                        Toast.makeText(getContext(), "This is value!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (null != mKeyValueList ? mKeyValueList.size() : 0);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView tvKey, tvValue;
            CardView cardView;

            CustomViewHolder(View view) {
                super(view);
                tvKey = (TextView) view.findViewById(R.id.tv_key);
                tvValue = (TextView) view.findViewById(R.id.tv_value);
                cardView = (CardView) view.findViewById(R.id.card);
            }
        }
    }
}
