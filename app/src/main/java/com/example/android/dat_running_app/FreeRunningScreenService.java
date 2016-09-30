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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.os.Handler;

import java.util.Date;



public class FreeRunningScreenService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SensorEventListener{

    final static String MY_ACTION = "MY_ACTION";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private static String mLatitudeText;
    private String mLongitudeText;
    long startTime = new Date().getTime();

    private long elapsedTime = 0;
    private double distanceTravelled=0;
    private double deltaD;
    private Thread t;
    private String outputText;
    private boolean stable;

    private final int THREAD_MILLI = 100;//puts a delay in the update intervals.

    private double velocity=0;
    private int velocityCounter=0;
    private final int MAX_VELOCITY_COUNTER=10;//velocity counter gets incremented this many times.  when velocityCounter == this, the position at velocityCounter=0 is compared to the position at velocityCounter = 10 to get the velocity at that point.
    private Location startVelocityLocation;
    private Location endVelocityLocation;

    private int stepCounter=0;//number of steps in a step interval.
    private int stepInterval=0;//when this number is 100, the current steps per minute will update. e.g. after 10 seconds we will update the current steps per minute.
    private double stepsPerMinute = 0;//last steps per minute reading.
    private boolean stepLocked = false;//forces the accelerometer to wait between steps.  Otherwise it records several steps for each step because the acceleration is higher than the threshold for more than an instant.

    private static Handler handler;
    private SensorManager mSensorManager;
    private Sensor mAcceleration;

    @Override
    public void onCreate(){
        handler = new Handler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)//sets up Google Maps stuff.
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);//getting ready to
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//use accelerometer.

        waitForStableGPS();
    }

    /*
        sets up a thread to update at certain intervals.
     */
    private void runGPS(){
        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(THREAD_MILLI);
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

    /*
        waits for 50 consecutive intervals without gps detecting movement. after that, it starts the runGPS method.
     */
    private void waitForStableGPS(){
        t = new Thread() {
            int counter = 0;

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(THREAD_MILLI);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();

                                if(deltaD==0){//getting gps signal
                                    counter+=1;
                                }
                                else{//still getting gps signal. (reset because gps detected that we moved.)
                                    counter=0;
                                }
                                if(counter>=50){//if we didn't move for fifty updates, we have a good gps lock.  start running.
                                    stable = true;
                                    t.interrupt();
                                    startTime = new Date().getTime();
                                    elapsedTime=0;
                                    runGPS();//starts runGPS.
                                }

                                distanceTravelled=0;

                                Intent intent = new Intent();
                                intent.setAction(MY_ACTION);
                                try{
                                    intent.putExtra("outputString","GETTING GPS SIGNAL,"+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());//signals freeRunningScreen that run is starting.
                                }catch(NullPointerException e){

                                }
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

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            return;
        }

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

    /*
        calculates deltaD from two locations.
     */
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

    /*
        finds velocities using velocityCounter
     */
    private void findVelocity(){
        if(velocityCounter==0 && stable){//gets the starting point.
            try{startVelocityLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            velocityCounter++;
            }
            catch(final SecurityException e){}
        }
        else if(velocityCounter>=MAX_VELOCITY_COUNTER){
            try{endVelocityLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                double distanceTravelled = getDeltaD(startVelocityLocation,endVelocityLocation);
                int deltaT = THREAD_MILLI * 10/1000;//time 10 for the ten update intervals and div 1000 for milli to seconds conversion
                if(distanceTravelled/deltaT<.1)//trying to filter out noise.
                    velocity = 0;
                else
                    velocity = distanceTravelled/deltaT;
                velocityCounter=0;
            }
            catch(final SecurityException e){}
        }
        else if(stable) {
            velocityCounter++;
        }
    }


    /*
        this is a big un'.  If runGPS is the brain of this service, update is the heart.
        updates outputText, which is sent via intent to FreeRunningScreen.
     */
    private void update(){
        try {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {//have we gotten a position yet? if not, we need to get our first one.
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            else{
                if (mCurrentLocation != null)
                    deltaD = getDeltaD(mLastLocation,mCurrentLocation);//get deltaD

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                distanceTravelled+=deltaD;//get distanceTravelled.
                elapsedTime = new Date().getTime()-startTime;//get elapsedTime

                try{

                    findVelocity();//get velocity


                    if(stepInterval>=100){
                        stepsPerMinute = stepCounter*6;//get stepsPerMinute (cadence)
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
                }catch(NullPointerException e){
                    outputText="";
                }
            }
        }
        catch(final SecurityException ex){}
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
        mGoogleApiClient.connect();
        mSensorManager.registerListener(this,mAcceleration,SensorManager.SENSOR_DELAY_GAME);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
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


    /*
        accelerometer stuff for cadence.
        if resultant acceleration from all three directions is over a threshold, call that a step.  Don't allow any other steps to be counted until the resultant acceleration is
        below a certain threshold (to differentiate steps.  Then look for a step again.
     */
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
