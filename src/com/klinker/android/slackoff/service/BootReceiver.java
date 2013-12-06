package com.klinker.android.slackoff.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast that is captured when the phone is rebooted
 * need so we can reschedule the classes and their killers because the alarms are killed when rebooted.
 *
 * @author Luke Klinker
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}