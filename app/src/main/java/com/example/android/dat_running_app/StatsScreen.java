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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.vision.text.Line;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.R.attr.colorPrimary;
import static android.R.attr.colorPrimaryDark;
import static android.R.attr.data;
import static android.R.attr.format;
import static android.R.attr.textColorPrimary;
import static android.R.attr.x;
import static android.R.attr.y;
import static android.R.id.primary;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
//import static com.example.android.dat_running_app.R.id.lineChartButton;
import static com.example.android.dat_running_app.R.id.startChart;
//import static com.example.android.dat_running_app.R.id.timeIntervalButton;
import static com.google.android.gms.analytics.internal.zzy.B;
import static com.google.android.gms.analytics.internal.zzy.d;
import static com.google.android.gms.analytics.internal.zzy.g;
import static com.google.android.gms.analytics.internal.zzy.i;
import static com.google.android.gms.analytics.internal.zzy.l;
import static com.google.android.gms.analytics.internal.zzy.p;
import static com.google.android.gms.analytics.internal.zzy.r;
import static com.google.android.gms.analytics.internal.zzy.s;
import static com.google.android.gms.analytics.internal.zzy.u;
import static com.google.android.gms.analytics.internal.zzy.v;
import static com.google.android.gms.analytics.internal.zzy.w;

/**
 * Created by Ben on 8/11/2016.
 */

