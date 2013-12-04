package com.klinker.android.slackoff.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.data.NoteFile;
import com.klinker.android.slackoff.utils.Utils;

import java.util.ArrayList;

/**
 * The adapter which is used to show notes in the listview
 *
 * @author Jake and Luke Klinker
 */
public class NoteItemAdapter extends ArrayAdapter<String> {

    /**
     * The notes to be displayed in the adapter
     */
    private ArrayList<String> notes;

    /**
     * The activity context
     */
    private Context context;

    /**
     * whether or not to show the check boxes on each note
     */
    private boolean checkBoxes;

    /**
     * creates adapter
     * @param context the activities context
     * @param notes the notes to be displayed
     * @param checkBoxes whether or not check boxes should be displayed on the notes
     */
    public NoteItemAdapter(Context context, ArrayList<String> notes, boolean checkBoxes) {
        super(context, R.layout.note_item);
        this.context = context;
        this.notes = notes;
        this.checkBoxes = checkBoxes;
    }

    public void setCheckBoxes(boolean checkBoxes) {
        this.checkBoxes = checkBoxes;
    }

    /**
     * Sets the number of items in the adapter
     * @return the size of the adapter
     */
    @Override
    public int getCount() {
        return notes.size();
    }

    /**
     * Gets the view to be shown in the list (uses view recycling for efficiency)
     * @param position the position of the item in the list
     * @param convertView the recycled view
     * @param parent the parent of the recycled view
     * @return the final view to be shown
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // use the recycled view
        View rowView = convertView;

        // check if this view has been initialized already, if not, inflate it and initialize the view holder
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.note_item, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            viewHolder.note = (EditText) rowView.findViewById(R.id.note);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.imageView);
            viewHolder.discard = (ImageButton) rowView.findViewById(R.id.deleteButton);

            // sets the tag so we can find the view later
            rowView.setTag(viewHolder);
        }

        String note = notes.get(position);

        // finds specified view holder and sets the text and information correctly
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        if (checkBoxes) {
            // set whether or not the check box should be shown checked
            holder.checkBox.setVisibility(View.VISIBLE);
            if (note.startsWith("_[1]_")) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        note = note.replace("_[1]_", "").replace("_[0]_", "");

        if (note.startsWith("<img>")) {
            holder.checkBox.setVisibility(View.GONE);
            holder.note.setVisibility(View.GONE);
            holder.image.setVisibility(View.VISIBLE);
            // TODO parse the image tag and display the bitmap in the holder.image
        } else {
            holder.note.setVisibility(View.VISIBLE);
            holder.note.setText(note);
            holder.image.setVisibility(View.GONE);
        }

        holder.note.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    holder.discard.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right);
                    anim.setDuration(200);
                    holder.discard.startAnimation(anim);
                } else {
                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_right);
                    anim.setDuration(200);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            holder.discard.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                    holder.discard.startAnimation(anim);
                }
            }
        });

        holder.discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notes.remove(position);
                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    /**
     * holds the view information for quick and efficient recycling
     * and buttery smooth animations
     */
    static class ViewHolder {
        public CheckBox checkBox;
        public EditText note;
        public ImageView image;
        public ImageButton discard;
    }
}
