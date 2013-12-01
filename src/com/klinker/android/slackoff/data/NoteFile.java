package com.klinker.android.slackoff.data;

import java.io.File;

public class NoteFile {

    private boolean isFolder;
    private String path;
    private String fileName;

    public NoteFile(File file) {
        this.isFolder = file.isDirectory();
        this.path = file.getPath();
        this.fileName = file.getName();
    }

    public NoteFile(boolean isFolder, String path, String name) {
        this.isFolder = isFolder;
        this.path = path;
        this.fileName = name;
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
}
