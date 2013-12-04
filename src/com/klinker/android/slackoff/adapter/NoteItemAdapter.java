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

import java.util.ArrayList;

/**
 * The adapter which is used to show notes in the listview
 *
 * @author Jake and Luke Klinker
 */
public class NoteItemAdapter extends ArrayAdapter<String> {

    /**
     * Marks that item is checked
     */
    private static final String CHECKED = "_[1]_";

    /**
     * marks that item is not checked
     */
    private static final String UNCHECKED = "_[0]_";

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
     * variable holding what item should have focus when the list is refreshed
     */
    private int focusOn = 0;

    /**
     * creates adapter
     *
     * @param context    the activities context
     * @param notes      the notes to be displayed
     * @param checkBoxes whether or not check boxes should be displayed on the notes
     */
    public NoteItemAdapter(Context context, ArrayList<String> notes, boolean checkBoxes) {
        super(context, R.layout.note_item);
        this.context = context;
        this.notes = notes;
        this.checkBoxes = checkBoxes;
    }

    /**
     * sets whether or not to show the checkboxes on the note
     * @param checkBoxes
     */
    public void setCheckBoxes(boolean checkBoxes) {
        this.checkBoxes = checkBoxes;
        notifyDataSetChanged();
    }

    /**
     * Sets the number of items in the adapter
     *
     * @return the size of the adapter
     */
    @Override
    public int getCount() {
        return notes.size();
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
            rowView = inflater.inflate(R.layout.note_item, null);

            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            viewHolder.note = (EditText) rowView.findViewById(R.id.note);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.imageView);
            viewHolder.discard = (ImageButton) rowView.findViewById(R.id.deleteButton);

            viewHolder.note.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    notes.set(viewHolder.note.getId(), (viewHolder.checkBox.isChecked() ? CHECKED : UNCHECKED) + editable.toString());
                }
            });

            // sets the tag so we can find the view later
            rowView.setTag(viewHolder);
        }

        String note = notes.get(position);

        // finds specified view holder and sets the text and information correctly
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.note.setId(position);

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

        // remove the check box markers from the start of the note
        note = note.replace("_[1]_", "").replace("_[0]_", "");

        // act on image or text
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

        // show and hide the discard button as focus changes
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
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            holder.discard.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    holder.discard.startAnimation(anim);
                }
            }
        });

        // remove the line when the discard button is clicked
        holder.discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.note.getId();

                if (position == getCount() - 1) {
                    focusOn = position - 1;
                } else {
                    focusOn = position;
                }

                notes.remove(position);

                if (getCount() == 0) {
                    notes.add("");
                    focusOn = 0;
                }

                notifyDataSetChanged();
            }
        });

        // adjust what view should be focused on
        if (focusOn > getCount()) {
            focusOn = getCount() - 1;
        }

        // set that focus when appropriate
        if (focusOn == position) {
            holder.note.requestFocusFromTouch();
            holder.note.setSelection(note.length());
        }

        // show the discard button when necessary
        if (holder.note.hasFocus()) {
            holder.discard.setVisibility(View.VISIBLE);
        } else {
            holder.discard.setVisibility(View.GONE);
        }

        return rowView;
    }

    /**
     * adds a new note line to the bottom of the adapter
     */
    public void addLine() {
        focusOn = getCount();
        notes.add("");
        notifyDataSetChanged();
    }

    /**
     * Gets all of the notes in the current adapter which have been kept up to date as the user types
     * @return the notes associated with each line
     */
    public ArrayList<String> getNotes() {
        return this.notes;
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
