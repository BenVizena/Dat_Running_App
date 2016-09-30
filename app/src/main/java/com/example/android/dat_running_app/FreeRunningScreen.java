package com.example.android.dat_running_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
//import static com.example.android.dat_running_app.R.id.txtOutput;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.google.android.gms.analytics.internal.zzy.B;
import static com.google.android.gms.analytics.internal.zzy.e;
import static com.google.android.gms.analytics.internal.zzy.r;
import static com.google.android.gms.analytics.internal.zzy.s;
import static com.google.android.gms.analytics.internal.zzy.t;
import static com.google.android.gms.analytics.internal.zzy.v;
import static com.google.android.gms.cast.internal.zzl.pa;


/**
 * Created by Ben on 7/12/2016.
 *
 * controlls the running screen for all runs.
 */

public class FreeRunningScreen extends AppCompatActivity implements OnMapReadyCallback{

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
    private boolean soundPlayed;
    private boolean stopPressed;
    private MyReceiver myReceiver;
    private String outputString;
    private RunDBHelper RDB;
    private WhichRunDBHelper WRDB;
    private double distanceToTravel;//the distance specified in the Run For Distance menu.
    private long timeToRun;//the time in millis specified in the Run For Time mneu.
    private ArrayList<String> interpretedIntervalList;
    private int dbUpdateTimer=0;
    private final int DBUPDATELIMIT=10;//this says that every x updates to the running screen, the db will get 1 update.
    private boolean metric;
    private int txtOutputId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundPlayed=false;
        stopPressed=false;

        setContentView(R.layout.activity_runningscreen);

        RDB = new RunDBHelper(this);
        WhichRunDBHelper wr = new WhichRunDBHelper(this);
        runType = wr.getRunType();
        RDB.close();

        if(runType.equals("RUN FOR DISTANCE")){
            RfdDistanceDBHelper temp = new RfdDistanceDBHelper(this);
            String distString = temp.getRunDistance();
            distanceToTravel=Double.parseDouble(distString);
            temp.close();
        }else
            distanceToTravel=0;

        if(runType.equals("RUN FOR TIME")){
            RfTimeDBHelper temp = new RfTimeDBHelper(this);
            String timeString = temp.getRunTime();
            timeToRun=Long.parseLong(timeString);
            temp.close();
        }else
            timeToRun=0;

        //and then if timeToRun && distanceToTravel==0, then intervalRun==true.

        wr.close();

        irStarted=false;
        interpretedIntervalList = new ArrayList<>();

        intervalList = new ArrayList<>();

        //map stuff
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //this section interprets the units for the running screen.
        FreeRunDBHelper ui = new FreeRunDBHelper(this);
        String tempUnit = ui.getUnitSetting();
        ui.close();
        if(tempUnit.equals("true"))
            metric=true;
        else
            metric=false;


