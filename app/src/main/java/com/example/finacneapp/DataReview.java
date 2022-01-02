package com.example.finacneapp;


import static com.example.finacneapp.MainActivity.TAG;
import static com.example.finacneapp.MainActivity.currentYear;
import static com.example.finacneapp.MainActivity.getTableData;
import static com.example.finacneapp.MainActivity.getTableName;
import static com.example.finacneapp.MainActivity.months;

import android.graphics.Color;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataReview extends AppCompatActivity implements OnChartValueSelectedListener {
    private BarChart barChart;
    ArrayList<String> monthsName = new ArrayList<>();
    Spinner month_selector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_review);

        barChart = findViewById(R.id.barChart);
        month_selector = findViewById(R.id.month_Selector);

        MainActivity.monthDataSetup(new Callback() {
            @Override
            public void StringData(String value) {

            }
            @Override
            public void JsonData(JSONArray jsonObjects) {
                try {
                    setData(false, getTableName(months[new Date().getMonth()], currentYear), jsonObjects);
                    monthsName = new ArrayList<>();
                    monthsName.add("all month");
                    monthsName.add(getTableName(months[new Date().getMonth()], currentYear));
                    for (int i = 0; i < jsonObjects.length(); i++) {
                        String monthTableName = jsonObjects.getString(i).substring(1, jsonObjects.getString(i).indexOf(":")).replace("\"", "");
                        if(!monthsName.contains(monthTableName)) {
                            monthsName.add(monthTableName);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, monthsName);

                    month_selector.setAdapter(adapter);
                    int position = adapter.getPosition(monthsName.get(monthsName.size()-1));
                    month_selector.setSelection(position);

                    month_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Object name = parent.getItemAtPosition(position);
                            MainActivity.monthDataSetup(new Callback() {
                                @Override
                                public void StringData(String value) throws JSONException {

                                }

                                @Override
                                public void JsonData(JSONArray jsonObjects) throws JSONException {
                                    boolean month = false;
                                    if(name == "all month"){
                                        month = true;
                                    }
                                    try {
                                        setData(month, (String) name, jsonObjects);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void setData(boolean month, String monthName, JSONArray jsonObjects) throws JSONException, ParseException {
        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<String> monthsLabel = new ArrayList<>();
        final boolean[] done = {false};
        String currentTable = monthName;
        for (int i = 0; i < jsonObjects.length(); i++) {
            final int[] combineYaxis = {0};
            String monthTableName = jsonObjects.getString(i).substring(1, jsonObjects.getString(i).indexOf(":")).replace("\"", "");

            if(!monthTableName.equals(currentTable) && !month){
                continue;
            }
            if(done[0]){
                break;
            }
            int finalI = i;
            getTableData(monthTableName, new Callback() {
                @Override
                public void StringData(String value) throws JSONException {
                }
                @Override
                public void JsonData(JSONArray daysData) throws JSONException {
                    for (int j = 0; j < daysData.length(); j++) {
                        int exp = daysData.getJSONObject(j).getInt("exp");
                        if(month) {
                            combineYaxis[0] += exp;
                        }
                        else {
                            values.add(new BarEntry(j, exp));
                        }
                    }
                    if(month) {
                        values.add(new BarEntry(finalI, combineYaxis[0]));
                        monthsLabel.add(monthTableName);
                    }
                    else{
                        done[0] = true;
                    }
                    if(jsonObjects.length() == values.size() || !month){
                        BarDataSet barDataSet = new BarDataSet(values, "Dummy Data");
                        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        barDataSet.setValueTextColor(Color.BLACK);
                        barDataSet.setValueTextSize(16f);
                        BarData barData = new BarData(barDataSet);

                        XAxis xAxis = barChart.getXAxis();

                        xAxis.setValueFormatter(barChart.getDefaultValueFormatter());

                        if(month) {
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            ValueFormatter formatter = new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    if(value < monthsLabel.size()){
                                        return monthsLabel.get((int) value);
                                    }
                                    else{
                                        return null;
                                    }
                                }
                            };
                            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
                            xAxis.setValueFormatter(formatter);
                        }

                        barChart.getXAxis().setDrawGridLines(false);
                        barChart.getDescription().setEnabled(false);
                        barChart.getLegend().setEnabled(false);
                        barChart.setData(barData);
                        barChart.animateY(500);
                    }

                }
            });

        }

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}

