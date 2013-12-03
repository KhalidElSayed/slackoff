package com.klinker.android.slackoff.ui;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.utils.Utils;

import java.io.File;

/**
 * Activity to handle all notes that are opened in the app (have an extension of .klink and this activity is registered
 * in the manifest to open .klink files, which are our notes)
 *
 * @author Jake and Luke Klinker
 */
public class NoteActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        // get the uri passed through the intent for the opened file so we know what to display
        Uri fileUri = getIntent().getData();
        File file = new File(fileUri.getPath());

        // set the activities name to the title of the file
        setTitle(file.getName().replace(Utils.EXTENSION, " " + getString(R.string.notes_file)));

        // add the footer view to the bottom of the list so that there is always a way to add another bullet point
        getListView().addFooterView(getLayoutInflater().inflate(R.layout.note_item, null, false));
    }
}
