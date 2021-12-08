package com.example.finacneapp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitInterface {
    @GET("/getData")
    Call<List<JsonElement>> getData();

    @POST("/sendData")
    Call<JSONObject> sendData(@Body JSONObject map);
}
