package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.FileListAdapter;
import com.klinker.android.slackoff.data.NoteFile;
import com.klinker.android.slackoff.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

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
        } else {
            parent = Environment.getExternalStorageDirectory();
        }

        // get a list of all files in the parent directory and sort them in alphabetical order
        File[] dirFiles = parent.listFiles();
        Arrays.sort(dirFiles, fileComparator);

        // add sorted files to the correct array list to be used in adapters
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                folders.add(new NoteFile(file));
            } else {
                files.add(new NoteFile(file));
            }
        }

        // initialize adapters with the files from the directory
        folderAdapter = new FileListAdapter(BrowserActivity.this, folders, true);
        fileAdapter = new FileListAdapter(BrowserActivity.this, files, false);

        // set up layout stuff based on orientation
        if (portrait) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) folderList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 49 * (folderAdapter.getCount()) + 25, getResources().getDisplayMetrics());
            folderList.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) fileList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 49 * (fileAdapter.getCount()) + 25, getResources().getDisplayMetrics());
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
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            int padding2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
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
    }

    /**
     * Handles all long clicks on a file or folder
     * @param noteFile is the file which we are acting on
     * @return
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
                                                noteFile.getFile().delete();
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
}
