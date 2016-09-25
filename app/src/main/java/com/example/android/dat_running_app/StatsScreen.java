package com.example.android.dat_running_app;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import static com.example.android.dat_running_app.R.id.lineChartButton;
import static com.example.android.dat_running_app.R.id.startChart;
import static com.example.android.dat_running_app.R.id.totalTimeDispTV;
import static com.google.android.gms.analytics.internal.zzy.d;
import static com.google.android.gms.analytics.internal.zzy.m;
import static com.google.android.gms.analytics.internal.zzy.p;
import static com.google.android.gms.analytics.internal.zzy.s;
//import static com.example.android.dat_running_app.R.id.timeIntervalButton;


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


    public void refreshSpinners(View view){

        drawChart(view);
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
   //     xAxisSpinnerList.add("Elevation Change (ft)");
   //     xAxisSpinnerList.add("Elevation Change (m)");

        //need to add spinner with a list of all runs in last-to-first order.

        yAxisSpinnerList.add("Distance (mi)");
        yAxisSpinnerList.add("Distance (km)");
        yAxisSpinnerList.add("Pace (mi)");
        yAxisSpinnerList.add("Pace (km)");
        yAxisSpinnerList.add("Speed (mi/hr)");
        yAxisSpinnerList.add("Speed (km/hr)");
        yAxisSpinnerList.add("Cadence (strikes/min)");
   //     yAxisSpinnerList.add("Elevation Change (ft)");
   //     yAxisSpinnerList.add("Elevation Change (m)");

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
        ArrayList<String> splitsKm = new ArrayList<>();
        ArrayList<String> splitsMi = new ArrayList<>();
        int splitCounterKm =1;//increments after each time a multiple of 1km is reached. used in recording of km splits.
        int splitCounterMi=1;//increments after each time a multiple of 1km is reached. used in recording of mi splits.
        long totalTime=0;

        try{
            while(cursor.moveToNext()) {

                double currentDistance=0;



                String startTimeString = startTimes.get(index)+"";
                String[] startTimeArray = cursor.getString(2).split(" ");
                String[] xChoice = xAxisSelection.split(" ");
                String[] yChoice = yAxisSelection.split(" ");

                boolean xMetric=false;
                boolean yMetric=false;

                for(int x = 0;x<yChoice.length;x++){
                    Log.d("Metric",yChoice[x]);
                }

                try {
                    if (xChoice[1].equals("(km)") || xChoice[1].equals("(m)") || xChoice[1].equals("(km/hr)"))
                        xMetric = true;
                }catch(ArrayIndexOutOfBoundsException e){Log.d("Metric","CaughtX");};

                try {
                    if (yChoice[1].equals("(km)") || yChoice[1].equals("(m)") || yChoice[1].equals("(km/hr)"))
                        yMetric = true;
                }
                catch (ArrayIndexOutOfBoundsException e){Log.d("Metric","CaughtY");};

                //       boolean rightRun=false;

                Log.d("Metric",""+xMetric+" "+yMetric);

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
                            case "Cadence:": xValue = Double.parseDouble(xValueString[1]);
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
                                case "Cadence:":
                                    xValue = Double.parseDouble(xValueString[1]);;
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
                            case "Cadence:": yValue = Double.parseDouble(yValueString[1]);;
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
                                case "Cadence:": yValue = Double.parseDouble(yValueString[1]);;
                                    break;
                                case "ELEVATION:": yValue = 42;
                                    break;
                                default:// yValue = -9001;
                                    Log.d("DEFAULT Y",""+yValue+" "+yValueString[0]+" "+yValueString[1]);
                            }
                    }

                    currentDistance+=Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000;
                    Log.d("MYDISTANCE",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000);


                    if(currentDistance >= splitCounterKm){//this finds the splits in km.  I need another split section for miles because I can't just convert these values.
                        if(splitsKm.size()>0){
                            long t1 = Long.parseLong(cursor.getString(3).split(" ")[1]);
                            long t2 = 0;
                            for(int x = 0; x< splitsKm.size(); x++){
                                t2+=Long.parseLong(splitsKm.get(x));
                            }
                            long t3 = t1-t2;
                            splitsKm.add(""+t3);
                 //           Log.d("WTF",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000+" "+ splitCounterKm);
                        }else{
                            splitsKm.add(cursor.getString(3).split(" ")[1]);
                  //          Log.d("WTF",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000+" "+ splitCounterKm);

                        }
                        splitCounterKm++;
                        //    currentDistance=0;
                    }

                    if(currentDistance/1.609 >= splitCounterMi){//this finds the splits in mi.  I need another split section for miles because I can't just convert these values.
                        if(splitsMi.size()>0){
                            long t1 = Long.parseLong(cursor.getString(3).split(" ")[1]);
                            long t2 = 0;
                            for(int x = 0; x< splitsMi.size(); x++){
                                t2+=Long.parseLong(splitsMi.get(x));
                            }
                            long t3 = t1-t2;
                            splitsMi.add(""+t3);
                    //        Log.d("WTF",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000+" "+ splitCounterMi);
                        }else{
                            splitsMi.add(cursor.getString(3).split(" ")[1]);
                   //         Log.d("WTF",""+Double.parseDouble(cursor.getString(4).split(":")[1].trim())/1000+" "+ splitCounterMi);

                        }
                        splitCounterMi++;
                        //    currentDistance=0;
                    }


                    labelList.add((long)(xValue));
                    entries.add(new Entry((float) yValue, (int) xValue));
                    totalTime=Long.parseLong(cursor.getString(3).split(" ")[1]);
                    Log.d("totalTime",""+cursor.getString(3));
                }
            }
        }finally{
            cursor.close();
            updateTotalDistanceTV();
            updateTotalTimeTV(totalTime+"");
            double avgSpeed = sumOfSpeed/numUpdates;//in km/hr
                           Log.d("sumOfSpeed",""+sumOfSpeed+" "+numUpdates);
            updateAvgSpeed(avgSpeed);
            double avgPaceKm = 1/avgSpeed*3600*1000;
            double avgPaceMi = avgPaceKm*1.609;
            updateCalsBurned(avgSpeed,totalTime);

      //      Log.d("avgPace",(1/avgSpeed)*3600*Math.pow(10,9)+" "+formatTime((long)((1/avgSpeed)*3600*Math.pow(10,9)/1000000)+""));


            updateAvgPaceTV(formatTime((long)avgPaceKm+""),formatTime((long)avgPaceMi+""));//time it takes to finish a km


            updateSplits(splitsKm, splitsMi);//false means that it is an individual run
            splitCounterKm =1;
            splitCounterMi=1;
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

    private void updateTotalTimeTV(String s){
        TextView textView = (TextView)findViewById(totalTimeDispTV);
        textView.setText(formatTime(s));
    }

    private void updateCalsBurned(double kph,long totalTime){
        TextView textView = (TextView)findViewById(R.id.caloriesBurnedTV);

        double mph = kph * .6214;

        double mets = 0;

        if(mph<5)
            mets = 6;
        else if(mph<5.2)
            mets = 8.3;
        else if(mph<6)
            mets = 9;
        else if(mph<6.7)
            mets = 9.8;
        else if(mph<7)
            mets=10.5;
        else if(mph<7.5)
            mets=11;
        else if(mph<8.6)
            mets=11.8;
        else if(mph<9)
            mets=12.3;
        else if(mph<10)
            mets=12.8;
        else if(mph<11)
            mets=14.5;
        else if(mph<12)
            mets=15;
        else if(mph<13)
            mets=19;
        else if(mph<14)
            mets=19.8;
        else
            mets=23;



        MainSettingsDBHelper msdb = new MainSettingsDBHelper(this);
        if(msdb.hasMass()){
            double mass = Double.parseDouble(msdb.getMass());
            msdb.close();

            double hours = totalTime * 2.778 * Math.pow(10,-7);

            int calsBurned = (int)Math.round(mets * mass * hours);

            Log.d("cals",mets+" "+mass+" "+hours);

            textView.setText(" "+calsBurned);
        }else{
            textView.setText(" Please enter your mass or weight in the settings menu");
        }

    }

    private void updateTotalDistanceTV(){
        TextView textView = (TextView) findViewById(R.id.totalDistanceTV);
        RadioButton metricRB = (RadioButton)findViewById(R.id.metricRadioButton);
        double tempDistanceTravelled = distanceTravelled/1000;
        double miles = .621371 * tempDistanceTravelled;


        if(metricRB.isChecked()){
            textView.setText(""+tempDistanceTravelled+" km");
        }else{
            textView.setText(""+miles+ " mi");
        }



    }

    private void updateAvgSpeed(double s){//s is in km/hr
        TextView textView = (TextView) findViewById(R.id.avgSpeedNumTV);
        RadioButton metricRB = (RadioButton)findViewById(R.id.metricRadioButton);
        if(metricRB.isChecked()){
            textView.setText(""+s + " km/hr");
        }else{
            textView.setText(""+(.6214 * s)+" mi/hr");
        }
        double mph = .6214 * s;
    }

    private void updateAvgPaceTV(String km, String mi){
        TextView textView = (TextView) findViewById(R.id.avgPaceNumTV);
        RadioButton metricRB = (RadioButton)findViewById(R.id.metricRadioButton);
        if(metricRB.isChecked())
            textView.setText(""+km+" per km");
        else
            textView.setText(""+mi+" per mi");
    }

    private void updateSplits(ArrayList<String> km, ArrayList<String> mi){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.splitsLL);
        linearLayout.removeAllViews();
        RadioButton metricRB = (RadioButton)findViewById(R.id.metricRadioButton);
        TextView splitsTV = (TextView)findViewById(R.id.distanceSplitsTV);

        if(metricRB.isChecked()){
            if(km.size()==0)
                splitsTV.setVisibility(View.INVISIBLE);
            else
                splitsTV.setVisibility(View.VISIBLE);

            for(int x=0;x<km.size();x++){
                TextView tv = new TextView(this);
                tv.setText("km "+(x+1)+": "+formatTime(km.get(x)));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
                linearLayout.addView(tv);
            }
            km.clear();
        }
        else{
            if(mi.size()==0)
                splitsTV.setVisibility(View.INVISIBLE);
            else
                splitsTV.setVisibility(View.VISIBLE);

            for(int x=0;x<mi.size();x++){
                TextView tv = new TextView(this);
                tv.setText("mile "+(x+1)+": "+formatTime(mi.get(x)));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
                linearLayout.addView(tv);
            }
            mi.clear();
        }


    }

    private String formatTime(String s){//converts milliseconds to hmsmilli format.
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
