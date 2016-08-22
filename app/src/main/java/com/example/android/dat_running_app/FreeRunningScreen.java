package com.example.android.dat_running_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.android.dat_running_app.R.id.txtOutput;
import static com.google.android.gms.analytics.internal.zzy.e;
import static com.google.android.gms.analytics.internal.zzy.r;
import static com.google.android.gms.analytics.internal.zzy.t;
import static com.google.android.gms.analytics.internal.zzy.v;


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
    private boolean irStarted;
    private ArrayList<String> intervalList;
    private IntervalDBHelper imdb;

    private MyReceiver myReceiver;
//    private MyReceiver myReceiverIR;
    private String outputString;
    private RunDBHelper RDB;
    private WhichRunDBHelper WRDB;
    private double distanceToTravel;//the distance specified in the Run For Distance menu.
    private long timeToRun;//the time in millis specified in the Run For Time mneu.
    private ArrayList<String> interpretedIntervalList;

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
        }else
            distanceToTravel=0;

        if(runType.equals("RUN FOR TIME")){
            String timeString = new RfTimeDBHelper(this).getRunTime();
            timeToRun=Long.parseLong(timeString);
        }else
            timeToRun=0;

        irStarted=false;
        interpretedIntervalList = new ArrayList<>();

        intervalList = new ArrayList<>();

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
                    String[] delimedTime = delimedString[1].split(" ");

                    distanceTravelled = Double.parseDouble(delimedDistance[2]);

                    if(Double.parseDouble(delimedDistance[2]) >= distanceToTravel && distanceToTravel>0){;
                        goalReachedStopService();
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                    }

                    if(Long.parseLong(delimedTime[1]) >= timeToRun && timeToRun>0){
                        goalReachedStopService();
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                    }

                    if(runType.equals("INTERVAL RUN") && irStarted==false){
                        irStarted=true;
                        /*
                        myReceiverIR = new MyReceiver();
                        IntentFilter intentFilterIR = new IntentFilter();
                        intentFilterIR.addAction(IntervalRunService.MY_ACTION);
                        registerReceiver(myReceiverIR,intentFilterIR);
                        Intent intentIR = new Intent(FreeRunningScreen.this,IntervalRunService.class);
                        startService(intentIR);
                         */

                        populateIntervalList();
                        Log.d("MADE","SIZE: "+intervalList.size());
                        interpretIntervals();



                    }


                }
            }catch(NullPointerException e){}





        }



    }



    private boolean isTime(String maybeTime){
 //       Log.d("DelimedLength",""+maybeTime);
        String[] delimedMaybeTime = maybeTime.split(":");
 //       Log.d("DelimedLength",""+delimedMaybeTime.length);
        if(delimedMaybeTime.length==3)
            return true;
        else
            return false;
    }

    private Long getTimeFromString(String timeString){
        String[] delimedTime = timeString.split(":");

        long hr = Long.parseLong(delimedTime[0]);
        hr=hr*3600*1000;

        long min = Long.parseLong(delimedTime[1]);
        min=min*60*1000;

        long sec = Long.parseLong(delimedTime[2]);
        sec=sec*1000;

        return hr+min+sec;
    }

    public interface Runnable{
        public void run();
    }

    private class AsyncInterval extends AsyncTask<String,Void,Boolean>{


        @Override
        protected Boolean doInBackground(String... Strings) {
            Boolean result = false;
            Boolean time = false;
            String[] delimedString = Strings[0].split("");
            String unit = delimedString[delimedString.length-1]+delimedString[delimedString.length-2];
            Log.d("UNIT",""+unit);

            if(unit.equals("im") || unit.equals("mk")){
                time=false;
                double startDistance = distanceTravelled;
                String distanceString="";
                for(int i = 0;i<delimedString.length-2;i++)
                    distanceString=distanceString+delimedString[i];
                double distanceDouble = Double.parseDouble(distanceString);

                if(unit.equals("mk")){
                    distanceDouble*=1000;

                    while(distanceTravelled<startDistance+distanceDouble){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {}
                    }
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                    mp.start();
                    result=true;
                }
                else{
                    distanceDouble*=1609.34;
                    Log.d("UNIT",distanceTravelled+"   "+distanceDouble);
                    while(distanceTravelled<startDistance+distanceDouble){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {}
                    }
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                    mp.start();
                    result=true;
                }
            }
            else{
                time=true;
                try{
                    Log.d("MADE","ASYNC MADE IT HERE");
                    Thread.sleep(Long.parseLong(Strings[0]));

                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                    mp.start();
                    result = true;
                }catch(InterruptedException e){

                }
            }
            return result;
        }
    }

    private void interpretIntervals(){
            boolean locked = false;
   //         int counter=0;
            for (int x = 0; x < intervalList.size(); x++) {
      //          while(counter<intervalList.size()*3){
                    String[] delimedInterval = intervalList.get(x).split(" ");
                    //           Log.d("IMPORTANT", ""+intervalList.get(x));

                    if (isTime(delimedInterval[2]) && !locked) {
                        locked=true;
                        Log.d("INTERNAL THREAD", "FIRST CONTAINS A TIME");
                        //do time things by starting a different thread.
                        Long time = getTimeFromString(delimedInterval[2]);


                        interpretedIntervalList.add("TIME "+time);

                        new AsyncInterval().execute(time+"");



            //            MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
             //           mp.start();

                        locked = false;
              //          counter+=1;


                    } else {
                        Log.d("INTERNAL THREAD", "FIRST CONTAINS A DISTANCE");
                        interpretedIntervalList.add("DISTANCE " + delimedInterval[2]);
                        //do distance things
                        new AsyncInterval().execute(delimedInterval[2]);



                    }

                    if (isTime(delimedInterval[4]) && !locked) {
                        locked=true;
                        Log.d("INTERNAL THREAD", "FIRST CONTAINS A TIME");
                        //do time things by starting a different thread.
                        Long time = getTimeFromString(delimedInterval[4]);

                        interpretedIntervalList.add("TIME "+time);

                        new AsyncInterval().execute(time+"");



                        locked = false;
              //          counter+=1;

                    } else {
                        Log.d("INTERNAL THREAD", "SECOND CONTAINS A DISTANCE");
                        interpretedIntervalList.add("DISTANCE "+delimedInterval[4]);
                        //do distance things
                        new AsyncInterval().execute(delimedInterval[4]);


                    }
         //       }

            }

    }



    private void populateIntervalList(){
        imdb = new IntervalDBHelper(this);
        Cursor dataCursor = imdb.getDataCursor();

        try {
            do {
                intervalList.add(dataCursor.getString(1));
            }while(dataCursor.moveToNext());
        } finally {
            dataCursor.close();
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

        RDB = new RunDBHelper(this);
        runType = new WhichRunDBHelper(this).getRunType();

        /*
        if(runType.equals("INTERVAL RUN")){
            myReceiverIR = new MyReceiver();
            IntentFilter intentFilterIR = new IntentFilter();
            intentFilterIR.addAction(IntervalRunService.MY_ACTION);
            registerReceiver(myReceiverIR,intentFilterIR);
            Intent intentIR = new Intent(this,IntervalRunService.class);
            startService(intentIR);
        }
*/
    }

    public void stopService(View view){
  //      myReceiver = new MyReceiver();
        Intent intent = new Intent(this,FreeRunningScreenService.class);
        stopService(intent);
        unregisterReceiver(myReceiver);
/*
        Intent intentIR = new Intent(this,IntervalRunService.class);
        stopService(intentIR);
        unregisterReceiver(myReceiverIR);
*/

    }

    public void goalReachedStopService(){
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