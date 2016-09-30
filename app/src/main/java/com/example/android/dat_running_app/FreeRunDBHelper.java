package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Ben on 7/27/2016.
 *
 * holds the settings for runningscreen display units.
 */

public class FreeRunDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dat_running_app_db.db";
    private static final String TABLE_NAME = "runningscreen_settings_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_METRIC = "METRIC";


    public FreeRunDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_METRIC + " TEXT " +
                ")";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addSetting(String metricBool){//metricBool is either the String "true" or "false"
        ContentValues v = new ContentValues();
        v.put(COLUMN_METRIC,""+"true");
        SQLiteDatabase dbt = getWritableDatabase();
        dbt.insert(TABLE_NAME,null,v);
        dbt.close();
        //the above db entry is to ensure that i don't try to delete on an empty database.

        deleteRow();
        ContentValues values = new ContentValues();
        values.put(COLUMN_METRIC,""+metricBool);
        SQLiteDatabase db = getWritableDatabase();
        long success = db.insert(TABLE_NAME,null,values);
        db.close();


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

    public String getUnitSetting(){//returns true if metric, false if customary
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_NAME;
        Cursor result = db.rawQuery(query, null);

        result.moveToFirst();
        if(result.getCount()==0)
            return "true";
        else{
            return result.getString(1);
        }


    }
}
