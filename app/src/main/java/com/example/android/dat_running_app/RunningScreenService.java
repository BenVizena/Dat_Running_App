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





public class RunningScreenService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private final String LOG_TAG = "running! activity";


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

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();
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
        // Here, thisActivity is the current activity
 /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG","PERMISSION CHECK IN SERVICE IDK WHAT THIS DOES");
            new RunningScreen().permissionRequest();
        }
*/
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
            mLatitudeText =""+mCurrentLocation.getLatitude();
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
/*
        Log.d("distance log: ","dist betn "+
                d + " " +
                mLastLocation.getLatitude()+ " " +
                mLastLocation.getLongitude() + " " +
                mCurrentLocation.getLatitude() + " " +
                mCurrentLocation.getLongitude()
        );

*/
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
                Log.d("DEBUG","(mLastLocation && mCurrentLocation) !=null");

                String outputString =
                        "COORDS: "+ Double.toString(mCurrentLocation.getLatitude())+"    "+Double.toString(mCurrentLocation.getLongitude())
                        +" \nTIME: "+elapsedTime+"\nALTITUDE: " +mCurrentLocation.getAltitude()
                        +"\nDISTANCE TRAVELLED: "+distanceTravelled+"\nDeltaD: "+deltaD;
                //txtOutput.setText(outputString);
                Log.d("DEBUG",outputString);
            }
        }
        catch(final SecurityException ex){
            Log.d("DEBUG","Bad Permissions Setup");
        }
    }



    @Override
    public void onLocationChanged(Location location) {
/*
        Log.i(LOG_TAG, location.toString()+"DeltaD: "+deltaD);
       // txtOutput.setText(Double.toString(location.getLatitude())+" "+Double.toString(location.getLongitude()));


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           // Log.d("DEBUG","bottom one");
            permissionRequest();
        }

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
         //   Log.d("DEBUG","PASSED ONCONNECTED CHECK -1");

            return;
        }
      //  Log.d("DEBUG","PASSED ONCONNECTED CHECK");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation == null)
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        else{

            if (mLastLocation != null && mCurrentLocation != null)
                deltaD = getDeltaD();

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        distanceTravelled+=deltaD;
        elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;

        String outputString = "COORDS: "+ Double.toString(location.getLatitude())+"    "+Double.toString(location.getLongitude())+" \nTIME: "+elapsedTime+"\nALTITUDE: "+location.getAltitude()
                +"\nDISTANCE TRAVELLED: "+distanceTravelled+"\nDeltaD: "+deltaD;
        txtOutput.setText(outputString);
*/
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
                .setInterval(800)
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
