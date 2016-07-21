package com.example.android.dat_running_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

import static android.R.attr.permission;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


/**
 * Created by Ben on 7/12/2016.
 */

public class RunningScreen extends AppCompatActivity{

    private final String LOG_TAG = "running! activity";

    private double longitude=-1;
    private double latitude=-1;
    private long elapsedTime=0;
    private double distanceTravelled=0;

    MyReceiver myReceiver;
    String outputString;


    private TextView txtOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runningscreen);
        Toolbar runningToolbar = (Toolbar) findViewById(R.id.runningToolbar);
        setSupportActionBar(runningToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        outputString="";

        txtOutput = (TextView) findViewById(R.id.txtOutput);
        Log.d("DEBUG Running Screen","preRun");

        permissionRequest();





    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            String outputString = arg1.getStringExtra("outputString");
            String[] delimedString=outputString.split("\n");

            if(delimedString.length==1)
                updateTextView(outputString);
            else {
                outputString = delimedString[0] + "\n" + formatTime(delimedString[1]) + "\n" + delimedString[2] + "\n" + delimedString[3] + "\n" + delimedString[4];
                updateTextView(outputString);
            }



        }

    }

    private String formatTime(String s){
        long millis = Long.parseLong(s);

        String hmsm = String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                TimeUnit.MILLISECONDS.toMillis(millis)  % TimeUnit.SECONDS.toMillis(1));



        return "TIME: "+hmsm;
    }

    public void startService(View view){
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RunningScreenService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        Intent intent = new Intent(this,RunningScreenService.class);
        startService(intent);

    }

    public void stopService(View view){
        Intent intent = new Intent(this,RunningScreenService.class);
        stopService(intent);
        unregisterReceiver(myReceiver);


    }

    private void updateTextView(String str){
        txtOutput.setText(str);
    }

    public void permissionRequest(){
        Log.d("DEBUG","made it to permissionRequest");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        10);

                Log.d("DEBUG","Requesting Permission...");

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("DEBUG","permission granted!");
               //     Intent intent = new Intent(this,RunningScreen.class);
               //     startActivity(intent);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    permissionRequest();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }







}