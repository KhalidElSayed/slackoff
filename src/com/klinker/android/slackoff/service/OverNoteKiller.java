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
import com.klinker.android.slackoff.utils.Utils;

import java.util.Date;

/**
 * Service to kill and schedule the next alarm for the class
 *
 * @author Luke Klinker
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

        SchoolClass currClass = Utils.getCurrentClass(getApplicationContext());

        // updates to the next time in the sql database
        SchoolData data = new SchoolData(getApplicationContext());
        data.open();

        // 0 will be the new start time, 1 will have the new end time, 2 will have the id
        long[] newTimes = data.updateTime(currClass);

        data.close();

        if (newTimes[0] != 0) {
            // schedules the next alarm for the next class
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pendingIntent = PendingIntent.getService(context, // context
                    (int) newTimes[2], // id
                    new Intent(context, OverNoteService.class), // intent
                    0); // extra flags (none here)

            am.set(AlarmManager.RTC_WAKEUP, newTimes[0], pendingIntent);

            // schedule the killer alarm
            PendingIntent killerServ = PendingIntent.getService(context,
                    (int) newTimes[2] + 1,
                    new Intent(context, OverNoteKiller.class),
                    0);

            am.set(AlarmManager.RTC_WAKEUP, newTimes[1], killerServ);
        }
    }
}
