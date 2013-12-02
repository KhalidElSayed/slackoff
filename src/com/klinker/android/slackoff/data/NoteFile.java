package com.klinker.android.slackoff.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteFile {

    private File file;

    private boolean isFolder;
    private String path;
    private String fileName;
    private long date;

    public NoteFile(File file) {
        this.file = file;

        this.isFolder = file.isDirectory();
        this.path = file.getAbsolutePath();
        this.fileName = file.getName();
        this.date = file.lastModified();
    }

    public File getFile() {
        return this.file;
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
