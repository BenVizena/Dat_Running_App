package com.example.android.dat_running_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by Ben on 9/16/2016.
 *
 * holds user's mass or wieght. (used for calculating calories burned).
 */

public class MainSettings extends AppCompatActivity{
    private MainSettingsDBHelper msdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;

        getWindow().setLayout((int)(width*.5),(int)(height*.25));//was .5
        // getWindow().setLayout(270,270);


 //       addMainButtons();

    }

    public void submitClicked(View view){
        msdb = new MainSettingsDBHelper(this);
        double mass=0;
        RadioButton kgButton = (RadioButton)findViewById(R.id.kgButton);
        EditText massET = (EditText)findViewById(R.id.massET);

        if(massET.getText().toString().equals("") || massET.getText().toString().equals("0")){
            Toast.makeText(MainSettings.this,"Please enter a non-zero value",Toast.LENGTH_SHORT).show();
        }
        else{
            if(kgButton.isChecked()){
                mass = Double.parseDouble(massET.getText().toString());
            }
            else{
                double pounds = Double.parseDouble(massET.getText().toString());
                mass = .4536 * pounds;
            }

            msdb.addMass(""+mass);
            finish();
        }
   //     Intent intent = new Intent(this,MainActivity.class);
   //     startActivity(intent);
    }
}
