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

import java.util.ArrayList;


public class FileListAdapter extends ArrayAdapter<NoteFile> {

    private ArrayList<NoteFile> files;
    private Context context;
    private boolean folders;

    public FileListAdapter(Context context, ArrayList<NoteFile> files, boolean folders) {
        super(context, folders ? R.layout.folder_item : R.layout.file_item);
        this.context = context;
        this.files = files;
        this.folders = folders;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(folders ? R.layout.folder_item : R.layout.file_item, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.fileName = (TextView) rowView.findViewById(R.id.fileName);
            viewHolder.lastModified = (TextView) rowView.findViewById(R.id.date);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.fileName.setText(files.get(position).getName());
        holder.lastModified.setText(files.get(position).getDate());

        return rowView;
    }

    static class ViewHolder {
        public TextView fileName;
        public TextView lastModified;
    }
}
