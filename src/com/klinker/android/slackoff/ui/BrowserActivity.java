package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.ClassesCursorAdapter;
import com.klinker.android.slackoff.adapter.FileListAdapter;
import com.klinker.android.slackoff.data.NoteFile;
import com.klinker.android.slackoff.data.SchoolClass;
import com.klinker.android.slackoff.service.OverNoteService;
import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.utils.IOUtils;
import com.klinker.android.slackoff.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main activity for app, which is a simple file browser
 *
 * @author Jake and Luke Klinker
 */
public class BrowserActivity extends Activity {

    /**
     * The listview to hold all folders in the parent file
     */
    private ListView folderList;

    /**
     * The adapter for the folder list view
     */
    private FileListAdapter folderAdapter;

    /**
     * The listview (or gridview depending on orientation) for the files in the parent file
     */
    private AbsListView fileList;

    /**
     * The adapter for the file list view
     */
    private FileListAdapter fileAdapter;

    /**
     * The drawer layout
     */
    private DrawerLayout mDrawerLayout;

    /**
     * Controls the toggle for when the drawer is open
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * whether the device is in portrait mode or not
     */
    private boolean portrait;

    /**
     * All the files in the current directory
     */
    private ArrayList<NoteFile> files;

    /**
     * all of the folders in the current directory
     */
    private ArrayList<NoteFile> folders;

    /**
     * The parent file where all files and folders will be taken from
     */
    private File parent;

