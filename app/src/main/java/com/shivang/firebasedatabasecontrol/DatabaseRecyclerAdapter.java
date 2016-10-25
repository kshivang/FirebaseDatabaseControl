package com.shivang.firebasedatabasecontrol;

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
                                 final int position) {
        String key = keys.get(position);
        final Class mClass = currentNode.child(key).getClass();
        isSubNode = false;
        String value;


        if (mClass == String.class) {
            value = (String)currentNode.child(key);
        } else if (mClass == int.class || mClass == Integer.class) {
            value = String.valueOf((int)currentNode.child(key));
        } else if (mClass == boolean.class || mClass == Boolean.class){
            value = String.valueOf((boolean)currentNode.child(key));
        } else {
            isSubNode = true;
            value = "subnode";
        }

        customViewHolder.tvKey.setText(key);

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
                    mCallback.onClickItem(keys.get(position));
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
                mKeys.remove(keys.get(position));
                mCallback.onKeysDelete(mKeys);
            }
        });

        customViewHolder.btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSubNode){
                    mCallback.onClickItem(keys.get(position));
                } else {
                    mCallback.onEditValue(currentNode,
                            currentNode.child(keys.get(position)).getClass());
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

