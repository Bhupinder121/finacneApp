package com.example.finacneapp;


import static com.example.finacneapp.MainActivity.TAG;
import static com.example.finacneapp.MainActivity.connector;
import static com.example.finacneapp.MainActivity.currentYear;
import static com.example.finacneapp.MainActivity.getTableData;
import static com.example.finacneapp.MainActivity.getTableName;
import static com.example.finacneapp.MainActivity.months;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class DataReview extends AppCompatActivity implements OnChartValueSelectedListener {
    private BarChart barChart;
    ArrayList<String> monthsName = new ArrayList<>();
    ArrayList<JSONObject> perMonthData = new ArrayList<>();
    Spinner month_selector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_review);

        barChart = findViewById(R.id.barChart);
        month_selector = findViewById(R.id.month_Selector);


        getMonthsName(DataReview.this, new Callback() {
            @Override
            public void StringData(String value) throws JSONException {

            }

            @Override
            public void JsonData(JSONArray jsonObjects){
                try {
                    addMonthName(jsonObjects);
                    month_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Object name = parent.getItemAtPosition(position);
                            setDataOnChart((String) name);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void setDataOnChart(String monthName){
        ArrayList<String> monthsLabel = new ArrayList<>();
        boolean isThere = false;
        Log.i(TAG, "setDataOnChart: "+perMonthData.size());
        for (int i = 0; i < perMonthData.size(); i++) {
            if(perMonthData.get(i).toString().contains(monthName)){
                isThere = true;
            }
        }
        if(!isThere){
            addMonthDataToArray(monthName, new Callback() {
                @Override
                public void StringData(String value) throws JSONException {
                    Log.i(TAG, "StringData: added");
                    showDataOnChart(new JSONArray(value));
                }

                @Override
                public void JsonData(JSONArray jsonObjects) throws JSONException {

                }
            });
        }
        else {
            for (int i = 0; i < perMonthData.size(); i++) {
                if(perMonthData.get(i).toString().contains(monthName)){
                    try {
                        showDataOnChart(perMonthData.get(i).getJSONArray(monthName));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void showDataOnChart(JSONArray daysData){
        ArrayList<BarEntry> values = new ArrayList<>();
        for (int j = 0; j < daysData.length(); j++) {
            int exp = 0;
            try {
                exp = daysData.getJSONObject(j).getInt("exp");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            values.add(new BarEntry(j, exp));
        }
        BarDataSet barDataSet = new BarDataSet(values, "Dummy Data");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(16f);
        BarData barData = new BarData(barDataSet);
        XAxis xAxis = barChart.getXAxis();

        xAxis.setValueFormatter(barChart.getDefaultValueFormatter());

        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getAxisRight().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setData(barData);
        barChart.animateY(500);
    }

    private void addMonthDataToArray(String monthName, Callback callback){
        getTableData(monthName, this, new Callback() {
            @Override
            public void StringData(String value) throws JSONException {
                JSONArray jsonArray = new JSONArray(value);
                perMonthData.add(new JSONObject().put(monthName, jsonArray));
                callback.StringData(value);
            }

            @Override
            public void JsonData(JSONArray jsonObjects) throws JSONException {

            }
        });
    }

    private void addMonthName(JSONArray jsonObjects) throws JSONException {
        monthsName = new ArrayList<>();
        monthsName.add(getTableName(months[new Date().getMonth()], currentYear));
        for (int i = 0; i < jsonObjects.length(); i++) {
            String monthTableName = jsonObjects.getJSONObject(i).getString("TABLE_NAME");
            if(!monthsName.contains(monthTableName)) {
                monthsName.add(monthTableName);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_month_selector, monthsName);

        month_selector.setAdapter(adapter);
        int position = adapter.getPosition(monthsName.get(0));
        month_selector.setSelection(position);
    }

    private void getMonthsName(Context context, Callback callback){
        String command = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'month_%'";
        connector.getData(command, context, new Callback() {
            @Override
            public void StringData(String value) throws JSONException {
                JSONArray jsonArray = new JSONArray(value);
                callback.JsonData(jsonArray);
            }

            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}

