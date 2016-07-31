package com.example.android.dat_running_app;

import android.Manifest;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static android.R.attr.alignmentMode;
import static android.R.attr.layout_alignBottom;
import static android.R.attr.layout_alignParentBottom;
import static android.R.attr.permission;
import static android.R.interpolator.linear;
import static android.R.style.Theme;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;
import static android.support.v7.appcompat.R.attr.actionBarSize;
import static android.support.v7.appcompat.R.attr.colorPrimary;
import static com.example.android.dat_running_app.R.id.map;
import static com.example.android.dat_running_app.R.id.txtOutput;
import static com.example.android.dat_running_app.R.style.AppTheme;
import static com.google.android.gms.analytics.internal.zzy.a;
import static com.google.android.gms.analytics.internal.zzy.g;


/**
 * Created by Ben on 7/12/2016.
 */

public class RunningScreen extends AppCompatActivity implements OnMapReadyCallback{

    private final String LOG_TAG = "running! activity";

    private double longitude=-1;
    private double latitude=-1;
    private long elapsedTime=0;
    private double distanceTravelled=0;
    private GoogleMap gMap;
    private Marker m;

    MyReceiver myReceiver;
    String outputString;


//    private TextView txtOutput;
    private int txtOutputId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


/*
        RelativeLayout myLayout = new RelativeLayout(this);//////////////////////////////start relative layout

        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams q = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams r = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

    //    ((RelativeLayout)myLayout).setGravity(Gravity.BOTTOM);

        Window window = this.getWindow();////////////////////////////////////////////////make status bar correct color
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Toolbar runningToolBar = new Toolbar(this);/////////////////////////////////////make new toolbar
        runningToolBar.generateViewId();
        int toolBarId = runningToolBar.getId();

  //      TextView spaceTaker = new TextView(this);



        int paddingOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -42, getResources().getDisplayMetrics());////////////////make title for toolbar
        TextView myTitle = new TextView(this);
        myTitle.setText("RUNNING!");
        myTitle.setLayoutParams(new Toolbar.LayoutParams(-1, -1));
        myTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
        myTitle.setTextColor(ContextCompat.getColor(this,android.R.color.black));
        myTitle.setPadding(paddingOffset,0,0,0);
        myTitle.setGravity(Gravity.CENTER);//////////////////////////////////////////////////////////////////////////////////////////////////////end toolbar title stuff

        LinearLayout sps = new LinearLayout(this);
        sps.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        myLayout.addView(sps,lp);


        final Button startButton = new Button(this);/////////////////////////////////////////////////////////////////////////////////////////////////////make start button
        startButton.generateViewId();
        startButton.setId((int)android.os.SystemClock.elapsedRealtime());
        int startButtonId=startButton.getId();
        startButton.setText("start");
        startButton.setBackgroundColor(Color.GREEN);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(startButton);
            }
        });
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        param.gravity=Gravity.BOTTOM;
        startButton.setLayoutParams(param);
        sps.addView(startButton);////////////////////////////////////////////////////////////////////////////////////////////////////////////////end start button


        final Button endButton = new Button(this);/////////////////////////////////////////////////////////////////////////////////////////////////////make end button
        endButton.generateViewId();
        endButton.setId((int)android.os.SystemClock.elapsedRealtime());
        int endButtonId=endButton.getId();
        endButton.setText("stop");
        endButton.setBackgroundColor(Color.RED);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(endButton);
            }
        });

        endButton.setLayoutParams(param);
        sps.addView(endButton);////////////////////////////////////////////////////////////////////////////////////////////////////////////////end end button

        final Button startMapButton = new Button(this);/////////////////////////////////////////////////////////////////////////////////////////////////////make temp map button
        startMapButton.generateViewId();
        startMapButton.setId((int)android.os.SystemClock.elapsedRealtime());
        int startMapButtonId=startMapButton.getId();
        startMapButton.setText("start map");
        startMapButton.setBackgroundColor(Color.YELLOW);
        startMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RunningScreen.this, FreeRunMapsActivity.class);
                startActivity(intent);
            }
        });

        startMapButton.setLayoutParams(param);
        sps.addView(startMapButton);////////////////////////////////////////////////////////////////////////////////////////////////////////////////end temp map button

        TextView txtOutput=new TextView(this);////////////////////////////////////////////////////////////////////////////////////////////begin txtOutput
        //txtOutput.generateViewId();
       // txtOutput.setId((int)android.os.SystemClock.elapsedRealtime()+123123);
      //  int txtOutputId=txtOutput.getId();
        txtOutput.setId(txtOutput.generateViewId());
        txtOutputId=txtOutput.getId();
        Log.d("TXTOUTPUT IDDDDDDDD",txtOutput.getId()+"");
        q.addRule(RelativeLayout.CENTER_IN_PARENT);
        txtOutput.setLayoutParams(q);
        txtOutput.setText("HELLO");
        myLayout.addView(txtOutput);///////////////////////////////////////////////////////////////////////////////////////////////////////////end txtOutput


        TextView commsTest=new TextView(this);////////////////////////////////////////////////////////////////////////////////////////////begin txtOutput
        //txtOutput.generateViewId();
        // txtOutput.setId((int)android.os.SystemClock.elapsedRealtime()+123123);
        //  int txtOutputId=txtOutput.getId();
        commsTest.setId(txtOutput.generateViewId());
       // txtOutputId=txtOutput.getId();
      //  Log.d("TXTOUTPUT IDDDDDDDD",txtOutput.getId()+"");
        r.addRule(RelativeLayout.CENTER_VERTICAL);
        r.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        txtOutput.setLayoutParams(r);
        txtOutput.setText(""+FreeRunChangeUI.showTotalDistance());
        myLayout.addView(commsTest);




        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());//////////////////////set up toolbar
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(-1, height);
        runningToolBar.setLayoutParams(params);
        runningToolBar.setElevation(20);
        runningToolBar.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));///////////////////////////////////////////////////end toolbar setup



        myLayout.addView(runningToolBar);
        runningToolBar.addView(myTitle);

        */

