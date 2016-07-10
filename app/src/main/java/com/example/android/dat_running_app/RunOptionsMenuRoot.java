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

import static android.R.attr.width;
import static com.example.android.dat_running_app.R.drawable.go_running;
import static com.example.android.dat_running_app.R.id.go_running_ib;

/**
 * Created by Ben on 7/9/2016.
 */

public class RunOptionsMenuRoot extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_options_menu_root);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.8));


    }

    public void goBack(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
