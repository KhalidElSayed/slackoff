package com.klinker.android.slackoff.sql;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.klinker.android.slackoff.data.SchoolClass;

import java.util.Date;

/**
 * Helper class for the database holding class information
 *
 * @author Luke Klinker
 */
public class SchoolData {

    /**
     * the actual database we use
     */
    private SQLiteDatabase database;

    /**
     * the helper class to get the database and work with some of it
     */
    private SchoolHelper dbHelper;

    /**
     * the columns we will query on the database
     */
    public String[] allColumns = {SchoolHelper.COLUMN_ID, SchoolHelper.COLUMN_NAME,
            SchoolHelper.COLUMN_START_TIME, SchoolHelper.COLUMN_END_TIME, SchoolHelper.COLUMN_DAYS};

    /**
     * public constructor
     *
     * @param context context of the app
     */
    public SchoolData(Context context) {
        dbHelper = new SchoolHelper(context);
    }

    /**
     * Opens the database to be read or written to
     *
     * @throws SQLException error opening the database
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * closes the database so it can't be changed
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * inserts a new class into the database
     *
     * @param mClass the SchoolClass object that should be written in
     * @return long for the insert id, used to schedule the alarms
     */
    public long addClass(SchoolClass mClass) {
        ContentValues values = new ContentValues();

        // puts the values into the columns
        values.put(SchoolHelper.COLUMN_NAME, mClass.getName());
        values.put(SchoolHelper.COLUMN_START_TIME, mClass.getStart());
        values.put(SchoolHelper.COLUMN_END_TIME, mClass.getEnd());
        values.put(SchoolHelper.COLUMN_DAYS, mClass.getDays());

        // inserts a new column in the database, returning the id
        return database.insert(SchoolHelper.TABLE_HOME, null, values);
    }

    /**
     * Deletes a class from the database
     *
     * @param name The name of the class to be deleted
     */
    public void deleteClass(String name) {

        database.delete(SchoolHelper.TABLE_HOME, // table name
                SchoolHelper.COLUMN_NAME + " = ?", // where clause
                new String[] {name}); // where args
    }

    /**
     * Deletes all the classes in the database
     */
    public void deleteAllClasses() {
        // deletes all the rows of the table
        database.delete(SchoolHelper.TABLE_HOME, null, null);
    }

    /**
     * gets the cursor to access the items in the database
     *
     * @return cursor for the database
     */
    public Cursor getCursor() {

        Cursor cursor = database.query(SchoolHelper.TABLE_HOME, // table name
                allColumns, // projection (columns to query)
                null, // selection clause
                null, // selection args
                null, // where clause
                null, // where args
                SchoolHelper.COLUMN_START_TIME + " DESC"); // order

        return cursor;
    }

