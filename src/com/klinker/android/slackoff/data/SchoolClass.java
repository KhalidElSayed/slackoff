package com.klinker.android.slackoff.data;

/**
 * Simple data file for handling class information from sql
 *
 * @author Jake and Luke Klinker
 */
public class SchoolClass {

    /**
     * Name of the class
     */
    private String name;

    /**
     * Start time as a long
     */
    private long start;

    /**
     * end time as a long
     */
    private long end;

    /**
     * Days of the week (Su, M, T, W, Th, F, S - only seperated by spaces)
     */
    private String days;

    /**
     * Public constructor
     *
     * @param name the name
     * @param start the start time
     * @param end the end time
     * @param days days of the week
     */
    public SchoolClass(String name, long start, long end, String days) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.days = days;
    }

    /**
     * getter method for name
     * @return name of the class
     */
    public String getName() {
        return name;
    }

    /**
     * getter method for start time
     * @return long with the start time
     */
    public long getStart() {
        return start;
    }

    /**
     * getter method for end time
     * @return long with the end time
     */
    public long getEnd() {
        return end;
    }

    /**
     * getter method for the days
     * @return String with the days of the week
     */
    public String getDays() {
        return days;
    }
}
