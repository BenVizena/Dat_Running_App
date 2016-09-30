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
 * Created by Ben on 8/17/2016.
 *
 * shows run now and change ui buttons.
 * allows user to enter goal distance for runForDistance.
 */

public class RunForDistanceClickedMenu extends AppCompatActivity{
    ImageButton runForDistanceNow_ib;
    ImageButton changeRFDUI_ib;
    RfdDistanceDBHelper rfdDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfd_clicked_menu);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));

        addMainButtons();
    }

    public void rfdNowClicked(View view){
        Intent intent = new Intent(this,FreeRunningScreen.class);
        startActivity(intent);
        finish();
    }

    public void rfdChangeUIClicked(View view){
        Intent intent = new Intent(this,RFDChangeUI.class);
        startActivity(intent);
        finish();
    }

    private String getDistanceText(){
        EditText editText = (EditText)findViewById(R.id.rfdDistanceEditText);
        return editText.getText().toString();
    }

    public void addMainButtons(){

        rfdDB = new RfdDistanceDBHelper(this);
        runForDistanceNow_ib=(ImageButton) findViewById(R.id.runForDistanceNow_ib);
        changeRFDUI_ib=(ImageButton)findViewById(R.id.changeRFDUI_ib);


        runForDistanceNow_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                String dist = getDistanceText();
                if(getDistanceText().length()==0)
                    Toast.makeText(RunForDistanceClickedMenu.this, "Please enter a valid distance.", Toast.LENGTH_SHORT).show();
                else{
                    rfdDB.addSettings(dist);
                    rfdNowClicked(runForDistanceNow_ib);
                }

            }
        });

        changeRFDUI_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                rfdChangeUIClicked(changeRFDUI_ib);
            }
        });


    }
}
