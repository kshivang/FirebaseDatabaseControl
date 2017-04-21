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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.shivang.firebasedatabasemanager.BuildConfig;
import com.shivang.firebasedatabasemanager.R;
import com.shivang.firebasedatabasemanager.misc.AppController;
import com.shivang.firebasedatabasemanager.misc.JsonCreator;
import com.shivang.firebasedatabasemanager.misc.NodeModel;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Created by kshivang on 13/10/16.
 * This is main activity
 *
 */

public class LandingActivity extends AppCompatActivity implements JsonCreator.AuthRequest{

    private EditText etUrl;
    private AppController appController;
    private ProgressBar progressBar;
    private static final String TAG = "base";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing_activity);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.mipmap.ic_launcher);
            actionBar.setTitle(getString(R.string.firebase_database));
        }

        etUrl = (EditText) findViewById(R.id.etUrl);
        etUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etUrl.getText())) {
                    etUrl.setText(R.string.base_url_init);
                    SharedPreferences sp = getSharedPreferences(BuildConfig.APPLICATION_ID,
                            MODE_PRIVATE);
                    String lastUrl = sp.getString("lastUrl", null);
                    if (lastUrl != null) {
                        etUrl.setText(lastUrl);
                    }
                    etUrl.setSelection(etUrl.getText().toString().indexOf(".firebaseio"));
                }
            }
        });

        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onAccess(findViewById(R.id.bt_access));
                return true;
            }
        });
        appController = AppController.getInstance(LandingActivity.this);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        // ATTENTION: This was auto-generated to handle app links.
//        Intent appLinkIntent = getIntent();
//        String appLinkAction = appLinkIntent.getAction();
//        Uri appLinkData = appLinkIntent.getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        int itemId = item.getItemId();

        switch (itemId) {
//            case R.id.action_help :
//                break;
            case R.id.action_local_database:
                onManageDatabase();
                return true;
//            case R.id.action_remove_ads:
//                startActivity(new Intent(Intent.ACTION_VIEW)
//                        .setData(Uri.parse(getString(R.string.url_paid_version))));
//                return true;
            case R.id.action_clear_auth:
                getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
                        .edit().putString("auth", null).apply();
                Toast.makeText(this, "Authentication secret removed!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_clear_last_url:
                getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
                        .edit().putString("auth", null).apply();
                Toast.makeText(this, "Last used url removed!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_exit :
                finish();
                return true;
        }

        return onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        appController.cancelPendingRequests(TAG);
    }

    public void onAccess(View view) {
        /*
          hide keyboard if it is inflated
         */
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        appController.cancelPendingRequests(TAG);
        if (etUrl != null && !TextUtils.isEmpty(etUrl.getText())) {
            String url = etUrl.getText().toString();
            if (!url.contains(".json")) {
                if (url.charAt(url.length() - 1) == '/'){
                    url = url + ".json";
                }
                else url = url + "/.json";
            }
            onBaseUrlRequest(url, view);
        }
    }



    private void onBaseUrlRequest(final String url, final View view) {
        view.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (url.contains("https://") && url.contains(".firebaseio.com")) {

            StringRequest request = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            view.setVisibility(View.VISIBLE);
                            if (response != null) {
                                getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
                                        .edit().putString("lastUrl",
                                        url.substring(0, url.indexOf("/.json"))).apply();
                                JsonCreator.onAuthSave(LandingActivity.this, url);
                                startActivity(new Intent(LandingActivity.this, DatabaseActivity.class)
                                        .putExtra("response", response)
                                        .putExtra("base", url.substring(0, url.indexOf("/.json"))));
                            }
                        }
                    }, new Response.ErrorListener() {
                        @SuppressLint("SetJavaScriptEnabled")
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            JsonCreator.onRequestError(LandingActivity.this, error,
                                    url, progressBar, view);
                        }
                    }) {
                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }
            };
            request.setShouldCache(false);
            appController.addToRequestQueue(request, TAG);
        } else {
            Toast.makeText(this, "You entered a invalid firebase url!", Toast.LENGTH_SHORT).show();
            view.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }


    private void onManageDatabase() {

        final CharSequence[] files = fileNames();
        if (files != null && files.length > 0) {
            final boolean[] isCheckedItems = new boolean[files.length];
            Arrays.fill(isCheckedItems, false);

            new AlertDialog.Builder(this)
                    .setMultiChoiceItems(files, isCheckedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which, boolean isChecked) {
                                    isCheckedItems[which] = isChecked;
                                }
                            })
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            JsonCreator.onNodeAdd(new NodeModel(), JsonCreator.SAVE,
                                    LandingActivity.this);
                        }
                    })
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean isDeleted = false;
                            for (int i = 0; i < isCheckedItems.length; i++) {
                                if (isCheckedItems[i]) {
                                    File file = new File(getFilesDir().getAbsolutePath()
                                            + "/" + files[i]);
                                    if (file.exists() && file.delete()) {
                                        isDeleted = true;
                                    }
                                }
                            }
                            if (isDeleted) {
                                Toast.makeText(LandingActivity.this, "Deleted",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                onManageDatabase();
                                Toast.makeText(LandingActivity.this,
                                        "You have not selected any database to delete",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).create().show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Create new json database and save locally")
                    .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JsonCreator.onNodeAdd(new NodeModel(), JsonCreator.SAVE,
                                    LandingActivity.this);
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
    }


    private CharSequence[] fileNames() {
        FilenameFilter jsonFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json") || name.endsWith(".JSON");
            }
        };

        File root = getFilesDir();
        if (root != null && root.list().length > 0)
            return root.list(jsonFilter);
        return null;
    }

    @Override
    public void onUrlRequest(String url, View view) {
        onBaseUrlRequest(url, view);
    }
}
