package com.example.finacneapp;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    PieChart pieChart;
    View limiter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("");
        pieChart = findViewById(R.id.piechart);
        limiter = findViewById(R.id.limit);
        setupPieChart();
        loadPieData();
        ProgressBar simpleProgressBar=findViewById(R.id.progressBar); // initiate the progress bar

//        limiter.setX(105);
        simpleProgressBar.setProgress(60);
        simpleProgressBar.setMax(100);
    }

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);

        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("category");
        pieChart.setCenterTextSize(23);
        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setEnabled(false);

    }
    private void loadPieData(){
        ArrayList<PieEntry> pieEntryes = new ArrayList();
        pieEntryes.add(new PieEntry(0.2f, "a"));
        pieEntryes.add(new PieEntry(0.15f, "b"));
        pieEntryes.add(new PieEntry(0.22f, "c"));
        pieEntryes.add(new PieEntry(0.10f, "d"));

        ArrayList<Integer> colors = new ArrayList();
        for(int color: ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }
        for(int color: ColorTemplate.VORDIPLOM_COLORS){
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
}
