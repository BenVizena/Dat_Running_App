package com.example.android.dat_running_app;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    ImageButton run_ib;
    ImageButton runwear_ib;
    ImageButton settings_ib;
    ImageButton stats_ib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        addMainButtons();



    }

    public void runClicked(View view){
        startActivity(new Intent(this,RunOptionsMenuRoot.class));
    }

    public void statsClicked(View view){
        startActivity(new Intent(this,StatsScreen.class));
    }

    public void settingsClicked(View view){
        startActivity(new Intent(this,MainSettings.class));
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void addMainButtons(){

        run_ib=(ImageButton) findViewById(R.id.go_running_ib);
        runwear_ib=(ImageButton)findViewById(R.id.runwear_ib);
        settings_ib=(ImageButton)findViewById(R.id.setting_ib);
        stats_ib=(ImageButton)findViewById(R.id.stats_ib);

        run_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                runClicked(run_ib);
            }
        });

        runwear_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                Toast.makeText(MainActivity.this, "Runwear features are coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        settings_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                settingsClicked(settings_ib);
       //         Toast.makeText(MainActivity.this, "MainSettings Button was clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        stats_ib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                statsClicked(stats_ib);
            }
        });

    }
}
