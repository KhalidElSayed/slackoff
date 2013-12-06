package com.klinker.android.slackoff.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.util.Date;

/**
 * Broadcast that is captured when the phone is rebooted
 * need so we can reschedule the classes and their killers because the alarms are killed when rebooted.
 *
 * @author Luke Klinker
 */
public class BootReceiver extends BroadcastReceiver {

    /**
     * Method called when the broadcast is received
     *
     * @param context context of the application
     * @param intent intent that called the broadcast
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // gets the sql database and opens it
        SchoolData data = new SchoolData(context);
        data.open();

        // gets the cursor for the database
        Cursor c = data.getCursor();

        // checks if there is data in the database
        if (c.moveToFirst()) {
            do {
                // gets the data from the elements
                long startTime = c.getLong(c.getColumnIndex(SchoolHelper.COLUMN_START_TIME));
                long endTime = c.getLong(c.getColumnIndex(SchoolHelper.COLUMN_END_TIME));
                long id = c.getLong(c.getColumnIndex(SchoolHelper.COLUMN_ID));

                // gets the alarm manager service
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                // creates the pending intent for the service
                PendingIntent pendingIntent = PendingIntent.getService(context, // context
                        (int) id, // id
                        new Intent(context, OverNoteService.class), // intent
                        0); // extra flags (none here)

                // schedules the alarm
                am.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);

                // schedule the killer alarm
                PendingIntent killerServ = PendingIntent.getService(context,
                        (int) id + 1,
                        new Intent(context, OverNoteKiller.class),
                        0);

                am.set(AlarmManager.RTC_WAKEUP, endTime, killerServ);

            } while (c.moveToNext());
        }

    }
}