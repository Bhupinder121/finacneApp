package com.example.finacneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    int count = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Decrypt decrypt = new Decrypt();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decrypt.decrypt();
        }
    }
}