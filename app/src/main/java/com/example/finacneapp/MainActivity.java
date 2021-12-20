package com.example.finacneapp;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    PieChart pieChart;
    serverConnector connector;
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
    boolean toggle = true;
    static String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spendButton = findViewById(R.id.spend);
        pieChart = findViewById(R.id.piechart);
        progressBar = findViewById(R.id.progressBar);
        limitBar = findViewById(R.id.limitProgress);
        budgetText = findViewById(R.id.budget);
        currentState = findViewById(R.id.currentState);
        budgetPer = findViewById(R.id.budgetSelecter);

        addExp = getLayoutInflater().inflate(R.layout.add_exp, null);

        dialogBuilder = new AlertDialog.Builder(this);
        connector = new serverConnector();
        connector.setup();
        categoryDataSetup(true);

        DataSetup(false);

        getSupportActionBar().setTitle("");

        progressBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DataSetup(toggle);
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


        spendButton.setOnClickListener(v -> expDialog());
    }

    private void DataSetup(boolean month){
        connector.getData(Encryption_Decryption.encrypt(String.format("SELECT * FROM %s", getTableName())), new Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSucess(String value) {
                try {
                    int budgetAmt = 0;
                    int expAmt = 0;
                    int totalAmt = 0;
                    JSONArray jsonArray = new JSONArray(value);
                    int index = 0;
                    if(!month){
                        index = jsonArray.length()-1;
                    }
                    for(int i = index; i<jsonArray.length(); i++){
                        budgetAmt += jsonArray.getJSONObject(i).getInt("budget");
                        expAmt += jsonArray.getJSONObject(i).getInt("exp");
                        totalAmt += jsonArray.getJSONObject(i).getInt("amt");
                        budgetPer.setValue(jsonArray.getJSONObject(i).getInt("budgetPer"));
                    }
                    limitBar.setMin(budgetAmt);
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void categoryDataSetup(boolean toggle){
        connector.getData(Encryption_Decryption.encrypt("SELECT * FROM exp_category"), new Callback() {
            @Override
            public void onSucess(String value) {
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
                            setupPieChart();
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
        });
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

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("category");
        pieChart.setCenterTextSize(23);
        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setEnabled(false);

    }

    private void getPieData(String command, int index){
        if(index == 0){
            category_amt = new ArrayList<>();
        }
        command = Encryption_Decryption.encrypt(command);
        connector.getData(command, new Callback() {
            @Override
            public void onSucess(String value) {
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

        pieChart.animateX(1200, Easing.EasingOption.EaseOutQuad);

    }

    public static String getTableName(){
        return String.format("month_%s_%s", months[new Date().getMonth()], new SimpleDateFormat("yyyy").format(new Date()));
    }
}