        setContentView(R.layout.activity_runningscreen);

 //       setContentView(R.layout.activity_runningscreen);
 //       Toolbar runningToolbar = (Toolbar) findViewById(R.id.runningToolbar);
  //      setSupportActionBar(runningToolbar);
  //      getSupportActionBar().setDisplayShowTitleEnabled(false);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




        outputString="";

 //       txtOutput = (TextView) findViewById(R.id.txtOutput);
 //       Log.d("DEBUG Running Screen",startButtonId+" "+endButtonId);

        permissionRequest();





    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
//        LatLng llStart = new LatLng()
 //       googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void updateMarker(double lat, double lng){
        LatLng latLng = new LatLng(lat,lng);
        try{
            m.remove();
        }catch(NullPointerException e){

        }

        MarkerOptions mo = new MarkerOptions().position(latLng);
        m = gMap.addMarker(mo);
        m.setPosition(latLng);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
//        gMap.animateCamera(CameraUpdateFactory.zoomIn());
//        gMap.animateCamera(CameraUpdateFactory.zoomTo(15),10000,null);//middle was 2000
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            String outputString = arg1.getStringExtra("outputString");
            String[] delimedString=outputString.split("\n");

           Log.d("DEBUG","MADE IT TO THE RECIEVER"+ outputString);
            if(delimedString.length==1){
                String[] delimedStringWithCoords = outputString.split(",");
                double lat = Double.parseDouble(delimedStringWithCoords[1]);
                double lng = Double.parseDouble(delimedStringWithCoords[2]);
                updateMarker(lat,lng);

                updateTextView(outputString);
            }
             else {
                outputString = delimedString[0] + "\n" + formatTime(delimedString[1]) + "\n" + delimedString[2] + "\n" + delimedString[3] + "\n" + delimedString[4];
                String[] coords = delimedString[0].split(" ");
                double lat = Double.parseDouble(coords[1]);
                double lng = Double.parseDouble(coords[2]);
                updateMarker(lat,lng);
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
        Log.d("SDKLFJSL","!!!!MADE IT HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Intent intent = new Intent(this,RunningScreenService.class);
        startService(intent);

    }

    public void stopService(View view){
  //      myReceiver = new MyReceiver();
        Intent intent = new Intent(this,RunningScreenService.class);
        stopService(intent);
        unregisterReceiver(myReceiver);


    }

    private void updateTextView(String str){
 //       try{
 //       Log.d("DEBUG","SDFJKLSDFJLSKDJFSDKLFJSDLKFJSKLDFJSLDKFJSKDLFJSLKDFJSLKFJ"+str);
        TextView output = new TextView(this);
        try {
            output = (TextView) findViewById(txtOutput);
            Log.d("TXTOUTPUT IDDDDDDDDDDDD","ID: "+output.getId());
//        Log.d("sdkfsklfdjsklfjfkl",txtOutput.toString()+"");
            output.setText(str);
        }
        catch(NullPointerException e){
            Log.d("TXTOUTPUT","NULL");
        }


 //       catch(NullPointerException e){

//        }

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