package com.example.finacneapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pieChart = findViewById(R.id.piechart);
//        encrypt_decrypt decrypt = new encrypt_decrypt();
//        decrypt.decrypt();
        setupPieChart();
        loadPieData();
    }

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("category");
        pieChart.setCenterTextSize(23);
        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setEnabled(false);

//        Legend l = pieChart.getLegend();
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
//        l.setOrientation(Legend.LegendOrientation.VERTICAL);
//        l.setDrawInside(false);
//        l.setEnabled(true);

    }
    private void loadPieData(){
        ArrayList<PieEntry> pieEntryes = new ArrayList();
        pieEntryes.add(new PieEntry(0.2f, "a"));
        pieEntryes.add(new PieEntry(0.15f, "c"));
        pieEntryes.add(new PieEntry(0.22f, "b"));
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

        PieData data = new PieData(pieDataSet);
        data.setDrawValues(false);
//        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateX(1200, Easing.EasingOption.EaseOutQuad);
    }
}