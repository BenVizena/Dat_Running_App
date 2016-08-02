package com.example.android.dat_running_app;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.analytics.internal.zzy.d;


public class RunningScreenService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private final String LOG_TAG = "running! activity";
    final static String MY_ACTION = "MY_ACTION";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private static String mLatitudeText;
    private String mLongitudeText;
    private long startTime = android.os.SystemClock.elapsedRealtime();
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

    private static Handler handler;

    @Override
    public void onCreate(){
        handler = new Handler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;


        Log.d("DEBUG","onCreate()");
        waitForStableGPS();




    }

    private void runGPS(){
        Log.d("DEBUG runGPS entry","Running this shit.");



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
        Log.d("DEBUG","starting waitForStableGPS()");
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
                                    Log.d("DEBUG","getting gps signal");
                                    counter+=1;
                                }
                                else{
                                    Log.d("DEBUG","getting gps signal");
                                    counter=0;
                                }
                                if(counter>=5){
                                    Log.d("DEBUG","gps signal acquired");
                                    stable = true;
                                    t.interrupt();
                                    startTime = android.os.SystemClock.elapsedRealtime();
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
                                Log.d("DEBUG","intent broadcasted");


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
        Log.d("DEBUG","onConnected");

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG","PERMISSION CHECK IN SERVICE ON CONNECTED");

            return;
        }
        Log.d("DEBUG","PASSED ONCONNECTED CHECK");



        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("DEBUG","Gave current and last location values");


        if (mCurrentLocation != null) {
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            mLatitudeText =""+mCurrentLocation.getLatitude();//these are useless
            mLongitudeText=""+mCurrentLocation.getLongitude();
            //  LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //txtOutput.setText(mLatitudeText+" "+mLongitudeText);
        }

        Log.d("DEBUG","startLocationUpdates");
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
        Log.d("STATUS","ENTERING POTENTIAL SNAFU!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! VelocityCounter: "+velocityCounter +" STABLE: "+stable);
        if(velocityCounter==0 && stable){
            try{startVelocityLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            velocityCounter++;
                Log.d("STATUS","AVOIDED SNAFU 1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            catch(final SecurityException e){Log.d("STATUS","COAXED INTO A SNAFU 1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");}
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
                Log.d("STATUS","NOT IN A SNAFU2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            catch(final SecurityException e){Log.d("STATUS","COAXED INTO A SNAFU2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");}
        }
        else if(stable) {
            velocityCounter++;
            Log.d("STATUS","EXITING A SNAFU HEAVY SITUATION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void update(){


        //  Log.d("DEBUG","PASSED ONCONNECTED CHECK");
        try {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Log.d("DEBUG","mLastLocation == null\n");
            }
            else{

                if (mCurrentLocation != null)
                    deltaD = getDeltaD(mLastLocation,mCurrentLocation);

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                distanceTravelled+=deltaD;
                elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;

                try{

                    findVelocity();

                outputText =
                        "COORDS: "+ Double.toString(mCurrentLocation.getLatitude())+" "+Double.toString(mCurrentLocation.getLongitude())
                        +" \n"+elapsedTime+"\nALTITUDE: " +mCurrentLocation.getAltitude()
                        +"\nDISTANCE TRAVELLED: "+distanceTravelled
                        +"\nDeltaD: "+deltaD
                        +"\nVelocity: "+velocity;
                //txtOutput.setText(outputString);
                }catch(NullPointerException e){
                    outputText="";
                }
                //outputText=outputString;
                //if RunningScreen is in the foreground

                Log.d("DEBUG",outputText);
            }
        }
        catch(final SecurityException ex){
            Log.d("DEBUG","Bad Permissions Setup");
        }
    }

    public long getElapsedTime(){
        return android.os.SystemClock.elapsedRealtime()-startTime;
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
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }


    private void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(500);



        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG","bottom one");
            new RunningScreen().permissionRequest();
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
        Toast.makeText(RunningScreenService.this, "service started", Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(RunningScreenService.this, "service destroyed", Toast.LENGTH_LONG).show();
        t.interrupt();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




}
