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
        runGPS();




    }

    private void runGPS(){
        Log.d("DEBUG runGPS entry","Running this shit.");



        t = new Thread() {
            int counter = 0;
            boolean gpsStable=false;

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);//500
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();
                                if(deltaD==0)
                                    counter+=1;
                                else if(!gpsStable)
                                    counter=0;
                                if(counter>=5 || gpsStable){
                                    gpsStable=true;
                                    Intent intent = new Intent();
                                    intent.setAction(MY_ACTION);

                                    intent.putExtra("outputString",outputText);

                                    sendBroadcast(intent);
                                }
                                else if(!gpsStable){
                                    distanceTravelled=0;
                                }

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

    private double getDeltaD(){
        double lat1 = mLastLocation.getLatitude();
        double lon1 = mLastLocation.getLongitude();
        double lat2 = mCurrentLocation.getLatitude();
        double lon2 = mCurrentLocation.getLongitude();

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
                    deltaD = getDeltaD();

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                distanceTravelled+=deltaD;
                elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;


                outputText =
                        "COORDS: "+ Double.toString(mCurrentLocation.getLatitude())+"    "+Double.toString(mCurrentLocation.getLongitude())
                        +" \nTIME: "+elapsedTime+"\nALTITUDE: " +mCurrentLocation.getAltitude()
                        +"\nDISTANCE TRAVELLED: "+distanceTravelled
                        +"\nDeltaD: "+deltaD;
                //txtOutput.setText(outputString);

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
