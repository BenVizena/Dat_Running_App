package com.example.android.dat_running_app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.RadioButton;

import static com.example.android.dat_running_app.RunningScreenService.MY_ACTION;

/**
 * Created by Ben on 7/24/2016.
 */

public class FreeRunChangeUI extends AppCompatActivity {
    private static boolean totalTime;
    private static boolean totalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_run_change_ui);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));

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


    }

    private void refreshTotalTime(){
        RadioButton stt;
        stt = (ToggleableRadioButton)findViewById(R.id.showTimeRadioButton);
        if(stt.isChecked())
            totalTime=true;
        else
            totalTime=false;
    }

    private void refreshTotalDistance(){
        ToggleableRadioButton std;
        std = (ToggleableRadioButton)findViewById(R.id.showDistanceRadioButton);
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
}
