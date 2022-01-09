package com.example.finacneapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;


public class loading_screen extends AppCompatActivity {

    public static ArrayList<String> category_name = new ArrayList<>();
    public static ArrayList<Integer> category_amt = new ArrayList<>();
    public static JSONArray tableData = new JSONArray();
    public static serverConnector connector;
    int total = 2;
    int count = 0;
    public static String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
    static String TAG = "mes";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        getSupportActionBar().hide();
        connector = new serverConnector();
        connector.setup();
        String currentYear = new SimpleDateFormat("yyyy").format(new Date());

        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                categoryDataSetup(new Callback() {
                    @Override
                    public void StringData(String value) throws JSONException {
                        update();
                    }

                    @Override
                    public void JsonData(JSONArray jsonObjects) throws JSONException {

                    }
                });
            }
        }, 100);

       new java.util.Timer().schedule(new TimerTask() {
           @Override
           public void run() {
               getTableData(getTableName(months[new Date().getMonth()], currentYear), loading_screen.this, new Callback() {
                   @Override
                   public void StringData(String value) {
                   }

                   @RequiresApi(api = Build.VERSION_CODES.O)
                   @Override
                   public void JsonData(JSONArray jsonObjects) {
                       tableData = jsonObjects;
                       update();
                   }
               });
           }
       }, 200);


    }

    void update(){
        Log.i(TAG, "update: "+count);
        count ++;
        if(count == total){
            startActivity(new Intent(loading_screen.this, MainActivity.class));
            finish();
        }
    }

    public static void getTableData(String tableName, Context context, Callback callback){

        String command = String.format("SELECT * FROM %s", tableName);
        Log.i(TAG, "getTableData: "+command);
        connector.getData(command, context, new Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void StringData(String value) {
                try {
                    Log.i("mes", "StringData: "+value);
                    JSONArray jsonArray = new JSONArray(value);
                    callback.JsonData(jsonArray);
                    callback.StringData(value);

                } catch (JSONException e) {
                    Log.e("mes", "StringData: "+e.getMessage() );
                }

            }

            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
    }

    private void categoryDataSetup(Callback callback){
        connector.getData("SELECT * FROM exp_category", this, new Callback() {
            @Override
            public void StringData(String value) {
                try {
                    category_name = new ArrayList();
                    category_amt = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(value);
                    for (int i = 0; i < jsonArray.length(); i++){
                        String category = jsonArray.getJSONObject(i).getString("ExpCate");
                        if(!category_name.contains(category)) {
                            category_name.add(category);
                        }
                    }
                    if(category_name.size()>0) {
                        for (int i = 0; i < category_name.size(); i++) {
                            int amt = 0;
                            for (int j = 0; j < jsonArray.length(); j++) {
                                String jsonCategory = jsonArray.getJSONObject(j).getString("ExpCate");
                                if(jsonCategory.equals(category_name.get(i))){
                                    int categoryAmt = jsonArray.getJSONObject(j).getInt("ExpAmt");
                                    amt += categoryAmt;
                                }
                            }
                            category_amt.add(amt);
                        }
                    }
                    callback.StringData("Done");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
    }

    public static String getTableName(String month,String year){
        return String.format("month_%s_%s", month, year);
    }
}