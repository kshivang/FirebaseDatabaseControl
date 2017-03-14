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

package com.shivang.firebasedatabasemanager.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.shivang.firebasedatabasemanager.R;
import com.shivang.firebasedatabasemanager.fragment.DatabaseFragment;
import com.shivang.firebasedatabasemanager.misc.AppController;
import com.shivang.firebasedatabasemanager.misc.JsonCreator;

import static com.shivang.firebasedatabasemanager.R.string.no_node_here;

/**
 * Created by kshivang on 13/10/16.
 * Contributors may mention there name below
 * Shivang
 *
 *
 * This activity just hold the fragment
 */

public class DatabaseActivity extends AppCompatActivity{
    private String baseURL;
    private String currentURL;
    private static final String TAG = "Database";

    private AppController appController;
    private View progressBarHolder;
    private ProgressBar progressBar;
    private TextView errorTest;
    private View view;
    private EditText etUrl;
    private boolean internetProblem = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_database);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        etUrl = (EditText) findViewById(R.id.etUrl);
        baseURL = getIntent().getStringExtra("base");
        currentURL = baseURL;
        etUrl.append(currentURL);
        appController = AppController.getInstance(DatabaseActivity.this);

        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                go(findViewById(R.id.bt_go));
                return true;
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DatabaseActivity.this.onRefresh();
            }
        });

        progressBarHolder = findViewById(R.id.progress_bar_holder);
        progressBar = (ProgressBar)  findViewById(R.id.progress_bar);
        errorTest = (TextView) findViewById(R.id.error_text);
        view = findViewById(R.id.fragment);
        onDatabaseFragment(getIntent().getStringExtra("response"));
    }

    public void home(View view) {
        finish();
    }

    public void go(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (!TextUtils.isEmpty(etUrl.getText())) {
            currentURL = etUrl.getText().toString();
            if (!currentURL.contains(baseURL)) {
                baseURL = currentURL;
            }
            if (!currentURL.contains("{")) {
                onUrlRequest(currentURL + "/.json");
            } else {
                onUrlRequest(currentURL + ".json");
            }
        }
    }

    private void onDatabaseFragment(String response) {
        if (response != null) {
            getSupportFragmentManager().
                    beginTransaction().replace(R.id.fragment,
                    DatabaseFragment.newInstance(response)).
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
        currentURL = currentURL + "/" + key;
        etUrl.setText("");
        etUrl.append(currentURL);
        onUrlRequest(currentURL + ".json");
    }

    public void onRefresh() {
        if (currentURL.equals(baseURL))
            onUrlRequest(currentURL + "/.json");
        else {
            onUrlRequest(currentURL + ".json");
        }
    }

    private void onBackwardTransverse() {
        currentURL = currentURL.substring(0, currentURL.lastIndexOf("/"));
        if (currentURL.equals(baseURL)) {
            etUrl.setText("");
            etUrl.append(currentURL);
            onUrlRequest(currentURL + "/.json");
            return;
        }
        etUrl.setText("");
        etUrl.append(currentURL);
        onUrlRequest(currentURL + ".json");
    }

    private void onUrlRequest(String url) {
        progressBarHolder.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        errorTest.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        StringRequest request =new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        internetProblem = false;
                        onDatabaseFragment(response);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        internetProblem = true;
                        progressBarHolder.setVisibility(View.VISIBLE);
                        errorTest.setText(R.string.intenet_problem);
                        errorTest.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setRefreshing(false);

                        view.setVisibility(View.GONE);
                        JsonCreator.onRequestError(DatabaseActivity.this,
                                error, progressBar, null);

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

    @Override
    public void onBackPressed() {
        if (internetProblem || currentURL.equals(baseURL)) {
            finish();
        } else {
            onBackwardTransverse();
        }
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


    public String getCurrentURL() {
        return currentURL;
    }

    public Boolean isBaseUrl() {
        return baseURL.equals(currentURL);
    }
}