package com.klinker.android.slackoff.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.io.*;
import java.util.Date;

public class IOUtils {

    /**
     * Function to write a note to a file
     *
     * @param text The text to write to the file
     * @param title the title of the note
     * @param className The data path to the folder that the note should be written to
     * @return whether the file was written successfully or not
     */
    public static boolean writeFile(String className, String text, String title) {
        try {
            // creates the file and the directory if it isn't there yet
            File mText = new File(Environment.getExternalStorageDirectory() + "/SlackOff/" + className + "/" + title.replaceAll(" ", "_") + Utils.EXTENSION);
            File dir = new File(Environment.getExternalStorageDirectory() + "/SlackOff/" + className);
            dir.mkdirs();

            // will overwrite the original file if it exists
            if (mText.exists()) {
                mText.delete();
            }

            // creates the output stream and writes to the file we made
            OutputStreamWriter output;
            output = new OutputStreamWriter(new FileOutputStream(mText, true));

            output.write(title + "\n\n\n");
            output.append(text);

            output.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function to write the file to whatever class is currently running
     * This method will be called from the OverNote service because there won't be a way to set any pathes to the notes
     *
     * @param context Context of the app
     * @return true if it was written successfully
     */
    public static boolean writeFile(Context context, String text, String title) {
        String path = getCurrentClass(context);
        return writeFile(path, text, title);
    }

    /**
     * Function to read the text in a file and return that text as a single string to act accordingly on
     *
     * @param file the file to read
     * @return a string of the files contents
     */
    public static String readFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            String text = "";

            while ((line = reader.readLine()) != null) {
                text += line + "\n";
            }

            reader.close();

            return text;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Function to get the name of the ongoing class and return it as a string
     *
     * @param context Context of the app
     * @return ongoing class as a path
     */
    public static String getCurrentClass(Context context) {
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
        return name;
    }
}