    /**
     * method to update the time on the class and write it into the database
     *
     * @param mClass the SchoolClass object that should be updated
     * @return long with the first index as the start time, second index as the end time, and 3rd index as the id
     */
    public long[] updateTime(SchoolClass mClass) {

        long start = 0;
        long end = 0;
        long id = 0;

        // checks to see if it should be rescheduled
        if (mClass.getDays().equals("")) {

            // if not, delete the class from the database
            deleteClass(mClass.getName());
        } else {

            int numDaysToIncrement = 0;

            String days = mClass.getDays();

            // gets what days the class is active
            boolean sunday = days.contains("S ");
            boolean monday = days.contains("M ");
            boolean tuesday = days.contains("T ");
            boolean wednesday = days.contains("W ");
            boolean thursday = days.contains("Th ");
            boolean friday = days.contains("F ");
            boolean saturday = days.contains("Sa ");

            int currentDay = new Date(mClass.getStart()).getDay();

            // uses the current day of the week to find out how many days forward to look for the next instance of the class
            switch (currentDay) {
                case 0: // sunday
                    if (monday)
                        numDaysToIncrement = 1;
                    else if (tuesday)
                        numDaysToIncrement = 2;
                    else if (wednesday)
                        numDaysToIncrement = 3;
                    else if (thursday)
                        numDaysToIncrement = 4;
                    else if (friday)
                        numDaysToIncrement = 5;
                    else if (saturday)
                        numDaysToIncrement = 6;
                    else if (sunday)
                        numDaysToIncrement = 7;
                    break;
                case 1: // monday
                    if (tuesday)
                        numDaysToIncrement = 1;
                    else if (wednesday)
                        numDaysToIncrement = 2;
                    else if (thursday)
                        numDaysToIncrement = 3;
                    else if (friday)
                        numDaysToIncrement = 4;
                    else if (saturday)
                        numDaysToIncrement = 5;
                    else if (sunday)
                        numDaysToIncrement = 6;
                    else if (monday)
                        numDaysToIncrement = 7;
                    break;
                case 2: // tuesday
                    if (wednesday)
                        numDaysToIncrement = 1;
                    else if (thursday)
                        numDaysToIncrement = 2;
                    else if (friday)
                        numDaysToIncrement = 3;
                    else if (saturday)
                        numDaysToIncrement = 4;
                    else if (sunday)
                        numDaysToIncrement = 5;
                    else if (monday)
                        numDaysToIncrement = 6;
                    else if (tuesday)
                        numDaysToIncrement = 7;
                    break;
                case 3: // wednesday
                    if (thursday)
                        numDaysToIncrement = 1;
                    else if (friday)
                        numDaysToIncrement = 2;
                    else if (saturday)
                        numDaysToIncrement = 3;
                    else if (sunday)
                        numDaysToIncrement = 4;
                    else if (monday)
                        numDaysToIncrement = 5;
                    else if (tuesday)
                        numDaysToIncrement = 6;
                    else if (wednesday)
                        numDaysToIncrement = 7;
                    break;
                case 4: // thursday
                    if (friday)
                        numDaysToIncrement = 1;
                    else if (saturday)
                        numDaysToIncrement = 2;
                    else if (sunday)
                        numDaysToIncrement = 3;
                    else if (monday)
                        numDaysToIncrement = 4;
                    else if (tuesday)
                        numDaysToIncrement = 5;
                    else if (wednesday)
                        numDaysToIncrement = 6;
                    else if (thursday)
                        numDaysToIncrement = 7;
                    break;
                case 5: // friday
                    if (saturday)
                        numDaysToIncrement = 1;
                    else if (sunday)
                        numDaysToIncrement = 2;
                    else if (monday)
                        numDaysToIncrement = 3;
                    else if (tuesday)
                        numDaysToIncrement = 4;
                    else if (wednesday)
                        numDaysToIncrement = 5;
                    else if (thursday)
                        numDaysToIncrement = 6;
                    else if (friday)
                        numDaysToIncrement = 7;
                    break;
                case 6: // saturday
                    if (sunday)
                        numDaysToIncrement = 1;
                    else if (monday)
                        numDaysToIncrement = 2;
                    else if (tuesday)
                        numDaysToIncrement = 3;
                    else if (wednesday)
                        numDaysToIncrement = 4;
                    else if (thursday)
                        numDaysToIncrement = 5;
                    else if (friday)
                        numDaysToIncrement = 6;
                    else if (saturday)
                        numDaysToIncrement = 7;
                    break;
            }

            // sets the next start time to the class depending on how many days forward to look
            start = mClass.getStart() + (numDaysToIncrement * AlarmManager.INTERVAL_DAY);
            end = mClass.getEnd() + (numDaysToIncrement * AlarmManager.INTERVAL_DAY);

            // content values to increment
            ContentValues cv = new ContentValues();
            cv.put(SchoolHelper.COLUMN_START_TIME, start);
            cv.put(SchoolHelper.COLUMN_END_TIME, end);

            database.update(SchoolHelper.TABLE_HOME,    // table name
                    cv,                                 // table values
                    SchoolHelper.COLUMN_NAME + " = ?",  // where clause
                    new String[] {mClass.getName()});   // where args
        }

        return new long[] {start, end, id};
    }
}
