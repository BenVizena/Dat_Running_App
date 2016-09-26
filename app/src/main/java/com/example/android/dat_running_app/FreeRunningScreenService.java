package com.example.android.dat_running_app;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.os.Handler;

import java.util.Date;

import static com.google.android.gms.analytics.internal.zzy.a;
import static com.google.android.gms.analytics.internal.zzy.l;
import static com.google.android.gms.analytics.internal.zzy.n;


public class FreeRunningScreenService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SensorEventListener{

 //   private final String LOG_TAG = "running! activity";
    final static String MY_ACTION = "MY_ACTION";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private static String mLatitudeText;
    private String mLongitudeText;
//    private long startTime = android.os.SystemClock.elapsedRealtime();
    long startTime = new Date().getTime();

    private long elapsedTime = 0;
    private double distanceTravelled=0;
    private double deltaD;
    private Thread t;
    private String outputText;
    private boolean stable;

    private final int THREAD_MILLI = 100;

    private double velocity=0;
    private int velocityCounter=0;
    private final int MAX_VELOCITY_COUNTER=10;
    private Location startVelocityLocation;
    private Location endVelocityLocation;

    private int stepCounter=0;//number of steps in a step interval.
    private int stepInterval=0;//when this number is 100, the current steps per minute will update. e.g. after 10 seconds we will update the current steps per minute.
    private double stepsPerMinute = 0;//last steps per minute reading.
    private boolean stepLocked = false;//forces the accelerometer to wait between steps.  Otherwise it records several steps for each step because the acceleration is higher than the threshold for more than an instant.
 //   private int numSteps = 0; //number of steps in the current interval.


    private static Handler handler;

    private SensorManager mSensorManager;
    private Sensor mAcceleration;

    @Override
    public void onCreate(){
        handler = new Handler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

   //     Log.d("StartTime!",""+startTime);
  //      elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;


//        Log.d("DEBUG","onCreate()");
        waitForStableGPS();




    }

    private void runGPS(){
//        Log.d("DEBUG runGPS entry","Running this shit.");



        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(THREAD_MILLI);//500
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();

                                Intent intent = new Intent();
                                intent.setAction(MY_ACTION);
                                intent.putExtra("outputString",outputText);
                                sendBroadcast(intent);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        t.start();
    }

