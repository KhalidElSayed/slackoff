package com.klinker.android.slackoff.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.klinker.android.slackoff.data.SchoolClass;

/**
 * Helper class for the database holding class information
 *
 * @author Jake and Luke Klinker
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

    public long[] updateTime(SchoolClass mClass) {

        if (mClass.getDays().equals("")) {
            deleteClass(mClass.getName());
        }

        long start = 0;
        long end = 0;
        long id = 0;

        return new long[] {start, end, id};
    }
}
