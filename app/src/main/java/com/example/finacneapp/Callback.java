package com.example.finacneapp;

import android.service.media.MediaBrowserService;

import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

public interface Callback {
    void StringData(String value) throws JSONException;

    void JsonData(JSONArray jsonObjects) throws JSONException;
}
