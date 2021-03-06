package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.version;
import static com.google.android.gms.analytics.internal.zzy.i;
import static com.google.android.gms.analytics.internal.zzy.s;


/**
 * Created by Ben on 8/4/2016.
 *
 * the main db of the app.  Stores all of the information about the runs.
 * runs are differentiated by their start time.
 */

public class RunDBHelper extends SQLiteOpenHelper {
    /*
        Runs are distinguished by their start times.
     */

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "run_db.db";
    private static final String TABLE_NAME = "RUN_TABLE";
    private static final String COLUMN_ID = "_ID";
    private static final String COLUMN_RUNTYPE = "RUN_TYPE";
    private static final String COLUMN_STARTTIME = "START_TIME";
    private static final String COLUMN_CURRENTTIME = "CURRENT_TIME";
    private static final String COLUMN_CURRENTDISTANCE = "CURRENT_DISTANCE";
    private static final String COLUMN_CURRENTPACE = "CURRENT_PACE";
    private static final String COLUMN_CURRENTSPEED = "CURRENT_SPEED";
    private static final String COLUMN_CURRENTCADENCE="CURRENT_CADENCE";
    private static final String COLUMN_CURRENTELEVATION = "CURRENT_ELEVATION";
    private static String DB_PATH = "";



    public RunDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if(android.os.Build.VERSION.SDK_INT >= 4.2){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
   //     this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RUNTYPE + " TEXT, " +
                COLUMN_STARTTIME + " TEXT, " +
                COLUMN_CURRENTTIME + " TEXT, " +
                COLUMN_CURRENTDISTANCE + " TEXT, " +
                COLUMN_CURRENTPACE + " TEXT, " +
                COLUMN_CURRENTSPEED + " TEXT, " +
                COLUMN_CURRENTCADENCE + " TEXT, " +
                COLUMN_CURRENTELEVATION + " TEXT )";

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addUpdate(String runType, String startTime, String currentTime, String currentDistance, String currentPace, String currentSpeed, String currentCadence, String currentElevation){
        ContentValues values = new ContentValues();
        values.put(COLUMN_RUNTYPE, runType);//1
        values.put(COLUMN_STARTTIME, startTime);//2
        values.put(COLUMN_CURRENTTIME, currentTime);//3
        values.put(COLUMN_CURRENTDISTANCE, currentDistance);//4
        values.put(COLUMN_CURRENTPACE, currentPace);//5
        values.put(COLUMN_CURRENTSPEED, currentSpeed);//6
        values.put(COLUMN_CURRENTCADENCE, currentCadence);//7
        values.put(COLUMN_CURRENTELEVATION, currentElevation);//8

        SQLiteDatabase db = getWritableDatabase();
        long success = db.insert(TABLE_NAME,null,values);
        db.close();

        getFRData();

        if(success == -1)
            return false;
        else
            return true;
    }

    public Cursor getFRData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_NAME;
        Cursor result = db.rawQuery(query, null);

        result.moveToFirst();
        db.close();
        return result;
    }

    public Cursor getFRDataReverse(long start){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" ORDER BY "+COLUMN_ID+" DESC LIMIT "+start+", 1";
        Cursor result = db.rawQuery(query, null);

        result.moveToFirst();
        db.close();
        return result;
    }

    public Cursor getDataByStartTimeEpoch(String startTime, long start){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from "+TABLE_NAME+" Where "+COLUMN_STARTTIME +" == " +startTime +" Order by "+COLUMN_ID;
        Cursor result = db.rawQuery(query, null);

        db.close();
        result.moveToFirst();
        return result;
    }
}