    private void waitForStableGPS(){
  //      Log.d("DEBUG","starting waitForStableGPS()");
        t = new Thread() {
            int counter = 0;

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(THREAD_MILLI);//500
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();

                                if(deltaD==0){
  //                                  Log.d("DEBUG","getting gps signal");
                                    counter+=1;
                                }
                                else{
   //                                 Log.d("DEBUG","getting gps signal");
                                    counter=0;
                                }
                                if(counter>=40){//was 50
 //                                  Log.d("DEBUG","gps signal acquired");
                                    stable = true;
                                    t.interrupt();
                                    startTime = new Date().getTime();
                                    elapsedTime=0;
                                    runGPS();
                                }

                                distanceTravelled=0;

                                Intent intent = new Intent();
                                intent.setAction(MY_ACTION);

                                try{
                                    intent.putExtra("outputString","GETTING GPS SIGNAL,"+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                                }catch(NullPointerException e){

                                }


                                sendBroadcast(intent);
  //                              Log.d("DEBUG","intent broadcasted");


                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        t.start();
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
 //       Log.d("DEBUG","onConnected");

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
  //          Log.d("DEBUG","PERMISSION CHECK IN SERVICE ON CONNECTED");

            return;
        }
 //       Log.d("DEBUG","PASSED ONCONNECTED CHECK");



        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
 //       Log.d("DEBUG","Gave current and last location values");


        if (mCurrentLocation != null) {
  //          Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            mLatitudeText =""+mCurrentLocation.getLatitude();//these are useless
            mLongitudeText=""+mCurrentLocation.getLongitude();
            //  LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //txtOutput.setText(mLatitudeText+" "+mLongitudeText);
        }

  //      Log.d("DEBUG","startLocationUpdates");
        startLocationUpdates();


    }

    private double getDeltaD(Location start, Location end){
        double lat1 = start.getLatitude();
        double lon1 = start.getLongitude();
        double lat2 = end.getLatitude();
        double lon2 = end.getLongitude();

        double R = 6371; // km
        double dLat = (lat2-lat1)*Math.PI/180;
        double dLon = (lon2-lon1)*Math.PI/180;
        lat1 = lat1*Math.PI/180;
        lat2 = lat2*Math.PI/180;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c * 1000;

        if(d>=.22352)
            return d;
        else
            return 0;
    }


    private void findVelocity(){
//        Log.d("STATUS","ENTERING POTENTIAL SNAFU!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! VelocityCounter: "+velocityCounter +" STABLE: "+stable);
        if(velocityCounter==0 && stable){
            try{startVelocityLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            velocityCounter++;
 //               Log.d("STATUS","AVOIDED SNAFU 1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            catch(final SecurityException e){}
        }
        else if(velocityCounter>=MAX_VELOCITY_COUNTER){
            try{endVelocityLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                double distanceTravelled = getDeltaD(startVelocityLocation,endVelocityLocation);
                int deltaT = THREAD_MILLI * 10/1000;//time 10 for the ten update intervals and div 1000 for milli to seconds conversion
                if(distanceTravelled/deltaT<.1)
                    velocity = 0;
                else
                    velocity = distanceTravelled/deltaT;
                velocityCounter=0;
 //               Log.d("STATUS","NOT IN A SNAFU2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            catch(final SecurityException e){}
        }
        else if(stable) {
            velocityCounter++;
//            Log.d("STATUS","EXITING A SNAFU HEAVY SITUATION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void update(){


        //  Log.d("DEBUG","PASSED ONCONNECTED CHECK");
        try {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
 //               Log.d("DEBUG","mLastLocation == null\n");
            }
            else{

                if (mCurrentLocation != null)
                    deltaD = getDeltaD(mLastLocation,mCurrentLocation);

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                distanceTravelled+=deltaD;
                elapsedTime = new Date().getTime()-startTime;

                try{

                    findVelocity();


                    if(stepInterval>=100){
                        stepsPerMinute = stepCounter*6;
                        stepInterval=0;
                        stepCounter=0;
                    }else{
                        stepInterval++;
                    }

                outputText =
                        "COORDS: "+ Double.toString(mCurrentLocation.getLatitude())+" "+Double.toString(mCurrentLocation.getLongitude())
                        +" \nTime: "+elapsedTime
                        +"\nALTITUDE: " +mCurrentLocation.getAltitude()
                        +"\nDISTANCE TRAVELLED: "+distanceTravelled
                        +"\nDeltaD: "+deltaD
                        +"\nVelocity: "+velocity
                        +"\nStartTime: "+startTime
                        +"\nCadence: "+stepsPerMinute;
                //txtOutput.setText(outputString);
                }catch(NullPointerException e){
                    outputText="";
                }
                //outputText=outputString;
                //if FreeRunningScreen is in the foreground

 //               Log.d("DEBUG",outputText);
            }
        }
        catch(final SecurityException ex){
  //          Log.d("DEBUG","Bad Permissions Setup");
        }
    }

    public long getElapsedTime(){
        return new Date().getTime()-startTime;
    }

    public Double getLatitude(){
        return mCurrentLocation.getLatitude();
    }

    public double getLongitude(){
        return mCurrentLocation.getLongitude();
    }

    public double getDistanceTravelled(){
        return distanceTravelled;
    }



    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
  //      Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }


    private void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(500);



        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
   //         Log.d("DEBUG","bottom one");
            new FreeRunningScreen().permissionRequest();
        }


        // Request location updates
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
  //      Toast.makeText(FreeRunningScreenService.this, "service started", Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();

        mSensorManager.registerListener(this,mAcceleration,SensorManager.SENSOR_DELAY_GAME);

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
  //      Toast.makeText(FreeRunningScreenService.this, "service destroyed", Toast.LENGTH_LONG).show();
        t.interrupt();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        mSensorManager.unregisterListener(this);



    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        double accX = Double.parseDouble(sensorEvent.values[0]+"");
        double accY = Double.parseDouble(sensorEvent.values[1]+"");
        double accZ = Double.parseDouble(sensorEvent.values[2]+"");
        double net = Math.sqrt(accX*accX+accY*accY+accZ+accZ);// accX+accY+accZ;

        if(!stepLocked && net>10){
            stepCounter++;
            stepLocked=true;
        }
        if(stepLocked && net<5)
            stepLocked=false;


 //       Log.d("ACCELERATION","NET: "+net+"");
  //      Log.d("ACCELERATION","STEPCOUNTER:" +stepCounter+"");
  //      Log.d("ACCELERATION","STEPS PER MINUTE: "+stepsPerMinute);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