    /**
     * First step in an activity lifecycle which creates the views
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the current view
        setContentView(R.layout.activity_main);

        // initialize the files I need from the main view
        folderList = (ListView) findViewById(R.id.folderList);
        fileList = (AbsListView) findViewById(R.id.fileList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // check whether we are using a portrait or landscape orientation for later on
        portrait = folderList.getTag().equals("portrait");

        // initialize the array lists
        files = new ArrayList<NoteFile>();
        folders = new ArrayList<NoteFile>();

        // check if a parent file is sent into the activity through the intent, and if so, set that as the parent
        // if not, set the main external directory as the parent
        if (getIntent().getStringExtra("parent_file") != null) {
            parent = new File(getIntent().getStringExtra("parent_file"));
            setTitle(parent.getName());
            getActionBar().setDisplayHomeAsUpEnabled(true);

            // lock the drawer so that it can't be opened when not on top level
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            parent = new File(Environment.getExternalStorageDirectory().getPath(), "SlackOff");

            if (!parent.exists()) {
                parent.mkdir();
            }

            // initialize the drawer layout since we are on the top level
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

                // drawer is fully closed
                public void onDrawerClosed(View view) {
                    String name = parent.getName();
                    if (!name.equals("0"))
                        getActionBar().setTitle(parent.getName());
                }

                // drawer is fully open
                public void onDrawerOpened(View drawerView) {
                    getActionBar().setTitle(getResources().getString(R.string.app_name));
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            // shows the drawer icon on the action bar
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

        }

        // get a list of all files in the parent directory and sort them in alphabetical order
        File[] dirFiles = parent.listFiles();
        Arrays.sort(dirFiles, fileComparator);

        // add sorted files to the correct array list to be used in adapters
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                folders.add(new NoteFile(file));
            } else {
                if (file.getName().endsWith(".klink")) {
                    files.add(new NoteFile(file));
                }
            }
        }

        // initialize adapters with the files from the directory
        folderAdapter = new FileListAdapter(BrowserActivity.this, folders, true);
        fileAdapter = new FileListAdapter(BrowserActivity.this, files, false);

        // set up layout stuff based on orientation
        if (portrait) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) folderList.getLayoutParams();
            params.height = Utils.toDP(BrowserActivity.this, 49 * (folderAdapter.getCount()) + 25);
            folderList.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) fileList.getLayoutParams();
            params.height = Utils.toDP(BrowserActivity.this, 49 * (fileAdapter.getCount()) + 25);
            fileList.setLayoutParams(params);

            // hide and show different views depending on how many files are in a directory
            if (folders.size() == 0) {
                findViewById(R.id.divider).setVisibility(View.GONE);
                findViewById(R.id.none).setVisibility(View.VISIBLE);
            }

            if (files.size() == 0) {
                findViewById(R.id.divider2).setVisibility(View.GONE);
                findViewById(R.id.none2).setVisibility(View.VISIBLE);
            }
        }

        // show the header for the folders
        View folderHeader = getLayoutInflater().inflate(R.layout.list_header, null, false);
        ((TextView) folderHeader.findViewById(R.id.headerText)).setText(getString(R.string.folder));
        folderList.addHeaderView(folderHeader);

        // show the header for the files (depends on whether we are using a listview which supports headers, or a gridview
        // which does not
        if (fileList instanceof ListView) {
            View fileHeader = getLayoutInflater().inflate(R.layout.list_header, null, false);
            ((TextView) fileHeader.findViewById(R.id.headerText)).setText(getString(R.string.file));
            ((ListView) fileList).addHeaderView(fileHeader);
        } else {
            ((TextView) findViewById(R.id.fileHeader).findViewById(R.id.headerText)).setText(getString(R.string.file));
            int padding = Utils.toDP(BrowserActivity.this, 10);
            int padding2 = Utils.toDP(BrowserActivity.this, 7);
            folderHeader.setPadding(padding2, 0, padding2, padding);
        }

        // set the adapters for the lists
        folderList.setAdapter(folderAdapter);
        fileList.setAdapter(fileAdapter);

        // advance to the next folder if you click on one (be sure to set the parent_file in the intent
        folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    Intent nextFolder = new Intent(BrowserActivity.this, BrowserActivity.class);
                    nextFolder.putExtra("parent_file", folders.get(i - 1).getPath());
                    startActivity(nextFolder);
                }
            }
        });

        // parse the mimetype of the file and find the correct app on your device to open that file (or none if no
        // correct app exists)
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // account for the header view
                if (portrait) {
                    i--;
                }

                if (i >= 0) {
                    Intent fileIntent = new Intent(Intent.ACTION_VIEW);
                    Log.v("mime_type", Utils.getMimeType(files.get(i).getPath()));
                    fileIntent.setDataAndType(Uri.fromFile(files.get(i).getFile()), Utils.getMimeType(files.get(i).getPath()));
                    fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    try {
                        startActivity(fileIntent);
                    } catch (Exception e) {
                        Toast.makeText(BrowserActivity.this, getString(R.string.no_activities), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // handles long clicks on files
        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                return longClickAction(files.get(portrait ? position - 1 : position));
            }
        });

        // handles long clicks on folders
        folderList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return longClickAction(folders.get(i - 1));
            }
        });

        // open our sqlite database
        SchoolData data = new SchoolData(this);
        data.open();

        // create the necessary adapter for the drawer and set a footer
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        View footer = getLayoutInflater().inflate(R.layout.add_class, null, false);
        drawerList.addFooterView(footer);
        drawerList.setAdapter(new ClassesCursorAdapter(this, data.getCursor()));

        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new class

                // creates a dialog from the new class dialog xml file
                final Dialog dialog = new Dialog(BrowserActivity.this);
                dialog.setContentView(R.layout.date_dialog);
                // sets the title
                dialog.setTitle(getResources().getString(R.string.next_instance_of_class));

                // gets the elements
                final DatePicker date = (DatePicker) dialog.findViewById(R.id.datePicker);
                final TimePicker startTime = (TimePicker) dialog.findViewById(R.id.startTimePicker);
                final TimePicker endTime = (TimePicker) dialog.findViewById(R.id.endTimePicker);
                final EditText name = (EditText) dialog.findViewById(R.id.class_name);
                Button save = (Button) dialog.findViewById(R.id.save);
                Button cancel = (Button) dialog.findViewById(R.id.cancel);

                // sets the save listener
                save.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // get the data
                        int month = date.getMonth();
                        int day = date.getDayOfMonth();
                        int year = date.getYear();
                        int startHour = startTime.getCurrentHour();
                        int startMinute = startTime.getCurrentMinute();
                        int endHour = endTime.getCurrentHour();
                        int endMinute = endTime.getCurrentMinute();

                        Date setDate = new Date(year,month,day,startHour,startMinute);
                        final long setTime = setDate.getTime();

                        setDate = new Date(year,month,day,endHour,endMinute);
                        final long endTime = setDate.getTime();

                        // dismiss the date picker dialog
                        dialog.dismiss();

                        // Open the repeat dialog
                        final Dialog repeat = new Dialog(BrowserActivity.this);
                        repeat.setContentView(R.layout.repeated_dialog);
                        repeat.setTitle(getResources().getString(R.string.set_days));

                        Button repeatSave = (Button) repeat.findViewById(R.id.save);

                        repeatSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                boolean sunday = ((CheckBox) repeat.findViewById(R.id.sunday)).isChecked();
                                boolean monday = ((CheckBox) repeat.findViewById(R.id.monday)).isChecked();
                                boolean tuesday = ((CheckBox) repeat.findViewById(R.id.tuesday)).isChecked();
                                boolean wednesday = ((CheckBox) repeat.findViewById(R.id.wednesday)).isChecked();
                                boolean thursday = ((CheckBox) repeat.findViewById(R.id.thursday)).isChecked();
                                boolean friday = ((CheckBox) repeat.findViewById(R.id.friday)).isChecked();
                                boolean saturday = ((CheckBox) repeat.findViewById(R.id.saturday)).isChecked();
                            }
                        });

                        repeat.show();
                    }
                });

                // sets the cancel listener
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // do nothing
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        //data.addClass(new SchoolClass("Circuits", new Date().getTime(), new Date().getTime() + 1000 * 60 * 60, "M W F"));

        // starts  the service
        // TODO: check if they have a class going on before starting it
        startService(new Intent(this, OverNoteService.class));
    }

    /**
     * Sets the drawer state
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after the config changes
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    /**
     * Called when the orientation changes
     * @param newConfig switches orientation
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /**
     * Handles all long clicks on a file or folder
     * @param noteFile is the file which we are acting on
     * @return true once dialog has been shown
     */
    private boolean longClickAction(final NoteFile noteFile) {
        new AlertDialog.Builder(BrowserActivity.this)
                .setTitle(R.string.file_options)
                .setItems(R.array.long_click_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                final EditText textEntry = new EditText(BrowserActivity.this);
                                textEntry.setText(noteFile.getName());
                                textEntry.setSelection(0, textEntry.getText().toString().length());

                                new AlertDialog.Builder(BrowserActivity.this)
                                        .setView(textEntry)
                                        .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                File file = noteFile.getFile();
                                                file.renameTo(new File(file.getParent(), textEntry.getText().toString()));
                                                recreate();
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, null)
                                        .show();
                                break;
                            case 1:
                                new AlertDialog.Builder(BrowserActivity.this)
                                        .setMessage(R.string.are_you_sure)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Utils.deleteDirectory(noteFile.getFile());
                                                recreate();
                                            }
                                        })
                                        .setNegativeButton(R.string.no, null)
                                        .show();
                                break;
                        }
                    }
                })
                .show();

        return true;
    }

    /**
     * the comparator used to determine alphabetical order of files when they are being sorted
     */
    Comparator<File> fileComparator = new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory()) {
                if (file2.isDirectory()) {
                    return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
                } else {
                    return -1;
                }
            } else {
                if (file2.isDirectory()) {
                    return 1;
                } else {
                    return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
                }
            }

        }
    };

    /**
     * Acts when the option item has been selected
     * @param item the selected item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            // stop now if it was the drawer toggle that was hit
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_add:
                // get the view to display
                final View v = getLayoutInflater().inflate(R.layout.create_dialog, null, false);
                new AlertDialog.Builder(BrowserActivity.this)
                        .setTitle(R.string.create_new)
                        .setView(v)
                        .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // ensure that the file name is filled in
                                if (((EditText) v.findViewById(R.id.fileName)).getText().toString().equals("")) {
                                    Toast.makeText(BrowserActivity.this, getString(R.string.error_creating_file), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (((RadioButton) v.findViewById(R.id.folderButton)).isChecked()) {
                                    // we are creating a folder...
                                    File file = new File(parent, ((EditText) v.findViewById(R.id.fileName)).getText().toString());

                                    if (!file.exists()) {
                                        file.mkdir();
                                    } else {
                                        Toast.makeText(BrowserActivity.this, getString(R.string.file_exists), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // we are creating a note file...
                                    try {
                                        File file = new File(parent, ((EditText) v.findViewById(R.id.fileName)).getText().toString() + ".klink");
                                        if (!file.exists()) {
                                            file.createNewFile();
                                        } else {
                                            Toast.makeText(BrowserActivity.this, getString(R.string.file_exists), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // :( something went wrong creating our note

                                        Toast.makeText(BrowserActivity.this, getString(R.string.error_creating_file), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                recreate();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attaches the options menu to the activity
     * @param menu the menu to attach
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_activity, menu);
        return true;
    }
}
