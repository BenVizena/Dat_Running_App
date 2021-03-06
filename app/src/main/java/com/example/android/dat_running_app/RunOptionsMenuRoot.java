package com.example.android.dat_running_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import static android.R.attr.width;
import static com.example.android.dat_running_app.R.drawable.go_running;
import static com.example.android.dat_running_app.R.drawable.intervalrun;
import static com.example.android.dat_running_app.R.id.freerun_ib;
import static com.example.android.dat_running_app.R.id.go_running_ib;
import static com.example.android.dat_running_app.R.id.intervalrun_ib;
import static com.example.android.dat_running_app.R.id.runfortime_ib;
import static com.example.android.dat_running_app.R.id.runwear_ib;
import static com.example.android.dat_running_app.R.id.stats_ib;

/**
 * Created by Ben on 7/9/2016.
 *
 * allows user to pick which type of run they want.
 *
 * shows freeRun, runForTime, runForDistance, and IntervalRun buttons.
 */



public class RunOptionsMenuRoot extends AppCompatActivity {

    ImageButton freerun_ib;
    ImageButton runfortime_ib;
    ImageButton runfordistance_ib;
    ImageButton intervalrun_ib;

    private WhichRunDBHelper wrdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_options_menu_root);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.5));


        addMainButtons();

    }

    public void goBack(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void freeRunClicked(View view){
        wrdb = new WhichRunDBHelper(this);
        wrdb.refreshRunType("FREE RUN");
        Intent intent = new Intent(this,FreeRunClickedMenu.class);
        startActivity(intent);
    }

    private void rfdClicked(View view){
        wrdb = new WhichRunDBHelper(this);
        wrdb.refreshRunType("RUN FOR DISTANCE");
        Intent intent = new Intent(this,RunForDistanceClickedMenu.class);
        startActivity(intent);
    }

    private void rftClicked(View view){
        wrdb = new WhichRunDBHelper(this);
        wrdb.refreshRunType("RUN FOR TIME");
        Intent intent = new Intent(this,RunForTimeClickedMenu.class);
        startActivity(intent);
    }

    private void irClicked(View view){
        wrdb = new WhichRunDBHelper(this);
        wrdb.refreshRunType("INTERVAL RUN");
        Intent intent = new Intent(this,IntervalRunClickedMenu.class);
        startActivity(intent);
    }

    public void addMainButtons(){

        freerun_ib=(ImageButton) findViewById(R.id.freerun_ib);
        runfortime_ib=(ImageButton)findViewById(R.id.runfortime_ib);
        runfordistance_ib=(ImageButton)findViewById(R.id.runfordistnace_ib);
        intervalrun_ib=(ImageButton)findViewById(R.id.intervalrun_ib);

        freerun_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                freeRunClicked(freerun_ib);
            }
        });

        runfortime_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                rftClicked(runfortime_ib);
            }
        });

        runfordistance_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                rfdClicked(runfordistance_ib);
            }
        });

        intervalrun_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                irClicked(intervalrun_ib);
            }
        });

    }
}
