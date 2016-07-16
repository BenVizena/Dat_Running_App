package com.example.android.dat_running_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static android.R.attr.permission;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Ben on 7/12/2016.
 */

public class RunningScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private final String LOG_TAG = "running! activity";

    private TextView txtOutput;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private String mLatitudeText;
    private String mLongitudeText;
    private long startTime = android.os.SystemClock.elapsedRealtime();
    private long elapsedTime = 0;
    private double distanceTravelled=0;
    private double deltaD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runningscreen);
        Toolbar runningToolbar = (Toolbar) findViewById(R.id.runningToolbar);
        setSupportActionBar(runningToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;
       // mCurrentLocation.setAltitude(0);

     //   String outputString = "COORDS: "+ findViewById(R.id.txtOutput) + " /nTIME: "+elapsedTime;

        txtOutput = (TextView) findViewById(R.id.txtOutput);
        Log.d("DEBUG","preRun");
  //      runUpdater();

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
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




    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG","bottom one");
            permissionRequest();
        }

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG","PASSED ONCONNECTED CHECK -1");

            return;
        }
        Log.d("DEBUG","PASSED ONCONNECTED CHECK");



        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);



        if (mCurrentLocation != null) {
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            mLatitudeText =""+mCurrentLocation.getLatitude();
            mLongitudeText=""+mCurrentLocation.getLongitude();
            //  LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            txtOutput.setText(mLatitudeText+" "+mLongitudeText);
        }

        Log.d("DEBUG","startLocationUpdates");
        startLocationUpdates();


    }

    public double getDeltaD(){
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
        return d;
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this,RunningScreen.class);
                    startActivity(intent);
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


    public void update(){

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
        if(mLastLocation == null){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("DEBUG","well... i'm there");
        }

        else{

            if (mLastLocation != null && mCurrentLocation != null)
                deltaD = getDeltaD();

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            distanceTravelled+=deltaD;
            elapsedTime = android.os.SystemClock.elapsedRealtime()-startTime;
            Log.d("DEBUG","well... i'm here");

            String outputString = "COORDS: "+ Double.toString(mCurrentLocation.getLatitude())+"    "+Double.toString(mCurrentLocation.getLongitude())+" \nTIME: "+elapsedTime+"\nALTITUDE: "+mCurrentLocation.getAltitude()
                    +"\nDISTANCE TRAVELLED: "+distanceTravelled+"\nDeltaD: "+deltaD;
            txtOutput.setText(outputString);
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


    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(800)
                .setFastestInterval(500);



        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG","bottom one");
            permissionRequest();
        }


        // Request location updates
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }


}