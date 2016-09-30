package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Ben on 8/17/2016.
 *
 * keeps track of intervals entered by user.
 */

public class IntervalDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "intervaldb.db";
    private static final String TABLE_NAME = "interval";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_INTERVALSTRING = "INTERVAL_STRING";


    public IntervalDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_INTERVALSTRING + " TEXT )";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addSettings(String interval){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_INTERVALSTRING,interval);
        long success = db.insert(TABLE_NAME,null,value);
        db.close();

        if(success == -1)
            return false;
        else
            return true;

    }

    public Cursor getDataCursor(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+TABLE_NAME;
        Cursor result = db.rawQuery(query,null);
        result.moveToFirst();
        db.close();
        return result;
    }

    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "delete from "+ TABLE_NAME;
        db.execSQL(query);
        db.close();
    }


}
