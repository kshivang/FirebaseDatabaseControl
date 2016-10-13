package com.shivang.firebasedatabasecontrol;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by kshivang on 13/10/16.
 * This activity just hold the fragment
 */

public class DatabaseActivity extends AppCompatActivity{
    private String base;
    private String current;
    private static final String TAG = "base";

    private AppController appController;
    private View progressBar;
    private View view;
    private boolean internetActive = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_database);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.mipmap.ic_launcher);
            actionBar.setTitle(getString(R.string.firebase_database));
        }
        base = getIntent().getStringExtra("base");
        current = base;
        appController = AppController.getInstance(DatabaseActivity.this);
        progressBar = findViewById(R.id.progress_bar);
        view = findViewById(R.id.fragment);
        onDatabaseFragment(getIntent().getStringExtra("response"));
    }

    private void onDatabaseFragment(String response) {
        getSupportFragmentManager().
                beginTransaction().replace(R.id.fragment,
                DatabaseFragment.newInstance(response)).
                commit();
        progressBar.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }

    public void onForwardTransverse(String key) {
        current = current + "/" + key;
        onUrlRequest(current + ".json");
    }

    private void onBackwardTransverse() {
        current = current.substring(0, current.lastIndexOf("/"));
        if (current.equals(base)) {
            onUrlRequest(current + "/.json");
            return;
        }
        onUrlRequest(current + ".json");
    }

    private void onUrlRequest(String url) {
        progressBar.setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        StringRequest request =new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        internetActive = true;
                        if (response != null) {
                            onDatabaseFragment(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        internetActive = false;
                        progressBar.setVisibility(View.GONE);
                        view.setVisibility(View.VISIBLE);
                        Toast.makeText(DatabaseActivity.this, "" + error.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
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
        if (!internetActive || current.equals(base)) {
            finish();
        } else {
            onBackwardTransverse();
        }
    }
}