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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shivang.firebasedatabasemanager.R;

import java.util.ArrayList;

/**
 * Created by kshivang on 21/10/16.
 *
 */

class DatabaseRecyclerAdapter
        extends RecyclerView.Adapter<DatabaseRecyclerAdapter.CustomViewHolder> {

    private ArrayList<String> keys;
    private NodeModel currentNode;
    private Context mContext;
    private boolean isSubNode = false;
    private Callback mCallback;

    static DatabaseRecyclerAdapter newInstance(NodeModel nodeModel,
                                               Context context, Callback callback) {
        return new DatabaseRecyclerAdapter(nodeModel, context, callback);
    }
    private DatabaseRecyclerAdapter(NodeModel nodeModel,
                                    Context context, Callback callback) {
        currentNode = nodeModel;
        keys = nodeModel.getKeys();
        mContext = context;
        mCallback = callback;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.list_key_data, viewGroup, false);
        return new DatabaseRecyclerAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DatabaseRecyclerAdapter.CustomViewHolder customViewHolder,
                                 int position) {
        final String key = keys.get(position);

        customViewHolder.tvKey.setText(key);

        Object child = currentNode.child(key);
        if (child != null) {
            Class mClass = child.getClass();
            isSubNode = false;
            String value;


            if (mClass == String.class) {
                value = (String) child;
            } else if (mClass == int.class || mClass == Integer.class) {
                value = String.valueOf((int) child);
            } else if (mClass == boolean.class || mClass == Boolean.class) {
                value = String.valueOf((boolean) child);
            } else {
                isSubNode = true;
                value = "subnode";
            }

            if (isSubNode) {
                customViewHolder.tvValue.setVisibility(View.GONE);
                customViewHolder.tvValueTitle.setVisibility(View.GONE);
                customViewHolder.btEdit.setText(R.string.open);
            } else {
                customViewHolder.tvValue.setVisibility(View.VISIBLE);
                customViewHolder.tvValueTitle.setVisibility(View.VISIBLE);
                customViewHolder.btEdit.setText(R.string.edit);
                customViewHolder.tvValue.setText(String.format(" %s", value));
            }
        } else {
            customViewHolder.tvValueTitle.setVisibility(View.GONE);
            customViewHolder.tvValue.setText(R.string.error_loading_value);
            customViewHolder.btEdit.setText(R.string.edit);
        }


        customViewHolder.ivExpandViewToggle.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (customViewHolder.llExpandView.getVisibility() == View.VISIBLE) {
                            customViewHolder.llExpandView.setVisibility(View.GONE);
                            customViewHolder.ivExpandViewToggle.setImageDrawable(
                                    ContextCompat.getDrawable(mContext,
                                            R.drawable.ic_more_vert));
                        } else {
                            customViewHolder.llExpandView.setVisibility(View.VISIBLE);
                            customViewHolder.ivExpandViewToggle.setImageDrawable(
                                    ContextCompat.getDrawable(mContext,
                                            R.drawable.ic_arrow_drop_up));
                        }
                    }
                });

        customViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSubNode){
                    mCallback.onClickItem(keys.get(customViewHolder.getAdapterPosition()));
                } else {
                    customViewHolder.llExpandView.setVisibility(View.VISIBLE);
                    customViewHolder.ivExpandViewToggle.setImageDrawable(
                            ContextCompat.getDrawable(mContext,
                                    R.drawable.ic_arrow_drop_up));
                }
            }
        });

        customViewHolder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> mKeys = keys;
                mKeys.remove(keys.get(customViewHolder.getAdapterPosition()));
                mCallback.onKeysDelete(mKeys);
            }
        });

        customViewHolder.btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = customViewHolder.getAdapterPosition();
                if (isSubNode){
                    mCallback.onClickItem(keys.get(position));
                } else {
                    Object child = currentNode.child(keys.get(position));
                    if (child != null) {
                        mCallback.onEditValue(currentNode,
                                child.getClass());
                    } else {
                        mCallback.onEditValue(currentNode, String.class);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != keys ? keys.size() : 0);
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

    abstract static class Callback {
        abstract void onKeysDelete(ArrayList<String> updatedKeys);
        abstract void onClickItem(String key);
        abstract void onEditValue(NodeModel currentNode, Class mClass);
    }
}

