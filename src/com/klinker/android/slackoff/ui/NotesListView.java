package com.klinker.android.slackoff.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Custom list view to prevent size changes refreshing the view
 */
public class NotesListView extends ListView {

    /**
     * Default constructor
     * @param context
     */
    public NotesListView(Context context) {
        super(context);
    }

    /**
     * Default constructor
     * @param context
     * @param attributeSet
     */
    public NotesListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * Overrides to prevent the focus on our text views from switching when the keyboard is shown
     * @param w
     * @param h
     * @param olw
     * @param oldh
     */
    @Override
    public void onSizeChanged(int w, int h, int olw, int oldh) {

    }
}
