package com.shivang.firebasedatabasecontrol;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by kshivang on 13/10/16.
 * This activity just hold the fragment
 */

public class DatabaseActivity extends AppCompatActivity{
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
        onDatabaseFragment(getIntent().getStringExtra("response"));
    }

    public void onDatabaseFragment(String response) {
        getSupportFragmentManager().
                beginTransaction().replace(R.id.fragment,
                DatabaseFragment.newInstance(response)).
                commit();
    }
}