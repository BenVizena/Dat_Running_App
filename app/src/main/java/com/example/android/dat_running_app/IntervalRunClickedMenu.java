package com.example.android.dat_running_app;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;

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
        //  Toast.makeText(FreeRunClickedMenu.this, "ChangeUI Button was clicked!", Toast.LENGTH_SHORT).show();
    }

    public void irChangeUIClicked(View view){
        Intent intent = new Intent(this,FreeRunChangeUI.class);
        startActivity(intent);
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

    private void addInterval(){
        Spinner h1Spinner = (Spinner)findViewById(R.id.irH1Spinner);
        Spinner h2Spinner = (Spinner)findViewById(R.id.irH2Spinner);

        String h1Constraint = h1Spinner.getSelectedItem().toString();
        String h2Constraint = h2Spinner.getSelectedItem().toString();

        if(h1Constraint.length()==1 || h2Constraint.length()==1)
            Toast.makeText(IntervalRunClickedMenu.this, "Please select a constraint for both halves of your intervals.", Toast.LENGTH_LONG).show();
        else{
            //stuff
        }

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



        Button add = (Button)findViewById(R.id.addIntervalButton);
        add.setVisibility(View.VISIBLE);

        LinearLayout irClickedMenuDivider = (LinearLayout)findViewById(R.id.irclickedMenuDividerLL);
        irClickedMenuDivider.setVisibility(View.VISIBLE);

        if(h1Constraint.equals("Distance")&& h2Constraint.equals("Distance")){
            LinearLayout dataEntryDD = (LinearLayout)findViewById(R.id.dataEntryLLDISTANCEDISTANCE);
            dataEntryDD.setVisibility(View.VISIBLE);
        }

        if(h1Constraint.equals("Distance")&& h2Constraint.equals("Time")){
            LinearLayout dataEntryDT = (LinearLayout)findViewById(R.id.dataEntryLLDISTANCETIME);
            dataEntryDT.setVisibility(View.VISIBLE);
        }

        if(h1Constraint.equals("Time")&& h2Constraint.equals("Distance")){
            LinearLayout dataEntryTD = (LinearLayout)findViewById(R.id.dataEntryLLTIMEDISTANCE);
            dataEntryTD.setVisibility(View.VISIBLE);
        }

        if(h1Constraint.equals("Time")&& h2Constraint.equals("Time")){
            LinearLayout dataEntryTT = (LinearLayout)findViewById(R.id.dataEntryLLTIMETIME);
            dataEntryTT.setVisibility(View.VISIBLE);
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
                String time = getIntervalText();
                if(Long.parseLong(time) == 0)
                    Toast.makeText(IntervalRunClickedMenu.this, "Please enter a valid time.", Toast.LENGTH_LONG).show();
                else{
                    Log.d("??",time+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    irDB.addSettings(time);
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
}