package com.klinker.android.slackoff.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteFile {

    private boolean isFolder;
    private String path;
    private String fileName;
    private long date;

    public NoteFile(File file) {
        this.isFolder = file.isDirectory();
        this.path = file.getPath();
        this.fileName = file.getName();
        this.date = file.lastModified();
    }

    public NoteFile(boolean isFolder, String path, String name, long date) {
        this.isFolder = isFolder;
        this.path = path;
        this.fileName = name;
        this.date = date;
    }

    public boolean isFolder() {
        return this.isFolder;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.fileName;
    }

    public String getDate() {
        return new SimpleDateFormat("M/d/yyyy, h:mm a").format(new Date(this.date));
    }
}
