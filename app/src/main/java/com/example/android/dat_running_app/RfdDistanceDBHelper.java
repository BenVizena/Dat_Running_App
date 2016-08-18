package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import static android.R.attr.value;
//import static android.R.attr.version;

/**
 * Created by Ben on 8/17/2016.
 */

public class RfdDistanceDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "distance_run_distance.db";
    private static final String TABLE_NAME = "DISTANCE_RUN_DISTANCE";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DISTANCETOGO = "DISTANCE_TO_GO";



    public RfdDistanceDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DISTANCETOGO + " TEXT )";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addSettings(String distance){
        deleteRow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_DISTANCETOGO,distance);
        long success = db.insert(TABLE_NAME,null,value);
        db.close();

        if(success == -1)
            return false;
        else
            return true;

    }

    public String getRunDistance(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+TABLE_NAME;
        Cursor result = db.rawQuery(query,null);
        result.moveToFirst();
        Log.d("!!!!!!!!!!",result.getCount()+"COUNTCOUNTCOUNT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d("!!!!!!!!!!",result.getString(0)+"0000000000000000000000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d("!!!!!!!!!!",result.getString(1)+"111111111111111111!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return result.getString(1);
    }

    private boolean deleteRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_NAME,null,null)>0;
        db.close();
        return result;
    }
}
