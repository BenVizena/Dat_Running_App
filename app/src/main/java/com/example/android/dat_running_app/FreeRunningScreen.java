package com.example.android.dat_running_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.TimeUnit;

import static com.example.android.dat_running_app.R.id.txtOutput;
import static com.google.android.gms.analytics.internal.zzy.e;


/**
 * Created by Ben on 7/12/2016.
 */

public class FreeRunningScreen extends AppCompatActivity implements OnMapReadyCallback{

    private final String LOG_TAG = "running! activity";

    private double longitude=-1;
    private double latitude=-1;
    private long elapsedTime=0;
    private double distanceTravelled=0;
    private GoogleMap gMap;
    private Marker m;
    private boolean rfd;
    private String runType;//"FREE RUN", "RUN FOR DISTANCE",

    private MyReceiver myReceiver;
    private String outputString;
    private RunDBHelper RDB;
    private WhichRunDBHelper WRDB;
    private double distanceToTravel;//the distance specified in the Run For Distance menu.

    private int dbUpdateTimer=0;
    private final int DBUPDATELIMIT=10;//this says that every x updates to the running screen, the db will get 1 update. //was 10. 10 is probably good. that is 1 update to db for every second. //100 works for debugging purposes.


//    private TextView txtOutput;
    private int txtOutputId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_runningscreen);

        RDB = new RunDBHelper(this);
        runType = new WhichRunDBHelper(this).getRunType();

        if(runType.equals("RUN FOR DISTANCE")){
            String distString = new RfdDistanceDBHelper(this).getRunDistance();
            Log.d("DEBUG","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+distString);
            distanceToTravel=Double.parseDouble(distString);
        }

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


    /*
        Used to determine pace, i.e. milliseconds until you finish 1 km.
     */
    private String milliToFinish(String speed, boolean metric){
        String delimedSpeed[] = speed.split(" ");
        double dubSpeed = Double.parseDouble(delimedSpeed[1]);
//        Log.d("DUBSPEED",dubSpeed+"");
        double milliToFin = 0;

        if(metric && dubSpeed!=0) {
            milliToFin = 1 / dubSpeed * 1000 * 1000;
  //          Log.d("metric","made it here "+ milliToFin);
        }
        else if(dubSpeed!=0)
            milliToFin = 1609.34 * 1000 / dubSpeed;

        Long result = (Long)Math.round(milliToFin);

        return ""+result;
    }

    /*
        turns speed in m/s to km/hr or mi/hr
     */
    private String getSpeed(String speed, boolean metric){
        String delimedSpeed[] = speed.split(" ");
        double dubSpeed = Double.parseDouble(delimedSpeed[1]);
        if(metric)
            return ((double)(dubSpeed*3.6))+"";
        else
            return ((double)(dubSpeed*2.23694)+"");
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            String outputString = arg1.getStringExtra("outputString");


            try {
                String[] delimedString = outputString.split("\n");
         //       Log.d("DEBUG","MADE IT TO THE RECIEVER"+ outputString);
                if(delimedString.length==1){
                    String[] delimedStringWithCoords = outputString.split(",");
                    double lat = Double.parseDouble(delimedStringWithCoords[1]);
                    double lng = Double.parseDouble(delimedStringWithCoords[2]);
                    updateMarker(lat,lng);

                    updateTextView(outputString);
                }
                else {//0 is coords, 1 is time (ms) , 2 is altitude (?), 3 is distance travelled (m), 4 is deltaD (m), 5 is velocity (m/s), 6 is start time (epoch)
                    dbUpdateTimer+=1;

                    String t[] = delimedString[1].split(" ");

                    outputString = delimedString[0] + "\nTIME: " + formatTime(t[1]) + "\n" + delimedString[2] + "\n" + delimedString[3] + "\n" + delimedString[4];
                    String[] coords = delimedString[0].split(" ");
                    String speedKmHr = "Speed: "+getSpeed(delimedString[5],true);
                    String speedMiHr = getSpeed(delimedString[5],false);
                    String timeToFinishKm=formatTime(milliToFinish(delimedString[5],true));
                    String paceKM = "Pace: "+milliToFinish(delimedString[5],true);
                    String timeToFinishMile=formatTime(milliToFinish(delimedString[5],false));
                    outputString += "\nSpeed (km/hr): "+speedKmHr+"\nSpeed (mi/hr): "+speedMiHr;
                    outputString += "\nPace (Km): "+timeToFinishKm + "\nPace (Mi): "+timeToFinishMile;
                    double lat = Double.parseDouble(coords[1]);
                    double lng = Double.parseDouble(coords[2]);
                    updateMarker(lat,lng);
                    updateTextView(outputString);

                    if(dbUpdateTimer>=DBUPDATELIMIT){
                        dbUpdateTimer=0;
                        RDB.addUpdate(runType,  delimedString[6],   delimedString[1],  delimedString[3],     paceKM   ,   speedKmHr,   "CADENCE: 1337", "ELEVATION: 1337");
                        //              N/A      startTime (epoch)     time (ms)          distance (m)       pace (ms)         m/s
                    }

                    String[] delimedDistance = delimedString[3].split(" ");

                    if(Double.parseDouble(delimedDistance[2]) >= distanceToTravel && distanceToTravel>0){
                        Log.d("FINISHED","DONE DONE DONE DONE DONE"+Double.parseDouble(delimedDistance[2])+" >= "+ distanceToTravel);
                        //stopService()
                    }

                }
            }catch(NullPointerException e){}





        }

    }

    private String formatTime(String s){
        String hmsm="";
        try {
            long millis = Long.parseLong(s);
            hmsm = String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                    TimeUnit.MILLISECONDS.toMillis(millis) % TimeUnit.SECONDS.toMillis(1));

            String delimedTime[] = hmsm.split(":");

            if(delimedTime[0].equals("00"))
                hmsm = delimedTime[1]+":"+delimedTime[2]+":"+delimedTime[3];

        }catch(NumberFormatException e){

        }


        return ""+hmsm;
    }

    public void startService(View view){
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FreeRunningScreenService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
 //       Log.d("SDKLFJSL","!!!!MADE IT HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Intent intent = new Intent(this,FreeRunningScreenService.class);
        startService(intent);

    }

    public void stopService(View view){
  //      myReceiver = new MyReceiver();
        Intent intent = new Intent(this,FreeRunningScreenService.class);
        stopService(intent);
        unregisterReceiver(myReceiver);


    }

    private void updateTextView(String str){
 //       try{
 //       Log.d("DEBUG","SDFJKLSDFJLSKDJFSDKLFJSDLKFJSKLDFJSLDKFJSKDLFJSLKDFJSLKFJ"+str);
        TextView output = new TextView(this);
        try {
            output = (TextView) findViewById(txtOutput);
 //           Log.d("TXTOUTPUT IDDDDDDDDDDDD","ID: "+output.getId());
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
  //      Log.d("DEBUG","made it to permissionRequest");
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

  //              Log.d("DEBUG","Requesting Permission...");

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
              //      Log.d("DEBUG","permission granted!");
               //     Intent intent = new Intent(this,FreeRunningScreen.class);
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