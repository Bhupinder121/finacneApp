package com.example.finacneapp;

import static com.example.finacneapp.MainActivity.TAG;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class serverConnector{
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;

    String baseUrl = "https://tesl-server.herokuapp.com";

//    String baseUrl = "http://192.168.0.118:4068";

    public void setup(){
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    public void getData(String date_category, Context context, com.example.finacneapp.Callback callback){
        date_category = Encryption_Decryption.encrypt(date_category).replace("+", "t36i")
                .replace("/", "8h3nk1").replace("=", "d3ink2"); // Add encryption
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = baseUrl+"/sendData?data_query="+date_category;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String encrypedData = response.replace("t36i", "+").replace("8h3nk1", "/").replace("d3ink2", "=");
                            String decryptionData = Encryption_Decryption.decrypt(encrypedData);
                            callback.StringData(decryptionData);
                            JSONArray jsonArray = new JSONArray(decryptionData);
                            callback.JsonData(jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: "+error);
            }
        });

        queue.add(stringRequest);


    }

    public void sendData(JSONObject data, com.example.finacneapp.Callback callback){
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
                    try {
                        callback.StringData(response.message());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
