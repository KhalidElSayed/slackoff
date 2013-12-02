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

    // Database fields
    private SQLiteDatabase database;
    private SchoolHelper dbHelper;
    public String[] allColumns = { SchoolHelper.COLUMN_ID, SchoolHelper.COLUMN_NAME,
            SchoolHelper.COLUMN_START_TIME, SchoolHelper.COLUMN_END_TIME, SchoolHelper.COLUMN_DAYS };

    public SchoolData(Context context) {
        dbHelper = new SchoolHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addClass(SchoolClass mClass) {
        ContentValues values = new ContentValues();

        values.put(SchoolHelper.COLUMN_NAME, mClass.getName());
        values.put(SchoolHelper.COLUMN_START_TIME, mClass.getStart());
        values.put(SchoolHelper.COLUMN_END_TIME, mClass.getEnd());
        values.put(SchoolHelper.COLUMN_DAYS, mClass.getDays());

        database.insert(SchoolHelper.TABLE_HOME, null, values);
    }

    public void deleteClass(String name) {

        database.delete(SchoolHelper.TABLE_HOME, SchoolHelper.COLUMN_NAME
                + " = " + name, null);
    }

    public void deleteAllClasses() {
        database.delete(SchoolHelper.TABLE_HOME, null, null);
    }

    public Cursor getCursor() {

        Cursor cursor = database.query(SchoolHelper.TABLE_HOME,
                allColumns, null, null, null, null, SchoolHelper.COLUMN_START_TIME + " DESC");

        return cursor;
    }
}
