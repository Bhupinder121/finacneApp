package com.example.finacneapp;

import android.content.Intent;
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


    boolean toggle = true;
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
        categoryDataSetup(true);


        getTableData(getTableName(months[new Date().getMonth()], currentYear), new Callback() {
            @Override
            public void StringData(String value) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void JsonData(JSONArray jsonObjects) {
                progressBarSetup(true, jsonObjects);
            }
        });

//        getSupportActionBar().setTitle("");

        progressBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getTableData(getTableName(months[new Date().getMonth()], currentYear), new Callback() {
                    @Override
                    public void StringData(String value) {

                    }

                    @Override
                    public void JsonData(JSONArray jsonObjects) {
                        progressBarSetup(toggle, jsonObjects);
                    }
                });

                if(toggle){
                    currentState.setText("Month budget");
                    toggle = false;
                }
                else {
                    currentState.setText("Day budget");
                    toggle = true;
                }
                return false;
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
            }
        });

        budgetPer.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                budgetNum.setText(String.valueOf(value));
            }
        });

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

        setuplineChart();

    }

    private void setuplineChart(){
        getTableData(getTableName(months[new Date().getMonth()], currentYear), new Callback() {
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
                lineDataSet.setLineWidth(3);


                LineData lineData = new LineData(lineDataSet);
                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.animateY(10);
                lineChart.setData(lineData);
            }
        });
    }


    private void progressBarSetup(boolean toggle, JSONArray jsonObjects){
        int budgetAmt = 0;
        int expAmt = 0;
        int totalAmt = 0;
        int index = 0;
        if(toggle){
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
            Log.i(TAG, "onSucess: "+limitBar.getProgress());
        }
        else {
            limitBar.setProgress(0);
        }

        progressBar.setMax(budgetAmt);
        progressBar.setProgress(expAmt);
        budgetText.setText("₹"+(budgetAmt-expAmt)+" left");
    }

    public static void monthDataSetup(Callback callback){
        String command = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'month_%'";
        connector.getData(command, new Callback() {
            @Override
            public void StringData(String value) throws JSONException {
                JSONArray jsonArray = new JSONArray(value);
                JSONArray allMonths = new JSONArray();

                for(int i = 0; i < jsonArray.length(); i++){
                    String tableName = jsonArray.getJSONObject(i).getString("TABLE_NAME");
                    getTableData(tableName, new Callback() {
                        @Override
                        public void StringData(String value) throws JSONException {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(tableName, value);
                            allMonths.put(jsonObject);
                            if(allMonths.length() == jsonArray.length()){
                                callback.JsonData(allMonths);
                            }
                        }

                        @Override
                        public void JsonData(JSONArray jsonObjects) {

                        }
                    });
                }

//                callback.JsonData(a);
                
            }

            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
    }

    public static void getTableData(String tableName, Callback callback){
        connector.getData(String.format("SELECT * FROM %s", tableName), new Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void StringData(String value) {
                try {
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

    private void categoryDataSetup(boolean toggle){
        connector.getData("SELECT * FROM exp_category", new Callback() {
            @Override
            public void StringData(String value) {
                try {
                    category_name = new ArrayList();
                    JSONArray jsonArray = new JSONArray(value);
                    for (int i = 0; i < jsonArray.length(); i++){
                        if(!category_name.contains(jsonArray.getJSONObject(i).getString("ExpCate"))) {
                            category_name.add(jsonArray.getJSONObject(i).getString("ExpCate"));
                        }
                    }
                    if(toggle) {
                        if(category_name.size()>0) {
                            String command = String.format("SELECT ExpAmt FROM exp_category WHERE ExpCate = '%s'", category_name.get(0));
                            getPieData(command, 0);
                        }
                    }
                    else {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, category_name);
                        autoCompleteTextView.setAdapter(adapter);
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

        categoryDataSetup(false);

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
                                    pieChart.clearValues();
                                    pieChart.clear();
                                    categoryDataSetup(true);
                                    getTableData(getTableName(months[new Date().getMonth()], new SimpleDateFormat("yyyy").format(new Date())),
                                            new Callback() {
                                        @Override
                                        public void StringData(String value) {

                                        }

                                        @Override
                                        public void JsonData(JSONArray jsonObjects) {
                                            progressBarSetup(toggle, jsonObjects);
                                        }
                                    });
                                }
                            },
                            1000
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


    private void getPieData(String command, int index){
        if(index == 0){
            category_amt = new ArrayList<>();
        }
        connector.getData(command, new Callback() {
            @Override
            public void StringData(String value) {
                try {
                    JSONArray jsonObject = new JSONArray(value);
                    int amount = 0;
                    for (int i = 0; i < jsonObject.length(); i++){
                        amount += jsonObject.getJSONObject(i).getInt("ExpAmt");
                    }
                    category_amt.add(amount);
                    if(index+1 < category_name.size()) {
                        String command = String.format("SELECT ExpAmt FROM exp_category WHERE ExpCate = '%s'", category_name.get(index+1));
                        getPieData(command, index+1);
                    }
                    if(category_amt.size()==category_name.size()) {
                        loadPieData(category_amt);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void JsonData(JSONArray jsonObjects) {

            }
        });
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
        pieDataSet.setColors(colors);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
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
