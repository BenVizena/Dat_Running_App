package com.example.android.dat_running_app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.colorPrimary;
import static android.R.attr.data;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.google.android.gms.analytics.internal.zzy.B;
import static com.google.android.gms.analytics.internal.zzy.r;
import static com.google.android.gms.analytics.internal.zzy.v;

/**
 * Created by Ben on 8/11/2016.
 */

public class StatsScreen extends AppCompatActivity{
    private RunDBHelper RDB;
    Boolean lineChart;

    private RadioButton lineChartButton;
    private RadioButton timeIntervalButton;
    private Spinner xAxisSpinner;
    private Spinner yAxisSpinner;
    private Spinner runSpinner;
    LinearLayout runSpinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_screen);
        Toolbar statsToolbar = (Toolbar) findViewById(R.id.statsToolbar);
        setSupportActionBar(statsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        timeIntervalButton=(RadioButton)findViewById(R.id.timeIntervalButton);

        if(timeIntervalButton.isChecked()){
            populateSpinnersTimeInterval();
            deleteRunSpinner();
        }
        else{
            populateSpinnersIndividualRun();
            addRunSpinner();
            //               Log.d("DEBUG","INDIVIDUAL RUN SHOULD BE CHECKED");
        }



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

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

       try{
           onCreate(new Bundle());
       }catch(IllegalStateException e){}
    }

    public void refreshSpinners(View view){
        lineChartButton = (RadioButton) findViewById(R.id.lineChartButton);
        timeIntervalButton=(RadioButton)findViewById(R.id.timeIntervalButton);

        if(timeIntervalButton.isChecked()){
            populateSpinnersTimeInterval();
            deleteRunSpinner();
        }
        else{
            populateSpinnersIndividualRun();
            addRunSpinner();
        }
    }

    private void populateSpinnersTimeInterval(){
        xAxisSpinner = (Spinner) findViewById(R.id.xAxisSpinner);
        yAxisSpinner = (Spinner) findViewById(R.id.yAxisSpinner);

        List<String> xAxisSpinnerList = new ArrayList<String>();
        List<String> yAxisSpinnerList = new ArrayList<String>();

        xAxisSpinnerList.add("Last Week");
        xAxisSpinnerList.add("Last Month");
        xAxisSpinnerList.add("Last Three Months");
        xAxisSpinnerList.add("Last Year");
        xAxisSpinnerList.add("All Time");

        yAxisSpinnerList.add("Pace (mi)");
        yAxisSpinnerList.add("Pace (km)");
        yAxisSpinnerList.add("Speed (mi/hr)");
        yAxisSpinnerList.add("Speed (km/hr)");
        yAxisSpinnerList.add("Distance (mi)");
        yAxisSpinnerList.add("Distance (km)");
        yAxisSpinnerList.add("Duration");
        yAxisSpinnerList.add("Calories Burned");

        ArrayAdapter<String> xDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, xAxisSpinnerList);
        xAxisSpinner.setAdapter(xDataAdapter);

        ArrayAdapter<String> yDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, yAxisSpinnerList);
        yAxisSpinner.setAdapter(yDataAdapter);
    }

    private void populateSpinnersIndividualRun(){
        xAxisSpinner = (Spinner) findViewById(R.id.xAxisSpinner);
        yAxisSpinner = (Spinner) findViewById(R.id.yAxisSpinner);

        List<String> xAxisSpinnerList = new ArrayList<String>();
        List<String> yAxisSpinnerList = new ArrayList<String>();

        xAxisSpinnerList.add("Time");
        xAxisSpinnerList.add("Distance (mi)");
        xAxisSpinnerList.add("Distance (km)");
        xAxisSpinnerList.add("Pace (mi)");
        xAxisSpinnerList.add("Pace (km)");
        xAxisSpinnerList.add("Speed (mi/hr)");
        xAxisSpinnerList.add("Speed (km/hr)");
        xAxisSpinnerList.add("Cadence (strikes/min)");
        xAxisSpinnerList.add("Elevation Change (ft)");
        xAxisSpinnerList.add("Elevation Change (m)");

        //need to add spinner with a list of all runs in last-to-first order.

        yAxisSpinnerList.add("Distance (mi)");
        yAxisSpinnerList.add("Distance (km)");
        yAxisSpinnerList.add("Pace (mi)");
        yAxisSpinnerList.add("Pace (km)");
        yAxisSpinnerList.add("Speed (mi/hr)");
        yAxisSpinnerList.add("Speed (km/hr)");
        yAxisSpinnerList.add("Cadence (strikes/min)");
        yAxisSpinnerList.add("Elevation Change (ft)");
        yAxisSpinnerList.add("Elevation Change (m)");

        ArrayAdapter<String> xDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, xAxisSpinnerList);
        xAxisSpinner.setAdapter(xDataAdapter);

        ArrayAdapter<String> yDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, yAxisSpinnerList);
        yAxisSpinner.setAdapter(yDataAdapter);

    }

    private void addRunSpinner(){




        RelativeLayout rl = (RelativeLayout)findViewById((R.id.chartAndSpinnerRelLayout));


        runSpinnerLayout = new LinearLayout(this);
        runSpinnerLayout.setOrientation(LinearLayout.HORIZONTAL);
        runSpinnerLayout.setId(View.generateViewId());
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
        runSpinnerLayout.setPadding((int)px,0,0,(int)px);

        ColorStateList txtColor = ((TextView)findViewById(R.id.xAxisText)).getTextColors();

        TextView textView = new TextView(this);
        textView.setTextColor(txtColor);
        textView.setText("focus:");

        runSpinnerLayout.addView(textView);


        runSpinner = new Spinner(this);
        runSpinner.setId(View.generateViewId());
        float widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 227, r.getDisplayMetrics());
        float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

        runSpinner.setLayoutParams(new Toolbar.LayoutParams((int)widthPx,(int)heightPx));

        //populate runSpinner
        populateRunSpinner();

        runSpinnerLayout.addView(runSpinner);


        rl.addView(runSpinnerLayout);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)runSpinnerLayout.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.yAxisLinearLayout);

        runSpinnerLayout.setLayoutParams(params);


    }

    private void deleteRunSpinner(){
        try {runSpinnerLayout.removeAllViews();}
        catch(NullPointerException e){}
    }


    private void populateRunSpinner(){
        List<String> runSpinnerList = new ArrayList<>();

        int i=0;

        ArrayList<Long> startTimes = new ArrayList<>();
        long increment=0;
        RDB = new RunDBHelper(this);

            while(i<10) {
                try {

                    Cursor cursor = RDB.getFRDataReverse(increment);
                    String[] startTime = cursor.getString(2).split(" ");

                    Long epochTime = Long.parseLong(startTime[1]);

                    if(!startTimes.contains(epochTime)&&epochTime>1471057640){
                        startTimes.add(epochTime);
             //           Log.d("ADDED",""+epochTime);
                        Date date = new Date(epochTime);
                        String strDate = date.toString();
                        String[] dateArray = strDate.split(" ");
                        String spinnerFormatDate = dateArray[0]+" "+dateArray[1]+" "+dateArray[2]+" "+dateArray[5]+" "+cursor.getString(1);
                        runSpinnerList.add(spinnerFormatDate);
                        i++;
                    }
                    else
                        increment+=1;

               //     Date date = new Date(Long.parseLong(startTime[1]));
               //     runSpinnerList.add(date.toString());
                }catch(CursorIndexOutOfBoundsException e){Log.d("EXCEPTION","CAUGHT"); i+=10;}
                catch(SQLiteCantOpenDatabaseException d){Log.d("EXCEPTION","CAUGHT"); i+=10;};
            }

        RDB.close();


        ArrayAdapter<String> runSpinnerDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,runSpinnerList);
        runSpinner.setAdapter(runSpinnerDataAdapter);
    }
}
