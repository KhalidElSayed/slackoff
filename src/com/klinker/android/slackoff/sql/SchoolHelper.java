package com.klinker.android.slackoff.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SchoolHelper extends SQLiteOpenHelper {

    // database information
    public static final String TABLE_HOME = "classes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_START_TIME = "start";
    public static final String COLUMN_END_TIME = "end";
    public static final String COLUMN_DAYS = "days";

    private static final String DATABASE_NAME = "classes.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_HOME + "(" + COLUMN_ID
            + " integer primary key, " + COLUMN_NAME
            + " text class name, " + COLUMN_START_TIME
            + " integer long start time, " + COLUMN_END_TIME
            + " integer long end time, " + COLUMN_DAYS
            + " text days of week);";

    /**
     * creates database to hold school data
     * @param context the activity you are currently in
     */
    public SchoolHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the database
     * @param database database to work with
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    /**
     * called when database is updated
     * @param db the database to work with
     * @param oldVersion the older version code
     * @param newVersion the new version code
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SchoolHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOME);
        onCreate(db);
    }

}