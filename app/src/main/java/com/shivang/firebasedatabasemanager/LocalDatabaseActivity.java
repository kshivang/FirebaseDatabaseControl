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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static com.shivang.firebasedatabasemanager.R.string.no_node_here;

/**
 * Created by kshivang on 22/10/16.
 * Contributors may mention there name below
 * Shivang
 *
 *
 * This activity just hold the fragment
 */

//Todo: This fragment would be used updated in use in another version of app
public class LocalDatabaseActivity extends AppCompatActivity{
    private String currentUri = "";
    private String baseUri = "";

    private View progressBarHolder;
    private ProgressBar progressBar;
    private TextView errorTest;
    private View view;
    private ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_database);
        findViewById(R.id.toolbar_view).setVisibility(View.GONE);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Local database");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        progressBarHolder = findViewById(R.id.progress_bar_holder);
        progressBar = (ProgressBar)  findViewById(R.id.progress_bar);
        errorTest = (TextView) findViewById(R.id.error_text);
        view = findViewById(R.id.fragment);
        onLocalDatabaseFragment((JsonCreator.onCreate(this)).fileNames(), null);
    }

    public void home(View view) {
        finish();
    }

    private void onLocalDatabaseFragment(ArrayList<String> files, String response) {
        if (files != null && files.size() > 0) {
            getSupportFragmentManager().
                    beginTransaction().replace(R.id.fragment,
                    LocalDatabaseFragment.newInstance(files)).
                    commit();
        } else if (response != null) {
            getSupportFragmentManager().
                    beginTransaction().replace(R.id.fragment,
                    LocalDatabaseFragment.newInstance(response)).
                    commit();
        } else {
            progressBar.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            progressBarHolder.setVisibility(View.VISIBLE);
            errorTest.setText(no_node_here);
            errorTest.setVisibility(View.VISIBLE);
        }
    }

    public void onFragmentSet(boolean isEmpty) {
        progressBar.setVisibility(View.GONE);
        if (isEmpty) {
            progressBarHolder.setVisibility(View.VISIBLE);
            errorTest.setText(no_node_here);
            errorTest.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        } else {
            progressBarHolder.setVisibility(View.GONE);
            errorTest.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        }
    }

    public void onForwardTransverse(String key) {
    }

    public void onRefresh() {
    }

    private void onBackwardTransverse() {
    }

    private void onUrlRequest(String url) {

    }

    @Override
    public void onBackPressed() {
//        if () {
//            finish();
//        } else {
            onBackwardTransverse();
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}