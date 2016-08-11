package com.example.android.dat_running_app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.colorPrimary;
import static android.R.attr.data;

/**
 * Created by Ben on 8/11/2016.
 */

public class StatsScreen extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_screen);
        Toolbar statsToolbar = (Toolbar) findViewById(R.id.statsToolbar);
        setSupportActionBar(statsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LineChart startChart = (LineChart) findViewById(R.id.startChart);
        List<Entry> entries = new ArrayList<Entry>();
        String[] labels = {"0","1","2","3","4","5","6"};
        entries.add(new Entry(0,0));
        entries.add(new Entry(3,2));
        entries.add(new Entry(8,3));
        entries.add(new Entry(5,4));
        entries.add(new Entry(6,5));
        entries.add(new Entry(2,6));
        LineDataSet dataSet = new LineDataSet(entries,"Label Place Holder");
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setValueTextColor(R.color.colorAccent);
        LineData lineData = new LineData(labels,dataSet);
        startChart.setData(lineData);
        startChart.setDescription("");
        startChart.getLegend().setEnabled(false);
        startChart.animateXY(500,1000);
        startChart.invalidate();


    }
}