        //this section sets the information filled part of the screen to 168 pixels in height (so from height to height-168
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height=dm.heightPixels;

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 168, r.getDisplayMetrics());

        View view = (View)findViewById(R.id.runningScreenRL);
        ViewGroup.LayoutParams p=view.getLayoutParams();
        p.height=(int)(px);
        p.width=width;
        view.setLayoutParams(p);


        outputString="";

        permissionRequest();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    /*
        updates runner's position on the map.
     */
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
    }


    /*
        Used to determine pace, i.e. milliseconds until you finish 1 km.
     */
    private String milliToFinish(String speed, boolean metric){
        String delimedSpeed[] = speed.split(" ");
        double dubSpeed = Double.parseDouble(delimedSpeed[1]);
        double milliToFin = 0;

        if(metric && dubSpeed!=0) {
            milliToFin = 1 / dubSpeed * 1000 * 1000;
        }
        else if(dubSpeed!=0)
            milliToFin = 1609.34 * 1000 / dubSpeed;

        Long result = (Long)Math.round(milliToFin);

        return ""+result;//time in milliseconds to finish 1 km or 1 mi.
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


    /*
        receives and interprets information from the service.
        updates db with info from the service.
     */
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            String outputString = arg1.getStringExtra("outputString");


            try {
                String[] delimedString = outputString.split("\n");
                if(delimedString.length==1){
                    String[] delimedStringWithCoords = outputString.split(",");
                    double lat = Double.parseDouble(delimedStringWithCoords[1]);
                    double lng = Double.parseDouble(delimedStringWithCoords[2]);
                    updateMarker(lat,lng);

                    updateTextView("GETTING GPS SIGNAL");
                }
                else {//0 is coords, 1 is time (ms) , 2 is altitude (?), 3 is distance travelled (m), 4 is deltaD (m), 5 is velocity (m/s), 6 is start time (epoch), 7 is cadence
                    dbUpdateTimer+=1;
                    changeVisibilities();


                    String t[] = delimedString[1].split(" ");

                    //this big string of commands basically just gets and organizes information in to usable forms.
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
                    updateTextView("");


                    if(dbUpdateTimer>=DBUPDATELIMIT){//when condition is true, update db.
                        dbUpdateTimer=0;
                        RDB.addUpdate(runType,  delimedString[6],   delimedString[1],  delimedString[3],     paceKM   ,   speedKmHr,            delimedString[7],               "ELEVATION: 1337");
                        //              N/A      startTime (epoch)     time (ms)          distance (m)       pace (ms)         km/hr       cadence (strikes per minute)          no longer a thing
                    }



                    String[] delimedDistance = delimedString[3].split(" ");
                    String[] delimedTime = delimedString[1].split(" ");

                    distanceTravelled = Double.parseDouble(delimedDistance[2]);

                    updateTime(delimedString[1]);
                    updateDistance(delimedDistance[2]);
                    updatePace(paceKM);
                    updateCadence(delimedString[7]);


                    //beeps if goal distance is reached (run for distance)
                    if(Double.parseDouble(delimedDistance[2]) >= distanceToTravel && distanceToTravel>0 && !soundPlayed){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                        soundPlayed=true;
                    }

                    //beeps if goal time is reached (run for time)
                    if(Long.parseLong(delimedTime[1]) >= timeToRun && timeToRun>0 && !soundPlayed){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                        soundPlayed=true;
                    }

                    if(runType.equals("INTERVAL RUN") && irStarted==false){//starts interval run
                        irStarted=true;

                        populateIntervalList();
                        interpretIntervals();
                    }
                }
            }catch(NullPointerException e){}
        }
    }

    /*
        update the time textview
     */
    private void updateTime(String s){
        TextView textView = (TextView)findViewById(R.id.timeOutputTV);
        textView.setText(formatTime(s.split(" ")[1]));
    }

    /*
        update the distance textView.
     */
    private void updateDistance(String d){
        Double dd = Double.parseDouble(d);
        TextView textView = (TextView)findViewById(R.id.distanceOutputTV);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        if(metric) {
            double km = Double.valueOf(decimalFormat.format(dd/1000));
            textView.setText(km + " km");
        }
        else{
            double mi = Double.valueOf(decimalFormat.format(dd*.000621371));
            textView.setText(mi+" mi");
        }

    }

    /*
        updates the pace textview
     */
    private void updatePace(String s){
        TextView textView = (TextView)findViewById(R.id.paceOutputTV);
        textView.setText(formatTime(s.split(" ")[1]));
    }

    /*
        updates the cadence textView
     */
    private void updateCadence(String s){
        TextView textView = (TextView)findViewById(R.id.cadenceOutputTV);
        double d = Double.parseDouble(s.split(" ")[1]);
        textView.setText(((int)d)+" strikes/min");
    }


    /*
        determines if an interval is a time interval based on the number of colons the string has has.
     */
    private boolean isTime(String maybeTime){
        String[] delimedMaybeTime = maybeTime.split(":");
        if(delimedMaybeTime.length==3)
            return true;
        else
            return false;
    }

    /*
        converts the time given by the interval (in h:m:s format) in to milliseconds
     */
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

    /*
        asynctask stuff. i think.
     */
    public interface Runnable{
        public void run();
    }


    /*
        setting up asyncInterval class for handling intervals.
     */
    private class AsyncInterval extends AsyncTask<String,Void,Boolean>{

        /*
            handles the intervals.
         */
        @Override
        protected Boolean doInBackground(String... Strings) {
            Boolean result = false;
            Boolean time = false;
            String[] delimedString = Strings[0].split("");
            String unit = delimedString[delimedString.length-1]+delimedString[delimedString.length-2];

            if(unit.equals("im") || unit.equals("mk")){//checking to see if we are dealing with a distance by checking to see if the units are mi (miles) or km (kilometers)
                time=false;
                double startDistance = distanceTravelled;
                String distanceString="";
                for(int i = 0;i<delimedString.length-2;i++)//reassembles the double
                    distanceString=distanceString+delimedString[i];
                double distanceDouble = Double.parseDouble(distanceString);

                if(unit.equals("mk")){
                    distanceDouble*=1000;

                    while(distanceTravelled<startDistance+distanceDouble){//waits for distanceTravelled to reach the target distance.  (re checks every half second).
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {}
                    }
                    if(!stopPressed){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                    }

                    result=true;
                }
                else{
                    distanceDouble*=1609.34;
                    while(distanceTravelled<startDistance+distanceDouble){//waits for distanceTravelled to reach the target distance.  (re checks every half second).
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {}
                    }
                    if(!stopPressed){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                    }

                    result=true;
                }
            }
            else{
                time=true;
                try{
                    Thread.sleep(Long.parseLong(Strings[0]));//sleeps for a time equal to the target time, then beeps if stop hasn't been pressed.

                    if(!stopPressed){
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                    }

                    result = true;
                }catch(InterruptedException e){

                }
            }
            return result;
        }
    }

    /*
        constructs a list of intervals
     */
    private void interpretIntervals(){
            boolean locked = false;
            for (int x = 0; x < intervalList.size(); x++) {
                String[] delimedInterval = intervalList.get(x).split(" ");

                if (isTime(delimedInterval[2]) && !locked) {
                    locked=true;
                    //do time things by starting a different thread.
                    Long time = getTimeFromString(delimedInterval[2]);
                    interpretedIntervalList.add("TIME "+time);

                    new AsyncInterval().execute(time+"");

                    locked = false;


                } else {
                    interpretedIntervalList.add("DISTANCE " + delimedInterval[2]);
                    //do distance things
                    new AsyncInterval().execute(delimedInterval[2]);
                }

                if (isTime(delimedInterval[4]) && !locked) {
                    locked=true;
                    //do time things by starting a different thread.
                    Long time = getTimeFromString(delimedInterval[4]);


                    interpretedIntervalList.add("TIME "+time);

                    new AsyncInterval().execute(time+"");

                    locked = false;

                } else {
                    interpretedIntervalList.add("DISTANCE "+delimedInterval[4]);
                    //do distance things
                    new AsyncInterval().execute(delimedInterval[4]);
                }
            }
    }


    /*
        takes the stored intervals and puts them in to intervalList so that they can be properly processed.
     */
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
        imdb.close();
    }

    /*
        takes in milliseconds (in String format) and outputs the time in hours:minutes:seconds:milliseconds format (as a String).
     */
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

        }catch(NumberFormatException e){}

        return ""+hmsm;
    }

    /*
        starts the service when start button is pressed.
        also sets up receiver for the service.
     */
    public void startService(View view){
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FreeRunningScreenService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        Intent intent = new Intent(this,FreeRunningScreenService.class);
        startService(intent);

        makeStartButtonGoAway();
    }

    /*
        stops the service when "stop" button is pressed.
        sends user directly to stats screen.  they do not pass go. they do not collect $200
     */
    public void stopService(View view){
        Intent intent = new Intent(this,FreeRunningScreenService.class);
        stopService(intent);//stops freeRunningScreenService
        unregisterReceiver(myReceiver);
        stopPressed=true;//this variable is used to stop beeps from happening after the user has left the screen

        Intent intent2 = new Intent(this,StatsScreen.class);
        startActivity(intent2);//sends user to statsscreen
        finish();


    }

    /*
        after start button is pressed, make the start button go away.
     */
    private void makeStartButtonGoAway(){
        Button b1 = (Button)findViewById(R.id.startButton);
        b1.setVisibility(View.GONE);
    }

    /*
        after gps is found, make all of the relevant views visible.
     */
    private void changeVisibilities(){
        Button b1 = (Button)findViewById(R.id.stopButton);
        Button b2 = (Button)findViewById(R.id.startButton);
        b1.setVisibility(View.VISIBLE);
        b2.setVisibility(View.GONE);

        TextView tv1 = (TextView)findViewById(R.id.timeOutputMarkerTV);
        TextView tv2 = (TextView)findViewById(R.id.timeOutputTV);
        TextView tv3 = (TextView)findViewById(R.id.distanceOutputMarkerTV);
        TextView tv4 = (TextView)findViewById(R.id.distanceOutputTV);
        TextView tv5 = (TextView)findViewById(R.id.paceOutputMarkerTV);
        TextView tv6 = (TextView)findViewById(R.id.paceOutputTV);
        TextView tv7 = (TextView)findViewById(R.id.cadenceOutputMarkerTV);
        TextView tv8 = (TextView)findViewById(R.id.cadenceOutputTV);

        tv1.setVisibility(View.VISIBLE);
        tv2.setVisibility(View.VISIBLE);
        tv3.setVisibility(View.VISIBLE);
        tv4.setVisibility(View.VISIBLE);
        tv5.setVisibility(View.VISIBLE);
        tv6.setVisibility(View.VISIBLE);
        tv7.setVisibility(View.VISIBLE);
        tv8.setVisibility(View.VISIBLE);
    }

    /*
        this displays "looking for gps" or whatever that message is.
     */
    private void updateTextView(String str){
        TextView output = new TextView(this);
        try {
            output = (TextView) findViewById(R.id.outputTextView);
            output.setText(str);
        }
        catch(NullPointerException e){}
    }

    public void permissionRequest(){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        10);

            }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                } else {

                    permissionRequest();

                    // permission denied, boo! Request permission
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}