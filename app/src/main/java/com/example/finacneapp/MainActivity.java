package com.example.finacneapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    PieChart pieChart;
    LineChart lineChart;
    static serverConnector connector;
    Button spendButton;
    View addExp;
    AlertDialog.Builder dialogBuilder;
    public static String TAG = "mes";
    ArrayList<String> category_name;
    ArrayList<Integer> category_amt;
    AutoCompleteTextView autoCompleteTextView;
    ProgressBar progressBar, limitBar;
    TextView budgetText, currentState;
    Slider budgetPer;
    EditText budgetNum, addmoney;
    static String currentYear;


    boolean toggle = false;
    static String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spendButton = findViewById(R.id.spend);
        addmoney = findViewById(R.id.addIncome);
        pieChart = findViewById(R.id.piechart);
        progressBar = findViewById(R.id.progressBar);
        limitBar = findViewById(R.id.limitProgress);
        budgetText = findViewById(R.id.budget);
        currentState = findViewById(R.id.currentState);
        budgetPer = findViewById(R.id.budgetSelecter);
        budgetNum = findViewById(R.id.budgetNum);
        lineChart = findViewById(R.id.lineChart);

        addExp = getLayoutInflater().inflate(R.layout.add_exp, null);
        currentYear = new SimpleDateFormat("yyyy").format(new Date());

        dialogBuilder = new AlertDialog.Builder(this);
        connector = new serverConnector();
        connector.setup();

        setupPieChart();
        categoryDataSetup();

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        resetProgressBar();
                    }
                },
                100
        );
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    setuplineChart();
                }
            },
            200
        );


        progressBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getTableData(getTableName(months[new Date().getMonth()], currentYear), MainActivity.this, new Callback() {
                    @Override
                    public void StringData(String value) {

                    }
                    @Override
                    public void JsonData(JSONArray jsonObjects) {
                        progressBarSetup(toggle, jsonObjects);
                    }
                });

                if(!toggle){
                    currentState.setText("Month budget");
                    toggle = true;
                }
                else {
                    currentState.setText("Day budget");
                    toggle = false;
                }
                return false;
            }
        });

        budgetPer.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                budgetNum.setText(String.valueOf(value));
            }
        });


        budgetPer.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("budgetPer", slider.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                connector.sendData(obj);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                resetProgressBar();
                            }
                        },
                        200
                );
            }
        });

