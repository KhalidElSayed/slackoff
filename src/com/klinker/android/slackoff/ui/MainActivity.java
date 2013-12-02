package com.klinker.android.slackoff.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.FileListAdapter;
import com.klinker.android.slackoff.data.NoteFile;

import java.io.File;
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

    private File parent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderList = (ListView) findViewById(R.id.folderList);
        fileList = (AbsListView) findViewById(R.id.fileList);

        portrait = folderList.getTag().equals("portrait");

        files = new ArrayList<NoteFile>();
        folders = new ArrayList<NoteFile>();

        if (getIntent().getStringExtra("parent_file") != null) {
            parent = new File(getIntent().getStringExtra("parent_file"));
            setTitle(parent.getName());
        } else {
            parent = Environment.getExternalStorageDirectory();
        }

        for (File file : parent.listFiles()) {
            if (file.isDirectory()) {
                folders.add(new NoteFile(file));
            } else {
                files.add(new NoteFile(file));
            }
        }

        folderAdapter = new FileListAdapter(MainActivity.this, folders, true);
        fileAdapter = new FileListAdapter(MainActivity.this, files, false);

        if (portrait) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) folderList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 49 * (folderAdapter.getCount()) + 25, getResources().getDisplayMetrics());
            folderList.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) fileList.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 49 * (fileAdapter.getCount()) + 25, getResources().getDisplayMetrics());
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

        folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent nextFolder = new Intent(MainActivity.this, MainActivity.class);
                nextFolder.putExtra("parent_file", folders.get(i - 1).getPath());
                startActivity(nextFolder);
            }
        });
    }
}
