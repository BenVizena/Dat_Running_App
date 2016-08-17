package com.example.android.dat_running_app;

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
 */



public class RunOptionsMenuRoot extends AppCompatActivity {

    ImageButton freerun_ib;
    ImageButton runfortime_ib;
    ImageButton runfordistance_ib;
    ImageButton intervalrun_ib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_options_menu_root);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.5));//was .8
       // getWindow().setLayout(270,270);


        addMainButtons();
    }

    public void goBack(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void freeRunClicked(View view){
        Intent intent = new Intent(this,FreeRunClickedMenu.class);
        startActivity(intent);

        //finish();//i don't really know if i want to close the window when i open the new one.
    }

    private void rfdClicked(View view){
        Intent intent = new Intent(this,RunForDistanceClickedMenu.class);
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
                Toast.makeText(RunOptionsMenuRoot.this, "Run for Time Button was clicked!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RunOptionsMenuRoot.this, "Interval Run Button was clicked!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
