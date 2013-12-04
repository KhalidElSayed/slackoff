package com.klinker.android.slackoff.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.util.Date;

/**
 * Adapter for handling the class schedule in the drawer
 *
 * @author Jake and Luke Klinker
 */
public class ClassesCursorAdapter extends CursorAdapter {

    /**
     * context of the app
     */
    private Context context;

    /**
     * cursor to page through
     */
    private Cursor mCursor;

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
         * holds the days of the week
         */
        public TextView days;
    }

    /**
     * Public constructor
     *
     * @param context context of the app
     * @param cursor  cursor to go through
     */
    public ClassesCursorAdapter(Context context, Cursor cursor) {
        // calls the super class constructor
        super(context, cursor, false);

        this.context = context;
        this.mCursor = cursor;
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
        v = inflater.inflate(R.layout.school_class, viewGroup, false);

        holder = new ViewHolder();

        holder.name = (TextView) v.findViewById(R.id.name);
        holder.start = (TextView) v.findViewById(R.id.start);
        holder.end = (TextView) v.findViewById(R.id.end);
        holder.days = (TextView) v.findViewById(R.id.days);

        v.setTag(holder);

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
            v = newView(context, mCursor, parent);

        } else {
            // view is recycled
            v = convertView;

            final ViewHolder holder = (ViewHolder) v.getTag();

        }

        bindView(v, context, mCursor);

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

        // sets the info to the view
        holder.name.setText(mName);
        holder.days.setText(mDays);

        // Formatting the start date
        Date startDate = new Date(mStart);
        int mHours = startDate.getHours();
        int mMins = startDate.getMinutes();
        String hours = mHours < 13 ? mHours + "" : mHours - 12 + "";
        String mins = mMins < 10 ? "0" + mMins : mMins + "";
        holder.start.setText(hours + ":" + mins);

        // Formatting the end date
        Date endDate = new Date(mEnd);
        mHours = endDate.getHours();
        mMins = endDate.getMinutes();
        hours = mHours < 13 ? mHours + "" : mHours - 12 + "";
        mins = mMins < 10 ? "0" + mMins : mMins + "";
        holder.end.setText(hours + ":" + mins);
    }
}
