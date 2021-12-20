package com.example.finacneapp;

import android.service.media.MediaBrowserService;

import com.google.gson.JsonElement;

import org.json.JSONArray;

import java.util.List;

import javax.xml.transform.Result;

public interface Callback {
    void onSucess(String value);
}
