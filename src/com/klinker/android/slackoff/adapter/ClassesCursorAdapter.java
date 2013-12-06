package com.klinker.android.slackoff.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.service.OverNoteKiller;
import com.klinker.android.slackoff.service.OverNoteService;
import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.util.Date;

/**
 * Adapter for handling the class schedule in the drawer
 *
 * @author Luke Klinker
 */
public class ClassesCursorAdapter extends CursorAdapter {

    /**
     * context of the app
     */
    private Context mContext;

    /**
     * cursor to page through
     */
    private Cursor mCursor;

    /**
     * Holds the list view so it can be updated
     */
    private ListView drawerList;

    /**
     * Layout inflator to get the view from the xml
     */
    private LayoutInflater inflater;

    public class ViewHolder {
        /**
         * holds the name
         */
        public TextView name;

        /**
         * holds start time
         */
        public TextView start;

        /**
         * holds the end time
         */
        public TextView end;

        /**
         * holds the delete button
         */
        public ImageButton delete;

        /**
         * holds the days of the week
         */
        public TextView sunday;
        public TextView monday;
        public TextView tuesday;
        public TextView wednesday;
        public TextView thursday;
        public TextView friday;
        public TextView saturday;
    }

    /**
     * Public constructor
     *
     * @param context context of the app
     * @param cursor  cursor to go through
     * @param drawerList list view that the cursor is being adapted to
     */
    public ClassesCursorAdapter(Context context, Cursor cursor, ListView drawerList) {
        // calls the super class constructor
        super(context, cursor, false);

        this.mContext = context;
        this.mCursor = cursor;
        this.drawerList = drawerList;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Called when you want to create the new view
     *
     * @param context   context of app
     * @param cursor    cursor to go through
     * @param viewGroup
     * @return the view that holds the data along with its tags
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v;
        final ViewHolder holder;
        // inflates view so that we can get the attributes
        v = inflater.inflate(R.layout.school_class, viewGroup, false);

        // creates the view holder object
        holder = new ViewHolder();

        // writes the attributes to the viewholder
        holder.name = (TextView) v.findViewById(R.id.name);
        holder.start = (TextView) v.findViewById(R.id.start);
        holder.end = (TextView) v.findViewById(R.id.end);
        holder.delete = (ImageButton) v.findViewById(R.id.delete_button);
        holder.sunday = (TextView) v.findViewById(R.id.sunday);
        holder.monday = (TextView) v.findViewById(R.id.monday);
        holder.tuesday = (TextView) v.findViewById(R.id.tuesday);
        holder.wednesday = (TextView) v.findViewById(R.id.wednesday);
        holder.thursday = (TextView) v.findViewById(R.id.thursday);
        holder.friday = (TextView) v.findViewById(R.id.friday);
        holder.saturday = (TextView) v.findViewById(R.id.saturday);

        // sets the tags so that we can get them later
        v.setTag(holder);

        // return the view we created
        return v;
    }

    /**
     * Called when the view is first getting prepared for the screen
     *
     * @param position    position in the list view
     * @param convertView recycled view if it is availible
     * @param parent      parent of the item
     * @return view to be shown
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // checks to make sure the item exists
        if (!mCursor.moveToPosition(mCursor.getCount() - 1 - position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        View v;
        if (convertView == null) {
            // view is not being recycled
            v = newView(mContext, mCursor, parent);

        } else {
            // view is recycled
            v = convertView;

            final ViewHolder holder = (ViewHolder) v.getTag();

        }

        bindView(v, mContext, mCursor);

        return v;
    }

    /**
     * Called when you actually display the data to the screen as you scroll through the view
     *
     * @param view    view that holds the data
     * @param context context of app
     * @param cursor  cursor to go through
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // sets up the view holder
        final ViewHolder holder = (ViewHolder) view.getTag();

        // gets the info for the view
        final String mName = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_NAME));
        final String mDays = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_DAYS));
        final long mStart = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_START_TIME));
        final long mEnd = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_END_TIME));
        final long mId = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_ID));

        // sets the info to the view
        holder.name.setText(mName);

        // Formatting the start date
        Date startDate = new Date(mStart);
        int mHours = startDate.getHours();
        int mMins = startDate.getMinutes();
        String ampm = mHours >= 12 ? " PM" : " AM";
        String hours = mHours < 13 ? mHours + "" :  mHours - 12 + "";
        String mins = mMins < 10 ? "0" + mMins : mMins + "";
        holder.start.setText(hours + ":" + mins + ampm);

        // Formatting the end date
        Date endDate = new Date(mEnd);
        mHours = endDate.getHours();
        mMins = endDate.getMinutes();
        ampm = mHours >= 12 ? " PM" : " AM";
        hours = mHours < 13 ? mHours + "" :  mHours - 12 + "";
        mins = mMins < 10 ? "0" + mMins : mMins + "";
        holder.end.setText(hours + ":" + mins + ampm);

        // setting up the days of the week
        if (mDays.contains("S ")) {
            holder.sunday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }
        if (mDays.contains("M ")) {
            holder.monday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }
        if (mDays.contains("T ")) {
            holder.tuesday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }
        if (mDays.contains("W ")) {
            holder.wednesday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }
        if (mDays.contains("Th ")) {
            holder.thursday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }
        if (mDays.contains("F ")) {
            holder.friday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }
        if (mDays.contains("Sa ")) {
            holder.saturday.setTextColor(context.getResources().getColor(R.color.dark_text));
        }

        // sets the click listener to delete the data when
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // opens up the school data sql database, then deletes the entry
                SchoolData data = new SchoolData(mContext);
                data.open();
                data.deleteClass(mName);

                // refreshes the class list by reseting the cursor adapter
                drawerList.setAdapter(new ClassesCursorAdapter(mContext, data.getCursor(), drawerList));
                data.close(); // closes the database

                // cancels the alarms by recreating the same pending intent, then using the alarm manager to cancel it
                AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                
                PendingIntent pendingIntent = PendingIntent.getService(mContext, (int) mId, new Intent(mContext, OverNoteService.class), 0);
                am.cancel(pendingIntent);
                PendingIntent killerServ = PendingIntent.getService(mContext, (int) mId + 1, new Intent(mContext, OverNoteKiller.class), 0);
                am.cancel(killerServ);
            }
        });
    }
}
