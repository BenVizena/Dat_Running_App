package com.example.android.dat_running_app;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import static android.R.attr.button;
import static com.example.android.dat_running_app.RunningScreenService.MY_ACTION;

/**
 * Created by Ben on 7/24/2016.
 */

public class FreeRunChangeUI extends AppCompatActivity {
    private FreeRunDBHelper frDB;
    private Button commitChangesButton;
    private static boolean totalTime;
    private static boolean totalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_run_change_ui);

        frDB = new FreeRunDBHelper(this);
        commitChangesButton = (Button)findViewById(R.id.confirmFreeRunUIButton);
        getSettings();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));
/*
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);//500
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshTotalDistance();
                                refreshTotalTime();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        t.start();
*/

    }
/*
    public void commitChanges(){
        commitChangesButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("DB TAG","PRE INSERTED");
                        boolean isInserted = frDB.addSettings(totalTime, totalDistance);
                        if(isInserted)
                            Log.d("DB TAG","INSERTED");
                        else
                            Log.d("DB TAG","NOT INSERTED");
                    }
                }
        );
    }
 */
    public void commitChanges(View view){
        refreshTotalTime();
        refreshTotalDistance();
        boolean isInserted = frDB.addSettings(totalTime, totalDistance);
        Log.d("THIS IS SENT",""+totalTime+" "+totalDistance+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if(isInserted)
            Log.d("DB TAG","INSERTED");
        else
            Log.d("DB TAG","NOT INSERTED");
        Intent intent = new Intent(this,FreeRunClickedMenu.class);
        startActivity(intent);

    }

    public void getSettings(){
        Cursor settings = frDB.getFRSettings();
        CheckBox stt;
        stt = (CheckBox)findViewById(R.id.showTimeCheckBox);
        CheckBox std;
        std = (CheckBox) findViewById(R.id.showDistanceCheckBox);

        if (!settings.moveToFirst())
            settings.moveToFirst();

        if(settings.getCount()==0){
            showMessage("ERROR","NOTHING FOUND");
        }
        else{
            if((""+settings.getString(1)).equals("true")){
                totalTime=true;
                stt.toggle();
            }
            else if(stt.isChecked()){
                totalTime=false;
                stt.toggle();
                Log.d("TOGGLE","TIME OFF");
            }
            if(settings.getString(2).equals("true")){
                totalDistance=true;
                std.toggle();
            }
            else if(std.isChecked()){
                totalDistance=false;
                std.toggle();
                Log.d("TOGGLE","DISTANCE OFF");
            }
         //   showMessage("DATA","TOTALTIME:"+totalTime+" "+"TOTALDISTANCE: "+totalDistance);

        }
    }

    private void refreshTotalTime(){
        CheckBox stt;
        stt = (CheckBox) findViewById(R.id.showTimeCheckBox);
        if(stt.isChecked())
            totalTime=true;
        else{
            totalTime=false;
        }

    }

    private void refreshTotalDistance(){
        CheckBox std;
        std = (CheckBox) findViewById(R.id.showDistanceCheckBox);
        if(std.isChecked())
            totalDistance=true;
        else
            totalDistance=false;
    }

    public static boolean showTotalTime(){
        return totalTime;
    }

    public static boolean showTotalDistance(){
        return totalDistance;
    }


    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}
