package com.example.finacneapp;

import static com.example.finacneapp.MainActivity.TAG;

import static java.security.AccessController.getContext;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.json.JSONException;
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

    public void getData(String date_category, com.example.finacneapp.Callback callback){
        Call<String> call = retrofitInterface.getData(date_category);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String encrypedData = response.body().replace("t36i", "+").replace("8h3nk1", "/").replace("d3ink2", "=");
                callback.onSucess(Encryption_Decryption.decrypt(encrypedData));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onSucess(t.getMessage());
            }
        });
    }

    public void sendData(JSONObject data){
        JSONObject encryptedData = new JSONObject();
        try {
            encryptedData.put("json", Encryption_Decryption.encrypt(data.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Call<JSONObject> call = retrofitInterface.sendData(encryptedData);
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
