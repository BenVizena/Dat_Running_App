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

/**
 * Created by Ben on 7/24/2016.
 */

public class IRChangeUI extends AppCompatActivity {
    private FreeRunDBHelper frDB;
    private Button commitChangesButton;
    private static boolean metric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_run_change_ui);

        frDB = new FreeRunDBHelper(this);
        commitChangesButton = (Button)findViewById(R.id.confirmFreeRunUIButton);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));


    }

    public void commitChanges(View view){
        RadioButton metricRadioButton = (RadioButton)findViewById(R.id.metricButton);
        if(metricRadioButton.isChecked())
            frDB.addSetting("true");
        else
            frDB.addSetting("false");
        Intent intent = new Intent(this,IntervalRunClickedMenu.class);
        startActivity(intent);
        finish();

    }






    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}