package com.klinker.android.slackoff.listeners;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.adapter.ClassesCursorAdapter;
import com.klinker.android.slackoff.data.SchoolClass;
import com.klinker.android.slackoff.sql.SchoolData;

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
     * Constructor for the listener
     * @param context the context of the app
     * @param list The list view
     */
    public AddClassListener(Context context, ListView list) {
        this.context = context;
        this.drawerList = list;
    }

    /**
     * On click method that we need to override for the implementation
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

        // sets the save listener
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // makes sure they have something entered
                if (name.getText().length() > 0) {
                    // get the data
                    final int month = datePick.getMonth();
                    final int day = datePick.getDayOfMonth();
                    final int year = datePick.getYear();
                    final String className = name.getText().toString();

                    // dismiss the date picker dialog
                    date.dismiss();

                    // Open the repeat dialog
                    final Dialog repeat = new Dialog(context);
                    repeat.setContentView(R.layout.repeated_dialog);
                    repeat.setTitle(context.getResources().getString(R.string.set_days));

                    Button save = (Button) repeat.findViewById(R.id.save);
                    Button cancel = (Button) repeat.findViewById(R.id.cancel);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean sunday = ((CheckBox) repeat.findViewById(R.id.sunday)).isChecked();
                            boolean monday = ((CheckBox) repeat.findViewById(R.id.monday)).isChecked();
                            boolean tuesday = ((CheckBox) repeat.findViewById(R.id.tuesday)).isChecked();
                            boolean wednesday = ((CheckBox) repeat.findViewById(R.id.wednesday)).isChecked();
                            boolean thursday = ((CheckBox) repeat.findViewById(R.id.thursday)).isChecked();
                            boolean friday = ((CheckBox) repeat.findViewById(R.id.friday)).isChecked();
                            boolean saturday = ((CheckBox) repeat.findViewById(R.id.saturday)).isChecked();

                            final String days = (sunday ?  "S " : "") +
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

                            Button save = (Button) start.findViewById(R.id.save);
                            Button cancel = (Button) start.findViewById(R.id.cancel);

                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final int startHour = ((TimePicker) start.findViewById(R.id.timePicker)).getCurrentHour();
                                    final int startMinute = ((TimePicker) start.findViewById(R.id.timePicker)).getCurrentMinute();

                                    start.dismiss();

                                    // open the start time dialog
                                    final Dialog end = new Dialog(context);
                                    end.setContentView(R.layout.time_dialog);
                                    end.setTitle(context.getResources().getString(R.string.end_time));

                                    Button save = (Button) end.findViewById(R.id.save);
                                    save.setText(context.getResources().getString(R.string.save));
                                    Button cancel = (Button) end.findViewById(R.id.cancel);

                                    save.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            int endHour = ((TimePicker) end.findViewById(R.id.timePicker)).getCurrentHour();
                                            int endMinute = ((TimePicker) end.findViewById(R.id.timePicker)).getCurrentMinute();

                                            Date setDate = new Date(year,month,day,startHour,startMinute);
                                            final long setTime = setDate.getTime();

                                            setDate = new Date(year,month,day,endHour,endMinute);
                                            final long endTime = setDate.getTime();

                                            // store the class to the database
                                            SchoolClass newClass = new SchoolClass(className, setTime, endTime, days);
                                            SchoolData data = new SchoolData(context);

                                            data.open();
                                            data.addClass(newClass);

                                            drawerList.setAdapter(new ClassesCursorAdapter(context, data.getCursor()));

                                            data.close();

                                            end.dismiss();

                                        }
                                    });

                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            end.dismiss();
                                        }
                                    });

                                    end.show();

                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    start.dismiss();
                                }
                            });

                            start.show();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            repeat.dismiss();
                        }
                    });

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


        date.show();

    }
}
