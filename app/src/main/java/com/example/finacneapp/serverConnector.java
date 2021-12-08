package com.example.finacneapp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class serverConnector {
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;
    String baseUrl = "http://192.168.0.118:420";

    public void setup(){
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    public void getData(com.example.finacneapp.Callback callback){
        Call<List<JsonElement>> call = retrofitInterface.getData();
        call.enqueue(new Callback<List<JsonElement>>() {
            @Override
            public void onResponse(Call<List<JsonElement>> call, Response<List<JsonElement>> response) {
                callback.onSucess(response.body());
            }

            @Override
            public void onFailure(Call<List<JsonElement>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void sendData(JSONObject data){
        Call<JSONObject> call = retrofitInterface.sendData(data);
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                if (response.code() == 200) {
                    Log.i("data", response.message());
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.i("data", t.getMessage());
            }
        });
    }
}
