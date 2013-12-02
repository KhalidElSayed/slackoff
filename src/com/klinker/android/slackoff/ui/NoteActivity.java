package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity to handle all notes that are opened in the app (have an extension of .klink and this activity is registered
 * in the manifest to open .klink files, which are our notes)
 *
 * @author Jake and Luke Klinker
 */
public class NoteActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
