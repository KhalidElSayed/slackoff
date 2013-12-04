package com.klinker.android.slackoff.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.data.NoteFile;
import com.klinker.android.slackoff.utils.Utils;

import java.util.ArrayList;

/**
 * The adapter which is used to show files in the listviews
 *
 * @author Jake and Luke Klinker
 */
public class FileListAdapter extends ArrayAdapter<NoteFile> {

    /**
     * The files to be displayed in the adapter
     */
    private ArrayList<NoteFile> files;

    /**
     * The activity context
     */
    private Context context;

    /**
     * if this is a folder adapter or files
     */
    private boolean folders;

    /**
     * creates adapter
     *
     * @param context the activities context
     * @param files   the files to be displayed
     * @param folders whether this is a folder system or file system
     */
    public FileListAdapter(Context context, ArrayList<NoteFile> files, boolean folders) {
        super(context, folders ? R.layout.folder_item : R.layout.file_item);
        this.context = context;
        this.files = files;
        this.folders = folders;
    }

    /**
     * Sets the number of items in the adapter
     *
     * @return the size of the adapter
     */
    @Override
    public int getCount() {
        return files.size();
    }

    /**
     * Gets the view to be shown in the list (uses view recycling for efficiency)
     *
     * @param position    the position of the item in the list
     * @param convertView the recycled view
     * @param parent      the parent of the recycled view
     * @return the final view to be shown
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // use the recycled view
        View rowView = convertView;

        // check if this view has been initialized already, if not, inflate it and initialize the view holder
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(folders ? R.layout.folder_item : R.layout.file_item, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.fileName = (TextView) rowView.findViewById(R.id.fileName);
            viewHolder.lastModified = (TextView) rowView.findViewById(R.id.date);

            // sets the tag so we can find the view later
            rowView.setTag(viewHolder);
        }

        // finds specified view holder and sets the text correctly
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.fileName.setText(files.get(position).getName().replace(Utils.EXTENSION, " " + context.getString(R.string.notes_file)));
        holder.lastModified.setText(files.get(position).getDate());

        return rowView;
    }

    /**
     * holds the view information for quick and efficient recycling
     * and buttery smooth animations
     */
    static class ViewHolder {
        public TextView fileName;
        public TextView lastModified;
    }
}
