package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.FileListAdapter;
import com.klinker.android.slackoff.data.NoteFile;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ListView folderList;
    private FileListAdapter folderAdapter;
    private ListView fileList;
    private FileListAdapter fileAdapter;

    private boolean portrait;

    private ArrayList<NoteFile> files;
    private ArrayList<NoteFile> folders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderList = (ListView) findViewById(R.id.folderList);
        fileList = (ListView) findViewById(R.id.fileList);

        portrait = folderList.getTag().equals("portrait");

        files = new ArrayList<NoteFile>();
        files.add(new NoteFile(false, "/", "note 1"));
        files.add(new NoteFile(false,"/", "note 2"));
        files.add(new NoteFile(false, "/", "note 3"));
        files.add(new NoteFile(false, "/", "note 4"));
        files.add(new NoteFile(false, "/", "note 5"));
        files.add(new NoteFile(false, "/", "note 6"));
        files.add(new NoteFile(false, "/", "note 7"));
        files.add(new NoteFile(false, "/", "note 8"));

        folders = new ArrayList<NoteFile>();
        folders.add(new NoteFile(true, "/", "folder 1"));
        folders.add(new NoteFile(true, "/", "folder 2"));
        folders.add(new NoteFile(true, "/", "folder 3"));
        folders.add(new NoteFile(true, "/", "folder 4"));
        folders.add(new NoteFile(true, "/", "folder 5"));
        folders.add(new NoteFile(true, "/", "folder 6"));
        folders.add(new NoteFile(true, "/", "folder 7"));
        folders.add(new NoteFile(true, "/", "folder 8"));

        folderAdapter = new FileListAdapter(MainActivity.this, folders, true);
        fileAdapter = new FileListAdapter(MainActivity.this, files, false);

        if (portrait) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) folderList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * (folderAdapter.getCount()) - 35, getResources().getDisplayMetrics());
            folderList.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) fileList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 * (fileAdapter.getCount()) - 35, getResources().getDisplayMetrics());
            fileList.setLayoutParams(params);
        }


        View folderHeader = getLayoutInflater().inflate(R.layout.list_header, null, false);
        ((TextView) folderHeader.findViewById(R.id.headerText)).setText(getString(R.string.folder));
        folderList.addHeaderView(folderHeader);

        View fileHeader = getLayoutInflater().inflate(R.layout.list_header, null, false);
        ((TextView) fileHeader.findViewById(R.id.headerText)).setText(getString(R.string.file));
        fileList.addHeaderView(fileHeader);

        folderList.setAdapter(folderAdapter);
        fileList.setAdapter(fileAdapter);

        startService(new Intent(this, OverNoteService.class));
    }
}
