package com.example.android.dat_running_app;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by Ben on 8/19/2016.
 */

public class RunForTimeClickedMenu extends AppCompatActivity{
    ImageButton runForTimeNow_ib;
    ImageButton changeRFTUI_ib;
    RfTimeDBHelper rftDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rft_clicked_menu);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));//was .8
        // getWindow().setLayout(270,270);

        addMainButtons();
    }

    public void rftNowClicked(View view){
        Intent intent = new Intent(this,FreeRunningScreen.class);
        startActivity(intent);
        finish();
        //  Toast.makeText(FreeRunClickedMenu.this, "ChangeUI Button was clicked!", Toast.LENGTH_SHORT).show();
    }

    public void rftChangeUIClicked(View view){
        Intent intent = new Intent(this,RFTChangeUI.class);
        startActivity(intent);
        finish();
    }

    private String getTimeText(){
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

    public void addMainButtons(){

        rftDB = new RfTimeDBHelper(this);
        runForTimeNow_ib=(ImageButton) findViewById(R.id.runForTimeNow_ib);
        changeRFTUI_ib=(ImageButton)findViewById(R.id.changeRFTUI_ib);


        runForTimeNow_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                String time = getTimeText();
                if(Long.parseLong(time) == 0)
                    Toast.makeText(RunForTimeClickedMenu.this, "Please enter a valid time.", Toast.LENGTH_LONG).show();
                else{
                    Log.d("??",time+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    rftDB.addSettings(time);
                    rftNowClicked(runForTimeNow_ib);
                }
            }
        });

        changeRFTUI_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                rftChangeUIClicked(changeRFTUI_ib);
            }
        });


    }
}