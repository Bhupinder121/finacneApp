package com.example.finacneapp;

import android.service.media.MediaBrowserService;

import com.google.gson.JsonElement;

import java.util.List;

import javax.xml.transform.Result;

public interface Callback {
    void onSucess(List<JsonElement> value);
    void onFailure(String error);
}
