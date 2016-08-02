package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.version;


/**
 * Created by Ben on 7/27/2016.
 */

public class FreeRunDBHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dat_running_app_db.db";
    public static final String TABLE_NAME = "freerun_settings_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SHOWTIME = "SHOW_TIME";
    public static final String COLUMN_SHOWDISTANCE = "SHOW_DISTANCE";


    public FreeRunDBHelper(Context context) {//, String name, SQLiteDatabase.CursorFactory factory, int version
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SHOWTIME + " TEXT, " +
                COLUMN_SHOWDISTANCE + " TEXT " +
                ")";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addSettings(boolean showTotalTime, boolean showTotalDistance){
        deleteRow();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOWTIME,""+showTotalTime);
        values.put(COLUMN_SHOWDISTANCE,""+showTotalDistance);
        SQLiteDatabase db = getWritableDatabase();
        long success = db.insert(TABLE_NAME,null,values);
        db.close();

        Log.d("degub", ""+success);

        if(success == -1)
            return false;
        else
            return true;

    }

    private boolean deleteRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_NAME,null,null)>0;
        db.close();
        return result;
    }

    public Cursor getFRSettings(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_NAME;
        Cursor result = db.rawQuery(query, null);
//        if (!result.moveToFirst())
//           result.moveToFirst();
 //       Log.d("RESULTS",".getInt(0): "+result.getInt(0)+" .getString(1): "+result.getString(1)+" .getString(2): "+result.getString(2)+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        db.close();
        return result;
    }
}
