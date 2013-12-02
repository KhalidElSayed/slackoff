package com.klinker.android.slackoff.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple data structure to easily get information from files
 *
 * @author Jake and Luke Klinker
 */
public class NoteFile {

    /**
     * the file associated with the note
     */
    private File file;

    /**
     * whether or not this file is a directory
     */
    private boolean isFolder;

    /**
     * the path to this file
     */
    private String path;

    /**
     * the files name
     */
    private String fileName;

    /**
     * the date the file was last modified
     */
    private long date;

    /**
     * public constructor creating information off of specified file
     * @param file the file to act on
     */
    public NoteFile(File file) {
        this.file = file;

        this.isFolder = file.isDirectory();
        this.path = file.getAbsolutePath();
        this.fileName = file.getName();
        this.date = file.lastModified();
    }

    /**
     * gets the file for use later if need be
     * @return the file
     */
    public File getFile() {
        return this.file;
    }

    /**
     * whether or not this file is a directory
     * @return true if it is a directory, else false
     */
    public boolean isFolder() {
        return this.isFolder;
    }

    /**
     * gets the path to the file
     * @return the path on sd card
     */
    public String getPath() {
        return this.path;
    }

    /**
     * the file name
     * @return the file's name
     */
    public String getName() {
        return this.fileName;
    }

    /**
     * gets the date formatted for the file
     * @return the date as a string
     */
    public String getDate() {
        return new SimpleDateFormat("M/d/yyyy, h:mm a").format(new Date(this.date));
    }
}
