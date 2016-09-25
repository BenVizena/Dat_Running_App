package com.example.android.dat_running_app;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Ben on 7/11/2016.
 */

public class FreeRunClickedMenu extends AppCompatActivity{

    ImageButton freeRunNow_ib;
    ImageButton changeUI_ib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_run_clicked_menu);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .5));//was .8
        // getWindow().setLayout(270,270);

        addMainButtons();
    }

    public void freeRunNowClicked(View view){
        Intent intent = new Intent(this,FreeRunningScreen.class);
        startActivity(intent);
        finish();
      //  Toast.makeText(FreeRunClickedMenu.this, "ChangeUI Button was clicked!", Toast.LENGTH_SHORT).show();
    }

    public void freeRunChangeUIClicked(View view){
        Intent intent = new Intent(this,FreeRunChangeUI.class);
        startActivity(intent);
        finish();
    }

    public void addMainButtons(){

        freeRunNow_ib=(ImageButton) findViewById(R.id.freerunnow_ib);
        changeUI_ib=(ImageButton)findViewById(R.id.freerunchangeui_ib);


        freeRunNow_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                freeRunNowClicked(freeRunNow_ib);
            }
        });

        changeUI_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                freeRunChangeUIClicked(changeUI_ib);
            }
        });


    }
}
