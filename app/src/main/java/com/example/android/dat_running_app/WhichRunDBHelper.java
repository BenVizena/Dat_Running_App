package com.example.android.dat_running_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.R.attr.version;
import static com.google.android.gms.analytics.internal.zzy.C;
import static com.google.android.gms.analytics.internal.zzy.b;

/**
 * Created by Ben on 8/17/2016.
 */

public class WhichRunDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "which_run_db.db";
    private static final String TABLE_NAME = "WHICH_RUN";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_RUNTYPE = "RUN_TYPE";

    public WhichRunDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RUNTYPE + " TEXT )";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean refreshRunType(String runType){
        deleteRow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_RUNTYPE,runType);
        long success = db.insert(TABLE_NAME,null,value);
        db.close();

        if(success == -1)
            return false;
        else
            return true;
    }

    public String getRunType(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from "+TABLE_NAME;
        Cursor result = db.rawQuery(query,null);
        result.moveToFirst();
        return result.getString(1);
//        return "qwerty";
    }

    private boolean deleteRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_NAME,null,null)>0;
        db.close();
        return result;
    }
}
