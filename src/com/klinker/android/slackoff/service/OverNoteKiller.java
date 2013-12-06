package com.klinker.android.slackoff.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.klinker.android.slackoff.data.SchoolClass;
import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.util.Date;

/**
 * Service to kill and schedule the next alarm for the class
 *
 * @author Luke and Jake Klinker
 */
public class OverNoteKiller extends IntentService {

    /**
     * public contructor
     */
    public OverNoteKiller() {
        super("OverNoteKillerService");
    }

    /**
     * called when the service is started
     *
     * @param intent the calling intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();

        // stops the service at the end of the class
        Intent killer = new Intent("com.klinker.android.notes.STOP_NOTES");
        sendBroadcast(killer);

        SchoolClass currClass = getCurrentClass(getApplicationContext());

        // updates to the next time in the sql database
        SchoolData data = new SchoolData(getApplicationContext());
        data.open();

        // 0 will be the new start time, 1 will have the new end time, 2 will have the id
        long[] newTimes = data.updateTime(currClass);

        data.close();

        // schedules the next alarm for the next class
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, (int) newTimes[2], new Intent(context, OverNoteService.class), 0);
        am.set(AlarmManager.RTC_WAKEUP, newTimes[0], pendingIntent);

        // schedule the killer alarm
        PendingIntent killerServ = PendingIntent.getService(context, (int) newTimes[2] + 1, new Intent(context, OverNoteKiller.class), 0);
        am.set(AlarmManager.RTC_WAKEUP, newTimes[1], killerServ);
    }

    /**
     * Function to get the name of the ongoing class and return it as a string
     *
     * @param context Context of the app
     * @return ongoing class as a path
     */
    public static SchoolClass getCurrentClass(Context context) {
        String name = "";
        String days = "";
        long start = 0;
        long end = 0;

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

            cursor.moveToPrevious();

            name = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_NAME));
            days = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_DAYS));
            end = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_END_TIME));
            start = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_START_TIME));
        }

        // close the database
        data.close();
        cursor.close();

        SchoolClass mClass = new SchoolClass(name, start, end, days);

        // return the path no matter what, if it is still blank, then the note will be placed in the root.
        // it shouldn't be black though or there has been an error with the cursor
        return mClass;
    }
}
