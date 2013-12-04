package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.NoteItemAdapter;
import com.klinker.android.slackoff.utils.IOUtils;
import com.klinker.android.slackoff.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Activity to handle all notes that are opened in the app (have an extension of .klink and this activity is registered
 * in the manifest to open .klink files, which are our notes)
 *
 * @author Jake and Luke Klinker
 */
public class NoteActivity extends Activity {

    /**
     * Constant to read at the start of the note to check checkable
     */
    public static final String CHECKABLE = "-[1]-";

    /**
     * The list view to display our notes
     */
    private ListView list;

    /**
     * the adapter for our listview
     */
    private NoteItemAdapter adapter;

    /**
     * the notes from the file
     */
    private ArrayList<String> notes;

    /**
     * edit text box for holding the notes title
     */
    private EditText title;

    /**
     * sets whether or not the items in the list view should have checkboxes next to them
     */
    private boolean checkable;

    /**
     * Holds the location of the note to overwrite later
     */
    private String pathToNote;

    /**
     * The first step of the activity lifecycle which will set up our view and initialize everything that we need
     * for the activity
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        // get the uri passed through the intent for the opened file so we know what to display
        Uri fileUri = getIntent().getData();
        pathToNote = fileUri.getPath();
        File file = new File(pathToNote);

        // set the activities name to the title of the file
        setTitle(file.getName().replace(Utils.EXTENSION, " " + getString(R.string.notes_file)));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        title = (EditText) findViewById(R.id.title);

        // get the notes for the specified file
        String note = IOUtils.readFile(file);

        if (!note.equals("")) {
            // this is a previously created note
            if (note.startsWith(CHECKABLE)) {
                checkable = true;
                note.replace(CHECKABLE, "");
            } else {
                checkable = false;
            }

            // process the title
            String titleText = note.substring(0, note.indexOf("\n"));
            title.setText(titleText);

            note = note.replace("\n\n\n", "").substring(titleText.length());

            notes = new ArrayList<String>(Arrays.asList(note.split("\n")));
        } else {
            // this is a new note with nothing in it
            checkable = false;
            notes = new ArrayList<String>();
            notes.add("");

            title.setText(file.getName().replace(Utils.EXTENSION, " " + getString(R.string.notes_file)));
        }

        // set up the listview stuff
        // add the footer view to the bottom of the list so that there is always a way to add another bullet point
        list = (ListView) findViewById(R.id.listView);
        adapter = new NoteItemAdapter(this, notes, checkable);
        list.setAdapter(adapter);
        list.setItemsCanFocus(true);

        findViewById(R.id.new_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.addLine();
                list.smoothScrollToPosition(adapter.getCount());
            }
        });

        findViewById(R.id.attach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO create image chooser to insert here
            }
        });
    }

    /**
     * Saves our edited note to a file
     */
    private void saveNote() {
        String note = "";

        for (String n : adapter.getNotes()) {
            note += n + "\n";
        }

        boolean success = IOUtils.writeToPath(pathToNote, note, (checkable ? CHECKABLE : "") + title.getText().toString());
        Toast.makeText(this, getString(success ? R.string.saved_successfully : R.string.error_saving), Toast.LENGTH_LONG)
                .show();
    }

    /**
     * Acts when the option item has been selected
     *
     * @param item the selected item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_save:
                saveNote();
                return true;
            case R.id.menu_checkable:
                item.setChecked(!item.isChecked());
                checkable = item.isChecked();
                adapter.setCheckBoxes(checkable);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attaches the options menu to the activity
     *
     * @param menu the menu to attach
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_activity, menu);
        return true;
    }

    /**
     * Controls back button behavior
     */
    @Override
    public void onBackPressed() {
        saveNote();
        super.onBackPressed();
    }
}
