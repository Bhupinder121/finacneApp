package com.example.finacneapp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitInterface {
    @GET("/sendData")
    Call<String> getData(@Query("data_query") String data_category);

    @POST("/getData")
    Call<JSONObject> sendData(@Body JSONObject map);
}
