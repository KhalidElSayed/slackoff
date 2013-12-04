package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Activity to handle all notes that are opened in the app (have an extension of .klink and this activity is registered
 * in the manifest to open .klink files, which are our notes)
 *
 * @author Jake and Luke Klinker
 */
public class NoteActivity extends Activity {

    /**
     * request code for picking an image to attach to the note
     */
    private static final int GET_IMAGE = 1209;

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
                note = note.replace(CHECKABLE, "");
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

            title.setText(file.getName().replace(Utils.EXTENSION, ""));
        }

        // set up the listview stuff
        // add the footer view to the bottom of the list so that there is always a way to add another bullet point
        list = (ListView) findViewById(R.id.listView);
        adapter = new NoteItemAdapter(this, notes, checkable, pathToNote);
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
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, GET_IMAGE);
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
            case R.id.menu_delete:
                IOUtils.deleteDirectory(new File(pathToNote));
                finish();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendBroadcast(new Intent(BrowserActivity.REFRESH_BROADCAST));
                    }
                }, 250);
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
     * sets up the checkable option in the menu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_checkable).setChecked(checkable);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GET_IMAGE:
                if (resultCode == RESULT_OK) {
                    try {
                        // get the bitmap that was returned
                        Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                        // save that bitmap to our sd card in the current location
                        // with the file name of the current time in millis
                        String fileName = Calendar.getInstance().getTimeInMillis() + ".jpg";
                        FileOutputStream fos = new FileOutputStream(new File(pathToNote).getParent() + "/" + fileName);
                        image.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                        // add a new line to our note adapter and refresh
                        adapter.addImage(fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // :(
                    }
                }

                break;
        }
    }
}