public class StatsScreen extends AppCompatActivity{
    private RunDBHelper RDB;
    private Spinner xAxisSpinner;
    private Spinner yAxisSpinner;
    private Spinner runSpinner;
    LinearLayout runSpinnerLayout;
    private LineChart chart;
    private ArrayList<Long> startTimes;//stores the epoch time of the run start times
    private ArrayList<String> startTimesDateHelper;//stores the human date corresponding to the epoch start times
    private double distanceTravelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_screen);
        Toolbar statsToolbar = (Toolbar) findViewById(R.id.statsToolbar);
        setSupportActionBar(statsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        populateSpinnersIndividualRun();
        addRunSpinner();


        chart = (LineChart) findViewById(startChart);


        Button rb = (Button)findViewById(R.id.drawChartButton);
        drawChart(rb);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

       try{
           onCreate(new Bundle());
       }catch(IllegalStateException e){}
    }





    private void populateSpinnersIndividualRun(){
        xAxisSpinner = (Spinner) findViewById(R.id.xAxisSpinner);
        yAxisSpinner = (Spinner) findViewById(R.id.yAxisSpinner);

        List<String> xAxisSpinnerList = new ArrayList<String>();
        List<String> yAxisSpinnerList = new ArrayList<String>();

        xAxisSpinnerList.add("Time ");//it is imperative to have a space after "Time" because of the way I determine if a selection is metric or not (which involves a split(" ").
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


        TextView textView = new TextView(this);
        textView.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));//txtColor
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


    private void populateRunSpinner(){
        List<String> runSpinnerList = new ArrayList<>();

        int i=0;

        startTimes = new ArrayList<>();
        startTimesDateHelper = new ArrayList<>();
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
       //                 Log.d("date", strDate);
                        String[] dateArray = strDate.split(" ");
                        String spinnerFormatDate = dateArray[0]+" "+dateArray[1]+" "+dateArray[2]+" "+dateArray[5]+" "+dateArray[3]+" "+cursor.getString(1);//dateArray[3] needs to be here to
                                                                                                                                                            // differentiate the start times.
                        startTimesDateHelper.add(spinnerFormatDate);
                        runSpinnerList.add(spinnerFormatDate);
                        i++;
                    }
                    else
                        increment+=1;

               //     Date date = new Date(Long.parseLong(startTime[1]));
               //     runSpinnerList.add(date.toString());
                }catch(CursorIndexOutOfBoundsException e){Log.d("EXCEPTION","CAUGHT"); i+=10;}
                catch(SQLiteCantOpenDatabaseException d){Log.d("EXCEPTION","CAUGHT CAN'T OPEN"); i+=10;};
            }

        RDB.close();


        ArrayAdapter<String> runSpinnerDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,runSpinnerList);
        runSpinner.setAdapter(runSpinnerDataAdapter);
    }

    public void drawChart(View view){
        double sumOfSpeed=0;
        double numUpdates=0;
        RDB = new RunDBHelper(this);

        List<Entry> entries = new ArrayList<Entry>();

        String xAxisSelection = xAxisSpinner.getSelectedItem().toString();
        String yAxisSelection = yAxisSpinner.getSelectedItem().toString();
        String runSelection;
        int index=-1;

        ////////////////////////this code segment finds which index of startTimes (and by extension which run) the user has selected.
        int tempIndex = 0;
        while (index == -1) {
            if(runSpinner.getSelectedItem()!=null){
                runSelection = runSpinner.getSelectedItem().toString();
                if (startTimesDateHelper.get(tempIndex).equals(runSelection)){
                    index = tempIndex;
                }
                else {
                    tempIndex++;
                    //                      Log.d("StatsScreen DrawChart", "Still going...");
                }
            }
        }

        ///////////////////////



        int xGetStringIndex=0;
        int yGetStringIndex=0;//these are the indices that database.getString() will use, i.e. db.getString(yGetStringIndex);




        //         Log.d("xAxisSelection log",""+xAxisSelection);
        switch(xAxisSelection){
            case "Run Type": xGetStringIndex=1;
                break;
            case "Time ": xGetStringIndex=3;
                break;
            case "Distance (km)": xGetStringIndex=4;
                break;
            case "Distance (mi)": xGetStringIndex=4;
                break;
            case "Pace (mi)": xGetStringIndex=5;
                break;
            case "Pace (km)": xGetStringIndex=5;
                break;
            case "Speed (mi/hr)": xGetStringIndex=6;
                break;
            case "Speed (km/hr)": xGetStringIndex=6;
                break;
            case "Cadence (strikes/min)": xGetStringIndex=7;
                break;
            case "Elevation Change (ft)": xGetStringIndex=8;
                break;
            case "Elevation Change (m)": xGetStringIndex=8;
                break;
            default: Log.d("xAxisSelection default",""+xAxisSelection);
        }

        switch(yAxisSelection){
            case "Run Type": yGetStringIndex=1;
                break;
            case "Time ": yGetStringIndex=3;
                break;
            case "Distance (km)": yGetStringIndex=4;
                break;
            case "Distance (mi)": yGetStringIndex=4;
                break;
            case "Pace (mi)": yGetStringIndex=5;
                break;
            case "Pace (km)": yGetStringIndex=5;
                break;
            case "Speed (mi/hr)": yGetStringIndex=6;
                break;
            case "Speed (km/hr)": yGetStringIndex=6;
                break;
            case "Cadence (strikes/min)": yGetStringIndex=7;
                break;
            case "Elevation Change (ft)": yGetStringIndex=8;
                break;
            case "Elevation Change (m)": yGetStringIndex=8;
                break;
            default: Log.d("yAxisSelection default",""+yAxisSelection);
        }

        Cursor cursor = RDB.getFRData();//need to cycle through entries until i find the one that starts at the right time.  Then cycle through those (adding the data to the chart) until i find a run
        //with a different start time.
        cursor.moveToFirst();

        ArrayList<Long> labelList = new ArrayList<>();
        ArrayList<String> splits = new ArrayList<>();
        int splitCounter=1;//increments after each time a multiple of 1km is reached. used in recording of mile splits.

        try{
            while(cursor.moveToNext()) {

                double currentDistance=0;



                String startTimeString = startTimes.get(index)+"";
                String[] startTimeArray = cursor.getString(2).split(" ");
                String[] xChoice = xAxisSelection.split(" ");
                String[] yChoice = yAxisSelection.split(" ");

                boolean xMetric=false;
                boolean yMetric=false;


                try {
                    if (xChoice[1].equals("(km)") || xChoice[1].equals("(m)") || xChoice[1].equals("(km/hr)"))
                        xMetric = true;
                    if (yChoice[1].equals("(km)") || yChoice[1].equals("(m)") || yChoice[1].equals("(km/hr)"))
                        yMetric = true;
                }catch(ArrayIndexOutOfBoundsException e){};

                //       boolean rightRun=false;

                if(startTimeArray[1].equals(startTimeString)){


                    String tempDist[] = cursor.getString(4).split(" ");
                    distanceTravelled=Double.parseDouble(tempDist[2]);

                    String partialSpeedStr[] = cursor.getString(6).split(" ");
                    sumOfSpeed += Double.parseDouble(partialSpeedStr[1]);
                    //             Log.d("PLS",""+Double.parseDouble(partialSpeedStr[1])+" "+sumOfSpeed+" "+numUpdates+1);
                    numUpdates+=1;

                    //       rightRun=true;
                    String[] xValueString = cursor.getString(xGetStringIndex).split(" ");
                    String[] yValueString = cursor.getString(yGetStringIndex).split(" ");
                    //                Log.d("x and y cursor String","["+cursor.getString(xGetStringIndex)+", "+cursor.getString(yGetStringIndex)+"]");
                    double xValue = 0;
                    double yValue = 0;
                    String[] timeStr = cursor.getString(3).split(" ");






                    if(xMetric==true){
                        switch(xValueString[0]){
                            case "DISTANCE":
                                xValue=Double.parseDouble(xValueString[2])/1000;
                                break;
                            case "Pace:": xValue = Double.parseDouble(xValueString[1]);
                                break;
                            case "Speed:": xValue = Double.parseDouble(xValueString[1]);
                                break;
                            case "Time:": xValue = Double.parseDouble(xValueString[1])/1000;
                                //                  Log.d("IVE GOT THE NEED","FOR TIME... in metric");
                                break;
                            case "CADENCE:": xValue = 42;
                                break;
                            case "ELEVATION:": xValue = 42;
                                break;
                            default: xValue = -9001;
                        }
                    }else{
                        try {
                            //           for (int p = 0; p < xValueString.length; p++)
                            //               Log.d("ARRAY THINGY X", "" + xValueString[p]);
                            switch (xValueString[0]) {
                                case "DISTANCE":
                                    xValue = Double.parseDouble(xValueString[2]) * .000621371;
                                    break;
                                case "Pace:":
                                    xValue = Double.parseDouble(xValueString[1]) * 1.60932;
                                    break;
                                case "Speed:":
                                    xValue = Double.parseDouble(xValueString[1]) * .621371;
                                    break;
                                case "Time:":
                                    xValue = Double.parseDouble(xValueString[1])/1000;
                                    //              Log.d("IVE GOT THE NEED","FOR TIME");
                                    break;
                                case "CADENCE:":
                                    xValue = 42;
                                    break;
                                case "ELEVATION:":
                                    xValue = 42;
                                    break;
                                default: //xValue = Double.parseDouble(xValueString[1])/1000;
                                    Log.d("DEFAULT X", "" + xValue + " " + xValueString[0] + " " + xValueString[1]);
                            }
                        }catch(ArrayIndexOutOfBoundsException e){};
                    }

                    if(yMetric==true){
                        switch(yValueString[0]){
                            case "DISTANCE":
                                yValue=Double.parseDouble(yValueString[2])/1000;
                                break;
                            case "Pace:": yValue = Double.parseDouble(yValueString[1]);
                                break;
                            case "Speed:": yValue = Double.parseDouble(yValueString[1]);
                                break;
                            case "Time:": yValue = Double.parseDouble(yValueString[1])/1000;
                                break;
                            case "CADENCE:": yValue = 42;
                                break;
                            case "ELEVATION:": yValue = 42;
                                break;
                            default: yValue = -9001;
                        }
                    }else{
                        for(int p = 0;p<yValueString.length;p++)
                            //                    Log.d("ARRAY THINGY Y",""+yValueString[p]);
                            switch(yValueString[0]){
                                case "DISTANCE":
                                    yValue=Double.parseDouble(yValueString[2])*.000621371;
                                    break;
                                case "Pace:": yValue = Double.parseDouble(yValueString[1])*1.60932;
                                    break;
                                case "Speed:": yValue = Double.parseDouble(yValueString[1])* .621371;
                                    break;
                                case "Time:": yValue = Double.parseDouble(yValueString[1])/1000;
                                    break;
                                case "CADENCE:": yValue = 42;
                                    break;
                                case "ELEVATION:": yValue = 42;
                                    break;
                                default:// yValue = -9001;
                                    Log.d("DEFAULT Y",""+yValue+" "+yValueString[0]+" "+yValueString[1]);
                            }
                    }

                    currentDistance+=Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000;
                    Log.d("MYDISTANCE",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000);


                    if(currentDistance >= splitCounter){
                        if(splits.size()>0){
                            long t1 = Long.parseLong(cursor.getString(3).split(" ")[1]);
                            long t2 = 0;
                            for(int x=0;x<splits.size();x++){
                                t2+=Long.parseLong(splits.get(x));
                            }
                            long t3 = t1-t2;
                            splits.add(""+t3);
                            Log.d("WTF",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000+" "+splitCounter);
                        }else{
                            splits.add(cursor.getString(3).split(" ")[1]);
                            Log.d("WTF",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000+" "+splitCounter);

                        }
                        splitCounter++;
                        //    currentDistance=0;
                    }


                    labelList.add((long)(xValue));
                    entries.add(new Entry((float) yValue, (int) xValue));
                }
            }
        }finally{
            cursor.close();
            updateTotalDistanceTV();
            double avgSpeed = sumOfSpeed/numUpdates;//i think that this is in meters per second
            //               Log.d("sumOfSpeed",""+sumOfSpeed);
            updateAvgSpeed(avgSpeed);
            updateAvgPaceTV(formatTime((int)(1/avgSpeed*1000*1000)+""));//time it takes to finish a km
            updateSplits(splits);//false means that it is an individual run
            splitCounter=1;
        }

        String[] labels = new String[labelList.size()];



        for(int x=0;x<labelList.size();x++){
            String temp = ""+labelList.get(x);
            labels[x]=formatTime(temp);
        }

        //           Log.d("DEBUG...",labelList.size()+" "+labels.length);





        LineDataSet dataSet = new LineDataSet(entries,"Label Place Holder");
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setValueTextColor(R.color.colorAccent);
        LineData lineData = new LineData(labels,dataSet);
        chart.setData(lineData);
        chart.setDescription("DEV BUILD");
        chart.getLegend().setEnabled(true);
        chart.animateXY(500,1000);
        chart.invalidate();
    }

    private void updateTotalDistanceTV(){
        TextView textView = (TextView) findViewById(R.id.totalDistanceTV);
        textView.setText(""+distanceTravelled+" km");

    }

    private void updateAvgSpeed(double s){
        TextView textView = (TextView) findViewById(R.id.avgSpeedNumTV);
        textView.setText(""+s + " m/s");
    }

    private void updateAvgPaceTV(String s){
        TextView textView = (TextView) findViewById(R.id.avgPaceNumTV);
        textView.setText(""+s);
    }

    private void updateSplits(ArrayList<String> s){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.splitsLL);
        linearLayout.removeAllViews();

        TextView splitsTV = (TextView)findViewById(R.id.distanceSplitsTV);

        if(s.size()==0)
            splitsTV.setVisibility(View.INVISIBLE);
        else
            splitsTV.setVisibility(View.VISIBLE);

        for(int x=0;x<s.size();x++){
            TextView tv = new TextView(this);
            tv.setText(formatTime(s.get(x)));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
            linearLayout.addView(tv);
        }
        s.clear();
    }

    private String formatTime(String s){
        String hmsm="";
        try {
            long millis = Long.parseLong(s);
            hmsm = String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                    TimeUnit.MILLISECONDS.toMillis(millis) % TimeUnit.SECONDS.toMillis(1));

            String delimedTime[] = hmsm.split(":");

            if(delimedTime[0].equals("00"))
                hmsm = delimedTime[1]+":"+delimedTime[2]+":"+delimedTime[3];

        }catch(NumberFormatException e){
            Log.d("EXCEPTION","Number format exception in formatTime() in StatsScreen"+s);
        }


        return ""+hmsm;
    }
}
