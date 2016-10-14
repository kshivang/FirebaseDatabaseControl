package com.shivang.firebasedatabasecontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by kshivang on 13/10/16.
 *
 */

public class MainActivity extends AppCompatActivity{

    private EditText etUrl;
    private AppController appController;
    private ProgressBar progressBar;
    private static final String TAG = "base";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
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
                    etUrl.setSelection(8);
                }
            }
        });
        appController = AppController.getInstance(MainActivity.this);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
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
            case R.id.action_exit :
                finish();
        }

        return onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        appController.cancelPendingRequests(TAG);
    }

    public void read(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        appController.cancelPendingRequests(TAG);
        if (etUrl != null && !TextUtils.isEmpty(etUrl.getText())) {
            if (view != null)
                view.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
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
        StringRequest request =new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        view.setVisibility(View.VISIBLE);
                        if (response != null) {
                            startActivity(new Intent(MainActivity.this, DatabaseActivity.class)
                                    .putExtra("response", response)
                                    .putExtra("base", url.substring(0, url.indexOf("/.json"))));
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        view.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "" + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
}