//

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        spendButton.setOnClickListener(v -> expDialog());

        addmoney.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    String text = String.valueOf(addmoney.getText());
                    if(!text.equals("")){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("income", text);
                            connector.sendData(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    addmoney.setText("");
                }
                return false;
            }
        });




    }

    private void setuplineChart(){
        getTableData(getTableName(months[new Date().getMonth()], currentYear), this, new Callback() {
            @Override
            public void StringData(String value) throws JSONException {

            }

            @Override
            public void JsonData(JSONArray jsonObjects) throws JSONException {
                ArrayList<Entry> values = new ArrayList<>();

                for (int i = 0; i < jsonObjects.length(); i++) {
                    values.add(new Entry(i, jsonObjects.getJSONObject(i).getInt("amt")));
                }
                LineDataSet lineDataSet = new LineDataSet(values, "Amount");
                lineDataSet.setLineWidth(2);
                lineDataSet.setValueTextColor(Color.WHITE);
                lineDataSet.setValueTextSize(10);

                LineData lineData = new LineData(lineDataSet);

                lineChart.getAxisRight().setTextColor(Color.WHITE);
                lineChart.getAxisLeft().setTextColor(Color.WHITE);
                lineChart.getDescription().setEnabled(false);
                lineChart.getXAxis().setEnabled(false);
                lineChart.getLegend().setEnabled(false);
                lineChart.animateY(10);
                lineChart.setData(lineData);
            }
        });
    }

    private void progressBarSetup(boolean month, JSONArray jsonObjects){
        int budgetAmt = 0;
        int expAmt = 0;
        int totalAmt = 0;
        int index = 0;
        if(!month){
            index = jsonObjects.length()-1;
        }
        for (int i = index; i < jsonObjects.length(); i++){
            try {
                budgetAmt += jsonObjects.getJSONObject(i).getInt("budget");
                expAmt += jsonObjects.getJSONObject(i).getInt("exp");
                totalAmt += jsonObjects.getJSONObject(i).getInt("amt");
                budgetPer.setValue(jsonObjects.getJSONObject(i).getInt("budgetPer"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            limitBar.setMin(budgetAmt);
        }
        limitBar.setMax(totalAmt);
        if(expAmt>budgetAmt){
            limitBar.setProgress(expAmt);
        }
        else {
            limitBar.setProgress(0);
        }

        progressBar.setMax(budgetAmt);
        progressBar.setProgress(expAmt);
        budgetText.setText("₹"+(budgetAmt-expAmt)+" left");
    }



    public static void getTableData(String tableName, Context context, Callback callback){
        String command = String.format("SELECT * FROM %s", tableName);

        connector.getData(command, context, new Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void StringData(String value) {
                try {
                    Log.i(TAG, "StringData: "+value);
                    JSONArray jsonArray = new JSONArray(value);
                    callback.JsonData(jsonArray);
                    callback.StringData(value);

                } catch (JSONException e) {
                    Log.e(TAG, "StringData: "+e.toString() );
                }

            }

            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
    }

    private void categoryDataSetup(){
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
                        loadPieData(category_amt);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
    }


    private void resetProgressBar(){
        getTableData(getTableName(months[new Date().getMonth()], currentYear), MainActivity.this, new Callback() {
            @Override
            public void StringData(String value) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void JsonData(JSONArray jsonObjects) {
                progressBarSetup(toggle, jsonObjects);
            }
        });
    }

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("category");
        pieChart.setCenterTextSize(23);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

    }

    private void expDialog() {
        if (addExp.getParent() != null) {
            ((ViewGroup) addExp.getParent()).removeView(addExp);
        }
        EditText expAmt = addExp.findViewById(R.id.Exp);

        autoCompleteTextView = addExp.findViewById(R.id.auto);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, category_name);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (autoCompleteTextView.getText().toString() != "" && expAmt.getText().toString() != "" && actionId == EditorInfo.IME_ACTION_SEND) {
                    String exp_amt = expAmt.getText().toString();
                    String category = autoCompleteTextView.getText().toString();
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("exp", exp_amt);
                        obj.put("cate", category);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    connector.sendData(obj);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    // your code here
                                    try {
                                        pieChart.clearValues();
                                        pieChart.clear();
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    new java.util.Timer().schedule(
                                            new TimerTask() {
                                                @Override
                                                public void run() {
                                                    categoryDataSetup();
                                                }
                                            }
                                    , 100);

                                    resetProgressBar();
                                }
                            },
                            500
                    );

                }
                expAmt.setText("");
                autoCompleteTextView.setText("");
                return false;
            }
        });
        dialogBuilder.setView(addExp);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }


    private void loadPieData(ArrayList<Integer> category_amt){
        ArrayList<PieEntry> pieEntryes = new ArrayList();
        for (int i = 0; i < category_amt.size(); i++) {
            pieEntryes.add(new PieEntry(category_amt.get(i), category_name.get(i)));
        }

        ArrayList<Integer> colors = new ArrayList();
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }


        PieDataSet pieDataSet = new PieDataSet(pieEntryes, "category");
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setColors(colors);

        PieData data = new PieData(pieDataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.invalidate();

//        pieChart.animateX(1200, Easing.EasingOption.EaseOutQuad);

    }

    public static String getTableName(String month,String year){
        return String.format("month_%s_%s", month, year);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String title = (String) item.getTitle();
        switch (title){
            case "Data":
                Intent intent = new Intent(this, DataReview.class);
                startActivity(intent);
                break;

        }
        return false;
    }
}
