package com.example.minidouyin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        findViewById(R.id.button).setOnClickListener(v -> {
            //todo 在这里申请相机、麦克风、存储的权限
            if (ContextCompat.checkSelfPermission(Main3Activity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(Main3Activity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                //todo 在这里申请相机、存储的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},0);
            }
            startActivity(new Intent(Main3Activity.this,Main2Activity.class));

        });
    }
}
