package com.example.android.dat_running_app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RadioButton;

/**
 * Created by Ben on 7/24/2016.
 *
 * User uses this class and the associated activity to choose the display units of runningscreen.
 */

public class FreeRunChangeUI extends AppCompatActivity {
    private FreeRunDBHelper frDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_run_change_ui);

        frDB = new FreeRunDBHelper(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));


    }
    /*
        if metric units desired, send "true", else, send "false"
     */
    public void commitChanges(View view){
        RadioButton metricRadioButton = (RadioButton)findViewById(R.id.metricButton);
        if(metricRadioButton.isChecked())
            frDB.addSetting("true");
        else
            frDB.addSetting("false");
        Intent intent = new Intent(this,FreeRunClickedMenu.class);
        startActivity(intent);
        finish();

    }

}
