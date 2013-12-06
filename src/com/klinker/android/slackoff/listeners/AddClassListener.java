package com.klinker.android.slackoff.listeners;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.ClassesCursorAdapter;
import com.klinker.android.slackoff.adapter.FileListAdapter;
import com.klinker.android.slackoff.data.NoteFile;
import com.klinker.android.slackoff.data.SchoolClass;
import com.klinker.android.slackoff.service.OverNoteKiller;
import com.klinker.android.slackoff.service.OverNoteService;
import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Class to hold the click listener for a new school class
 * This is basically cascading dialog windows, then it writes the class to the database
 *
 * @author Luke and Jake Klinker
 */
public class AddClassListener implements View.OnClickListener {

    /**
     * Context of the app
     */
    private Context context;

    /**
     * ListView for the drawer so we can refresh it at the end
     */
    private ListView drawerList;

    /**
     * List view for the directories so we can refresh it too
     */
    private ListView directories;

    /**
     * Constructor for the listener
     *
     * @param context the context of the app
     * @param list    The list view
     */
    public AddClassListener(Context context, ListView list, ListView dir) {
        this.context = context;
        this.drawerList = list;
        this.directories = dir;
    }

    /**
     * On click method that we need to override for the implementation
     * It pretty much is just a bunch of cascading windows, this could have been done differently, with different
     * click listeners, but this works just as well, it is just a little confusing code
     *
     * @param view View that was clicked
     */
    @Override
    public void onClick(View view) {
        // creates a dialog from the new class dialog xml file
        final Dialog date = new Dialog(context);
        date.setContentView(R.layout.date_dialog);
        // sets the title
        date.setTitle(context.getResources().getString(R.string.next_instance_of_class));

        // gets the elements
        final DatePicker datePick = (DatePicker) date.findViewById(R.id.datePicker);
        final EditText name = (EditText) date.findViewById(R.id.class_name);
        Button save = (Button) date.findViewById(R.id.save);
        Button cancel = (Button) date.findViewById(R.id.cancel);

        // sets the "continue" button click listener for the date picker
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // makes sure they have something entered
                if (name.getText().length() > 0) {
                    // get the data
                    final int month = datePick.getMonth();
                    final int day = datePick.getDayOfMonth();
                    final int year = datePick.getYear() - 1900; // 1900 is where it starts
                    final String className = name.getText().toString();

                    // dismiss the date picker dialog
                    date.dismiss();

                    // Open the repeat dialog
                    final Dialog repeat = new Dialog(context);
                    repeat.setContentView(R.layout.repeated_dialog);
                    repeat.setTitle(context.getResources().getString(R.string.set_days));

                    // finds the buttons on the repeat dialog
                    Button save = (Button) repeat.findViewById(R.id.save);
                    Button cancel = (Button) repeat.findViewById(R.id.cancel);

                    // sets the click listener for the "continue" button on the repeat dialog
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // gets all the checkboxes so we can find which ones were checked
                            boolean sunday = ((CheckBox) repeat.findViewById(R.id.sunday)).isChecked();
                            boolean monday = ((CheckBox) repeat.findViewById(R.id.monday)).isChecked();
                            boolean tuesday = ((CheckBox) repeat.findViewById(R.id.tuesday)).isChecked();
                            boolean wednesday = ((CheckBox) repeat.findViewById(R.id.wednesday)).isChecked();
                            boolean thursday = ((CheckBox) repeat.findViewById(R.id.thursday)).isChecked();
                            boolean friday = ((CheckBox) repeat.findViewById(R.id.friday)).isChecked();
                            boolean saturday = ((CheckBox) repeat.findViewById(R.id.saturday)).isChecked();

                            // saves what days the user has the class
                            final String days = (sunday ? "S " : "") +
                                    (monday ? "M " : "") +
                                    (tuesday ? "T " : "") +
                                    (wednesday ? "W " : "") +
                                    (thursday ? "Th " : "") +
                                    (friday ? "F " : "") +
                                    (saturday ? "Sa " : "");

                            repeat.dismiss();

                            // open the start time dialog
                            final Dialog start = new Dialog(context);
                            start.setContentView(R.layout.time_dialog);
                            start.setTitle(context.getResources().getString(R.string.start_time));

                            // finds the buttons on the start time dialog
                            Button save = (Button) start.findViewById(R.id.save);
                            Button cancel = (Button) start.findViewById(R.id.cancel);

                            // again, sets the "continue" button's action
                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // gets the pickers
                                    final int startHour = ((TimePicker) start.findViewById(R.id.timePicker)).getCurrentHour();
                                    final int startMinute = ((TimePicker) start.findViewById(R.id.timePicker)).getCurrentMinute();

                                    start.dismiss();

                                    // open the start time dialog
                                    final Dialog end = new Dialog(context);
                                    end.setContentView(R.layout.time_dialog);
                                    end.setTitle(context.getResources().getString(R.string.end_time));

                                    // finds the save button this time along with cancel
                                    Button save = (Button) end.findViewById(R.id.save);
                                    save.setText(context.getResources().getString(R.string.save));
                                    Button cancel = (Button) end.findViewById(R.id.cancel);

                                    // sets the final click listener
                                    save.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // gets the end time pickers
                                            int endHour = ((TimePicker) end.findViewById(R.id.timePicker)).getCurrentHour();
                                            int endMinute = ((TimePicker) end.findViewById(R.id.timePicker)).getCurrentMinute();

                                            // sets the start date object and gets the long time
                                            Date setDate = new Date(year, month, day, startHour, startMinute);
                                            final long setTime = setDate.getTime();

                                            // sets the end time
                                            setDate = new Date(year, month, day, endHour, endMinute);
                                            final long endTime = setDate.getTime();

                                            // store the class to the database
                                            SchoolClass newClass = new SchoolClass(className, setTime, endTime, days);
                                            SchoolData data = new SchoolData(context);

                                            // opens and writes the class to the sql table
                                            data.open();
                                            long id = data.addClass(newClass);

                                            // schedules the alarm for the class
                                            scheduleAlarm(id, newClass);

                                            // creates the folder for the class
                                            createFolder(newClass);

                                            // refreshes the adapter for the drawer so the user can see the new class
                                            drawerList.setAdapter(new ClassesCursorAdapter(context, data.getCursor(), drawerList));

                                            // closes the sql table
                                            data.close();

                                            // dismisses the end time dialog
                                            end.dismiss();

                                        }
                                    });

                                    // sets the cancel action for the end time dialog
                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            end.dismiss();
                                        }
                                    });

                                    // shows the end time dialog
                                    end.show();

                                }
                            });

                            // sets the cancel action for the start time dialog
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    start.dismiss();
                                }
                            });

                            // shows the start time dialog
                            start.show();
                        }
                    });

                    // sets the cancel action on the repeat dialog
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            repeat.dismiss();
                        }
                    });

                    // shows the repeat dialog
                    repeat.show();
                }
            }
        });

        // sets the cancel listener
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do nothing
                date.dismiss();
            }
        });

        // shows the first date picker
        date.show();

    }

    /**
     * function to schdule the alarms to start and end the service
     *
     * @param id id for the alarm, same as id for the class in the database
     * @param mClass the class to schedule for
     */
    public void scheduleAlarm(long id, SchoolClass mClass) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, (int) id, new Intent(context, OverNoteService.class), 0);
        am.set(AlarmManager.RTC_WAKEUP, mClass.getStart(), pendingIntent);

        PendingIntent killerServ = PendingIntent.getService(context, (int) id + 1, new Intent(context, OverNoteKiller.class), 0);
        am.set(AlarmManager.RTC_WAKEUP, mClass.getEnd(), killerServ);
    }

    /**
     * creates the new class folder and updates the listview of folders
     *
     * @param newClass the class to make a folder for
     */
    public void createFolder(SchoolClass newClass) {
        File parent = new File(Environment.getExternalStorageDirectory().getPath(), "SlackOff");

        File newClassFold = new File (parent, newClass.getName());
        if (!newClassFold.exists()) {
            newClassFold.mkdirs();
        }

        ArrayList<NoteFile> dirs = new ArrayList<NoteFile>();

        // get a list of all files in the parent directory and sort them in alphabetical order
        File[] dirFiles = parent.listFiles();
        Arrays.sort(dirFiles, fileComparator);

        // add sorted files to the correct array list to be used in adapters
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                dirs.add(new NoteFile(file));
            } else {
                if (file.getName().endsWith(Utils.EXTENSION)) {
                    dirs.add(new NoteFile(file));
                }
            }
        }

        // updates the directories with the new one
        directories.setAdapter(new FileListAdapter(context, dirs, true));
    }

    /**
     * the comparator used to determine alphabetical order of files when they are being sorted
     */
    private Comparator<File> fileComparator = new Comparator<File>() {
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
