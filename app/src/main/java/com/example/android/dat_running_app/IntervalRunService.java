package com.example.android.dat_running_app;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.x;
import static com.google.android.gms.analytics.internal.zzy.d;
import static com.google.android.gms.analytics.internal.zzy.e;
import static com.google.android.gms.analytics.internal.zzy.f;
import static com.google.android.gms.analytics.internal.zzy.p;
import static com.google.android.gms.analytics.internal.zzy.t;
import static com.google.android.gms.analytics.internal.zzy.w;

/**
 * Created by Ben on 8/21/2016.
 */

public class IntervalRunService extends Service{
    final static String MY_ACTION = "MY_ACTION";
    private static Handler handler;
    private IntervalDBHelper imdb;
    private long startTime;
    private ArrayList<String> intervalList;
    private int threadCount;
    private Thread interpreterThread;
    private Thread timeThread;
    private Thread distThread;

    @Override
    public void onCreate(){
        handler = new Handler();
        startTime = new Date().getTime();
        threadCount=0;

        intervalList = new ArrayList<>();
        populateIntervalList();
        interpretIntervals();
    }

    private boolean thisServiceIsRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTime(String maybeTime){
        Log.d("DelimedLength",""+maybeTime);
        String[] delimedMaybeTime = maybeTime.split(":");
        Log.d("DelimedLength",""+delimedMaybeTime.length);
        if(delimedMaybeTime.length==3)
            return true;
        else
            return false;
    }

    private synchronized void interpretIntervalsAlt(){

        for (int x = 0; x < intervalList.size(); x++) {
            String[] delimedInterval = intervalList.get(x).split(" ");
            Log.d("IMPORTANT", ""+intervalList.get(x));

            if (isTime(delimedInterval[2])) {
                Log.d("INTERNAL THREAD", "FIRST CONTAINS A TIME");
                threadCount += 1;
                //do time things by starting a different thread.
                Long time = getTimeFromString(delimedInterval[2]);

                synchronized(this){
                    try {
                        wait(time);
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                        mp.start();
                    } catch (InterruptedException e1) {}
                }

                /*
                try {
                    wait(time);
                    //    SystemClock.sleep(time);
                } catch (InterruptedException e1) {}
                */


            } else {
                Log.d("INTERNAL THREAD", "FIRST CONTAINS A DISTANCE");
                //do distance things
                try {
                    interpreterThread.wait();
                } catch (InterruptedException e) {
                    interpreterThread.interrupt();
                }

            }

            if (isTime(delimedInterval[4])) {
                Log.d("INTERNAL THREAD", "SECOND CONTAINS A TIME");
                threadCount += 1;
                //do time things
                Long time = getTimeFromString(delimedInterval[4]);
                try {
                    wait(time);
                 //   interpreterThread.sleep(time);
                } catch (InterruptedException e1) {}

            } else {
                Log.d("INTERNAL THREAD", "SECOND CONTAINS A DISTANCE");
                //do distance things

                while (threadCount > 0) {//threadcount will be decrimented when the other thread stops.
                    try {
                        interpreterThread.wait();
                    } catch (InterruptedException e) {
                        interpreterThread.interrupt();
                    }
                }
            }
        }
    }



    private synchronized void interpretIntervals(){
        if(thisServiceIsRunning(IntervalRunService.class)) {


            interpreterThread = new Thread() {

                @Override
                public void run() {
              //      try {
                     //   Thread.sleep(500);
                        Log.d("INTERNAL THREAD", "EXECUTING");
                        Log.d("INTERNAL THREAD", "" + intervalList.get(0));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                    for (int x = 0; x < intervalList.size(); x++) {
                                        String[] delimedInterval = intervalList.get(x).split(" ");
                                        Log.d("IMPORTANT", ""+intervalList.get(x));

                                        if (isTime(delimedInterval[2])) {
                                            Log.d("INTERNAL THREAD", "FIRST CONTAINS A TIME");
                                            threadCount += 1;
                                            //do time things by starting a different thread.
                                            Long time = getTimeFromString(delimedInterval[2]);
                                            try {
                                                interpreterThread.sleep(time);
                                                //    SystemClock.sleep(time);
                                            } catch (InterruptedException e1) {}


                                        } else {
                                            Log.d("INTERNAL THREAD", "FIRST CONTAINS A DISTANCE");
                                            //do distance things
                                            try {
                                                interpreterThread.wait();
                                            } catch (InterruptedException e) {
                                                interpreterThread.interrupt();
                                            }

                                        }

                                        if (isTime(delimedInterval[4])) {
                                            Log.d("INTERNAL THREAD", "SECOND CONTAINS A TIME");
                                            threadCount += 1;
                                            //do time things
                                            Long time = getTimeFromString(delimedInterval[4]);
                                            try {
                                                interpreterThread.sleep(time);
                                            } catch (InterruptedException e1) {}

                                        } else {
                                            Log.d("INTERNAL THREAD", "SECOND CONTAINS A DISTANCE");
                                            //do distance things

                                            while (threadCount > 0) {//threadcount will be decrimented when the other thread stops.
                                                try {
                                                    interpreterThread.wait();
                                                } catch (InterruptedException e) {
                                                    interpreterThread.interrupt();
                                                }
                                            }
                                        }
                                    }

                            }
                        });

            //        } catch (InterruptedException e) {
                     //   interpreterThread.interrupt();
                  //  }
                }

            };

            interpreterThread.start();

        }
    }

    private void runOnUiThread(Runnable runnable){
        handler.post(runnable);
    }

    private void halfForTime(String timeString){
        String[] delimedTime = timeString.split(":");

        long hr = Long.parseLong(delimedTime[0]);
        hr=hr*3600*1000;

        long min = Long.parseLong(delimedTime[1]);
        min=min*60*1000;

        long sec = Long.parseLong(delimedTime[2]);
        sec=sec*1000;

        final long time=hr+min+sec;

        timeThread=new Thread(){
            @Override
            public void run(){
                try{
                    while(!isInterrupted()){
                        try{
                            timeThread.wait(time);
                            interpreterThread.notify();
                            threadCount-=1;
                            MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.chimesteady);
                            mp.start();
                            timeThread.interrupt();
                        }catch(IllegalMonitorStateException a){}


                    }
                }catch(InterruptedException e){
                    timeThread.interrupt();
                }
            }
        };
        timeThread.start();
    }

    private void populateIntervalList(){
        imdb = new IntervalDBHelper(this);
        Cursor dataCursor = imdb.getDataCursor();

        try {
            while (dataCursor.moveToNext()) {
                intervalList.add(dataCursor.getString(1));
            }
        } finally {
            dataCursor.close();
        }
        Log.d("POPULATE",""+intervalList.size());
        for(int x=0;x<intervalList.size();x++){
            Log.d("POPULATE",""+intervalList.get(x));
        }
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




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
