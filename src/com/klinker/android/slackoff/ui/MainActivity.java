package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.FileListAdapter;
import com.klinker.android.slackoff.data.NoteFile;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity {

    private ListView folderList;
    private FileListAdapter folderAdapter;
    private AbsListView fileList;
    private FileListAdapter fileAdapter;

    private boolean portrait;

    private ArrayList<NoteFile> files;
    private ArrayList<NoteFile> folders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderList = (ListView) findViewById(R.id.folderList);
        fileList = (AbsListView) findViewById(R.id.fileList);

        portrait = folderList.getTag().equals("portrait");

        Calendar cal = Calendar.getInstance();

        files = new ArrayList<NoteFile>();
        files.add(new NoteFile(false, "/", "note 1", cal.getTimeInMillis()));
        files.add(new NoteFile(false,"/", "note 2", cal.getTimeInMillis()));
        files.add(new NoteFile(false, "/", "note 3", cal.getTimeInMillis()));
        files.add(new NoteFile(false, "/", "note 4", cal.getTimeInMillis()));
        files.add(new NoteFile(false, "/", "note 5", cal.getTimeInMillis()));
        files.add(new NoteFile(false, "/", "note 6", cal.getTimeInMillis()));
        files.add(new NoteFile(false, "/", "note 7", cal.getTimeInMillis()));
        files.add(new NoteFile(false, "/", "note 8", cal.getTimeInMillis()));

        folders = new ArrayList<NoteFile>();
        folders.add(new NoteFile(true, "/", "folder 1", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 2", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 3", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 4", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 5", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 6", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 7", cal.getTimeInMillis()));
        folders.add(new NoteFile(true, "/", "folder 8", cal.getTimeInMillis()));

        folderAdapter = new FileListAdapter(MainActivity.this, folders, true);
        fileAdapter = new FileListAdapter(MainActivity.this, files, false);

        if (portrait) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) folderList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * (folderAdapter.getCount()) + 32, getResources().getDisplayMetrics());
            folderList.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) fileList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * (fileAdapter.getCount()) + 28, getResources().getDisplayMetrics());
            fileList.setLayoutParams(params);
        }


        View folderHeader = getLayoutInflater().inflate(R.layout.list_header, null, false);
        ((TextView) folderHeader.findViewById(R.id.headerText)).setText(getString(R.string.folder));
        folderList.addHeaderView(folderHeader);

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

        folderList.setAdapter(folderAdapter);
        fileList.setAdapter(fileAdapter);
    }
}
