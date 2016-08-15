package com.example.android.dat_running_app;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

import static android.R.attr.x;
import static com.google.android.gms.fitness.data.Application.My;

/**
 * Created by Ben on 8/14/2016.
 */

public class MyValueFormatter implements ValueFormatter {

    private DecimalFormat decimalFormat;

    public MyValueFormatter(){
        decimalFormat = new DecimalFormat("###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {




        return null;
    }
}
