package com.ss.android.ugc.chapter1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class testActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        String data = getIntent().getStringExtra("data");
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
    }
}
