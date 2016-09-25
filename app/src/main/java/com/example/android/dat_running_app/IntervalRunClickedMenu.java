package com.example.android.dat_running_app;

import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;

import java.util.ArrayList;

import static android.R.attr.id;
import static com.google.android.gms.analytics.internal.zzy.i;

/**
 * Created by Ben on 8/19/2016.
 */

public class IntervalRunClickedMenu extends AppCompatActivity{
    ImageButton intervalRunNow_ib;
    ImageButton changeIRUI_ib;
    IrDBHelper irDB;
    Button addIntervalButton;
    Button confirmButton;
    int numIntervals=0;
    ArrayList<String> intervalArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_clicked_menu);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));//was .8
        // getWindow().setLayout(270,270);

        addMainButtons();
    }

    public void irNowClicked(View view){
        Intent intent = new Intent(this,FreeRunningScreen.class);
        startActivity(intent);
        finish();
        //  Toast.makeText(FreeRunClickedMenu.this, "ChangeUI Button was clicked!", Toast.LENGTH_SHORT).show();
    }

    public void irChangeUIClicked(View view){
        Intent intent = new Intent(this,IRChangeUI.class);
        startActivity(intent);
        finish();
    }

    private String getIntervalText(){
        EditText editTextHR = (EditText)findViewById(R.id.rfTimeEditTextHR);
        EditText editTextMIN = (EditText)findViewById(R.id.rfTimeEditTextMIN);
        Log.d("EDIT TEXT",editTextHR.getText()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d("EDIT TEXT",editTextMIN.getText()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        int hr;
        int min;
        if(editTextHR.getText().toString().length()==0)
            hr=0;
        else
            hr = Integer.parseInt(editTextHR.getText().toString());
        if(editTextMIN.getText().toString().length()==0)
            min=0;
        else
            min= Integer.parseInt(editTextMIN.getText().toString());

        long timeMillis = hr*3600*1000 + min*60*1000;

        return ""+timeMillis;
    }

    private void addIntervalDD(){//DD stands for DistanceDistance
        LinearLayout ll = (LinearLayout)findViewById(R.id.addIntervalLL);
        numIntervals+=1;

        IrHalfDBHelper irHalfDBHelper = new IrHalfDBHelper(this);
        String[] confirmedData = irHalfDBHelper.getData().split(" ");

        EditText h1ET = (EditText)findViewById(R.id.irH1DISTANCEDISTANCE);
        EditText h2ET = (EditText)findViewById(R.id.irH2DISTANCEDISTANCE);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, r.getDisplayMetrics());

        TextView textView = new TextView(this);
        textView.setTextSize(px);
        String interval = "Interval "+ numIntervals+": "+h1ET.getText().toString()+confirmedData[2]+" THEN "+h2ET.getText().toString()+confirmedData[2];
        textView.setText(interval);
        intervalArrayList.add(interval);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(textView);

    }

    private void addIntervalDT(){//DT stands for DistanceTime
        LinearLayout ll = (LinearLayout)findViewById(R.id.addIntervalLL);
        numIntervals+=1;

        IrHalfDBHelper irHalfDBHelper = new IrHalfDBHelper(this);
        String[] confirmedData = irHalfDBHelper.getData().split(" ");

        EditText h1ET = (EditText)findViewById(R.id.irH1DISTANCETIME);
        EditText h2ETpt1 = (EditText)findViewById(R.id.irH2DISTANCETIMEPt1);
        EditText h2ETpt2 = (EditText)findViewById(R.id.irH2DISTANCETIMEPt2);
        EditText h2ETpt3 = (EditText)findViewById(R.id.irH2DISTANCETIMEPt3);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, r.getDisplayMetrics());

        TextView textView = new TextView(this);
        textView.setTextSize(px);
        String interval = "Interval "+ numIntervals+": "+h1ET.getText().toString()+confirmedData[2]+" THEN "+h2ETpt1.getText().toString()+":"+h2ETpt2.getText().toString()+":"+h2ETpt3.getText().toString();
        intervalArrayList.add(interval);
        textView.setText(interval);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(textView);

    }

    private void addIntervalTT(){
        LinearLayout ll = (LinearLayout)findViewById(R.id.addIntervalLL);
        numIntervals+=1;

        IrHalfDBHelper irHalfDBHelper = new IrHalfDBHelper(this);
        String[] confirmedData = irHalfDBHelper.getData().split(" ");

        EditText h1ETpt1 = (EditText)findViewById(R.id.irH1TIMETIMEPt1);
        EditText h1ETpt2 = (EditText)findViewById(R.id.irH1TIMETIMEPt2);
        EditText h1ETpt3 = (EditText)findViewById(R.id.irH1TIMETIMEPt3);
        EditText h2ETpt1 = (EditText)findViewById(R.id.irH2TIMETIMEPt1);
        EditText h2ETpt2 = (EditText)findViewById(R.id.irH2TIMETIMEPt2);
        EditText h2ETpt3 = (EditText)findViewById(R.id.irH2TIMETIMEPt3);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, r.getDisplayMetrics());

        TextView textView = new TextView(this);
        textView.setTextSize(px);
        String interval = "Interval "+ numIntervals+": "+h1ETpt1.getText().toString()+":"+h1ETpt2.getText().toString()+":"+h1ETpt3.getText().toString()+" THEN "+h2ETpt1.getText().toString()+":"+h2ETpt2.getText().toString()+":"+h2ETpt3.getText().toString();
        textView.setText(interval);
        intervalArrayList.add(interval);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(textView);
    }

    private void addIntervalTD(){
        LinearLayout ll = (LinearLayout)findViewById(R.id.addIntervalLL);
        numIntervals+=1;

        IrHalfDBHelper irHalfDBHelper = new IrHalfDBHelper(this);
        String[] confirmedData = irHalfDBHelper.getData().split(" ");

        EditText h1ETpt1 = (EditText)findViewById(R.id.irH1TIMEDISTANCEPt1);
        EditText h1ETpt2 = (EditText)findViewById(R.id.irH1TIMEDISTANCEPt2);
        EditText h1ETpt3 = (EditText)findViewById(R.id.irH1TIMEDISTANCEPt3);
        EditText h2ET = (EditText)findViewById(R.id.irH2TIMEDISTANCE);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, r.getDisplayMetrics());

        TextView textView = new TextView(this);
        textView.setTextSize(px);
        String interval = "Interval "+ numIntervals+": "+h1ETpt1.getText().toString()+":"+h1ETpt2.getText().toString()+":"+h1ETpt3.getText().toString()+" THEN "+h2ET.getText().toString()+confirmedData[2];
        textView.setText(interval);
        intervalArrayList.add(interval);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(textView);
    }

    private void confirm(){
        Spinner h1Spinner = (Spinner)findViewById(R.id.irH1Spinner);
        Spinner h2Spinner = (Spinner)findViewById(R.id.irH2Spinner);
        Spinner unitSpinner=(Spinner)findViewById(R.id.irUnitSpinner);

        String h1Constraint = h1Spinner.getSelectedItem().toString();
        String h2Constraint = h2Spinner.getSelectedItem().toString();
        String unit = unitSpinner.getSelectedItem().toString();


        IrHalfDBHelper irHalfDBHelper = new IrHalfDBHelper(this);
        irHalfDBHelper.addSettings(h1Constraint,h2Constraint,unit);


        LinearLayout h1LL = (LinearLayout)findViewById(R.id.half1ConstraintLL);
        h1LL.setVisibility(View.GONE);

        LinearLayout h2LL = (LinearLayout)findViewById(R.id.half2ConstraintLL);
        h2LL.setVisibility(View.GONE);

        LinearLayout unitSpin = (LinearLayout)findViewById(R.id.irUnitSpinnerLL);
        unitSpin.setVisibility(View.GONE);

        Button confirm = (Button)findViewById(R.id.irConfirmedButton);
        confirm.setVisibility(View.GONE);

        IntervalDBHelper imdb = new IntervalDBHelper(this);
        imdb.clearTable();

        TextView explain = (TextView)findViewById(R.id.timeExplanation);


        Button add = (Button)findViewById(R.id.addIntervalButton);
        add.setVisibility(View.VISIBLE);

        LinearLayout irClickedMenuDivider = (LinearLayout)findViewById(R.id.irclickedMenuDividerLL);
        irClickedMenuDivider.setVisibility(View.VISIBLE);

        if(h1Constraint.equals("Distance")&& h2Constraint.equals("Distance")){
            LinearLayout dataEntryDD = (LinearLayout)findViewById(R.id.dataEntryLLDISTANCEDISTANCE);
            dataEntryDD.setVisibility(View.VISIBLE);

            explain.setVisibility(View.INVISIBLE);
            explain.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        if(h1Constraint.equals("Distance")&& h2Constraint.equals("Time")){
            LinearLayout dataEntryDT = (LinearLayout)findViewById(R.id.dataEntryLLDISTANCETIME);
            dataEntryDT.setVisibility(View.VISIBLE);

            explain.setVisibility(View.VISIBLE);
            explain.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        if(h1Constraint.equals("Time")&& h2Constraint.equals("Distance")){
            LinearLayout dataEntryTD = (LinearLayout)findViewById(R.id.dataEntryLLTIMEDISTANCE);
            dataEntryTD.setVisibility(View.VISIBLE);

            explain.setVisibility(View.VISIBLE);
            explain.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        if(h1Constraint.equals("Time")&& h2Constraint.equals("Time")){
            LinearLayout dataEntryTT = (LinearLayout)findViewById(R.id.dataEntryLLTIMETIME);
            dataEntryTT.setVisibility(View.VISIBLE);

            explain.setVisibility(View.VISIBLE);
            explain.setGravity(Gravity.CENTER_HORIZONTAL);
        }


    }

    private boolean addIntervalsToDB(){
        if(intervalArrayList.size()==0)
            return false;
        else{
            IntervalDBHelper imdb = new IntervalDBHelper(this);
            for(int x=0;x<intervalArrayList.size();x++)
                imdb.addSettings(intervalArrayList.get(x));
            return true;
        }
    }

    public void addMainButtons(){

        irDB = new IrDBHelper(this);
        intervalRunNow_ib=(ImageButton) findViewById(R.id.intervalRunNow_ib);
        changeIRUI_ib=(ImageButton)findViewById(R.id.changeIRUI_ib);
        addIntervalButton=(Button)findViewById(R.id.addIntervalButton);
        confirmButton=(Button)findViewById(R.id.irConfirmedButton);


        intervalRunNow_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
//                String time = getIntervalText();
                boolean success = addIntervalsToDB();
                if(success==false)
                    Toast.makeText(IntervalRunClickedMenu.this, "Please enter a valid time.", Toast.LENGTH_LONG).show();
                else{
                    irNowClicked(intervalRunNow_ib);
                }
            }
        });

        changeIRUI_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                irChangeUIClicked(changeIRUI_ib);
            }
        });

        addIntervalButton.setOnClickListener(new View.OnClickListener(){
            @Override
             public void onClick(View arg0){
                addInterval();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                confirm();
            }
        });


    }

    private void addInterval(){
        IrHalfDBHelper irHalfDBHelper = new IrHalfDBHelper(this);
        String[] confirmedData = irHalfDBHelper.getData().split(" ");

        if(confirmedData[0].equals("Distance")&&confirmedData[1].equals("Distance"))
            addIntervalDD();
        else if(confirmedData[0].equals("Distance")&&confirmedData[1].equals("Time"))
            addIntervalDT();
        else if(confirmedData[0].equals("Time")&&confirmedData[1].equals("Time"))
            addIntervalTT();
        else if(confirmedData[0].equals("Time")&&confirmedData[1].equals("Distance"))
            addIntervalTD();
    }
}