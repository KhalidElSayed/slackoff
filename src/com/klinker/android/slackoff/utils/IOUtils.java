package com.klinker.android.slackoff.utils;

import android.content.Context;
import android.database.Cursor;

import com.klinker.android.slackoff.data.SchoolClass;
import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.util.Date;

public class IOUtils {

    /**
     * Function to write a note to a file
     *
     * @param context The applications context
     * @param path The data path to the folder that the note should be written to
     * @return whether the file was written successfully or not
     */
    public boolean writeFile(Context context, String path) {
        return true;
    }

    /**
     * Function to write the file to whatever class is currently running
     * This method will be called from the OverNote service because there won't be a way to set any pathes to the notes
     * @param context Context of the app
     * @return true if it was written successfully
     */
    public boolean writeFile(Context context) {
        String path = getCurrentClassPath(context);
        return writeFile(context, path);
    }

    public String getCurrentClassPath(Context context) {
        String name = "";

        // opens up the school database to read from
        SchoolData data = new SchoolData(context);
        data.open();

        // gets the cursor from that database
        Cursor cursor = data.getCursor();

        if (cursor.moveToFirst()) { // should always be true, because if there are no classes scheduled, then the overnote will never show up and this won't be called, but just in case
            boolean flag = true;

            do {
                // gets the data from the sql table for the class time
                long startTime = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_END_TIME));

                // creates some date objects from those times
                Date startDate = new Date(startTime);
                Date endDate = new Date(endTime);
                Date currDate = new Date();

                if (startDate.before(currDate) && endDate.after(currDate)) {
                    // this is the class that is currently going on
                    // break out of the loop at this spot so we can get the name of the class for the path
                    flag = false;
                }

            } while (flag && cursor.moveToNext());

            name = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_NAME));
        }

        // close the database
        data.close();
        cursor.close();

        // return the path no matter what, if it is still blank, then the note will be placed in the root.
        // it shouldn't be black though or there has been an error with the cursor
        return "/SlackOff/" + name;
    }
}
