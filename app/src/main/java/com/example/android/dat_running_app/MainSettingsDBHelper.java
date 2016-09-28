package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.google.android.gms.analytics.internal.zzy.l;

/**
 * Created by Ben on 9/16/2016.
 */

public class MainSettingsDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "main_settings.db";
    private static final String TABLE_NAME = "mass_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_MASS = "MASS";

    public MainSettingsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "Create table " + TABLE_NAME + "(" +
                COLUMN_ID + " integer primary key autoincrement, " +
                COLUMN_MASS + " text )";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addMass(String mass){
        deleteRow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_MASS,mass);
        long success = db.insert(TABLE_NAME,null,value);
        db.close();

        if(success == -1)
            return false;
        else
            return true;
    }

    public String getMass(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+TABLE_NAME;
        Cursor result = db.rawQuery(query,null);
        result.moveToFirst();
        db.close();
        return result.getString(1);
    }

    private boolean deleteRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_NAME,null,null)>0;
        db.close();
        return result;
    }

    public boolean hasMass(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+TABLE_NAME;
        Cursor result = db.rawQuery(query,null);
   //     db.close();
        boolean r = result.moveToFirst();
        db.close();
        return r;
    }
}
