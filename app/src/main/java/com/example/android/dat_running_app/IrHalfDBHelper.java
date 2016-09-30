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
 *
 * holds the users selections for the interval constraints (e.g. Distance, then Time)
 */

public class IrHalfDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ir_half_db.db";
    private static final String TABLE_NAME = "irHalfSelection";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_FIRSTHALF = "FIRST_HALF";
    private static final String COLUMN_SECONDHALF = "SECOND_HALF";
    private static final String COLUMN_UNIT = "UNIT";



    public IrHalfDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FIRSTHALF + " TEXT, " +
                COLUMN_SECONDHALF + " TEXT, " +
                COLUMN_UNIT + " TEXT )";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addSettings(String firstHalf, String secondHalf, String unit){
        deleteRow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_FIRSTHALF,firstHalf);
        value.put(COLUMN_SECONDHALF,secondHalf);
        value.put(COLUMN_UNIT, unit);
        long success = db.insert(TABLE_NAME,null,value);
        db.close();

        if(success == -1)
            return false;
        else
            return true;

    }

    public String getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+TABLE_NAME;
        Cursor result = db.rawQuery(query,null);
        result.moveToFirst();
        return result.getString(1) + " " + result.getString(2)+" "+result.getString(3);
    }

    private boolean deleteRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_NAME,null,null)>0;
        db.close();
        return result;
    }
}