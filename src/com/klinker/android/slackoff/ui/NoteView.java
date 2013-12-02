package com.klinker.android.slackoff.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.ViewGroup;

/**
 * Created by luke on 12/1/13.
 */
public class NoteView extends ViewGroup {

    public Context mContext;

    public static float TEXT_SIZE;
    public static float TEXT_SIZE_BIG;
    public static float TEXT_GAP;

    public Bitmap halo;

    public Paint blackPaint;
    public Paint strokePaint;
    public TextPaint messageReceivedPaint;
    public TextPaint messageSentPaint;
    public Paint namePaint;

    public SharedPreferences sharedPrefs;

    public NoteView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        TEXT_GAP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, context.getResources().getDisplayMetrics());
        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, context.getResources().getDisplayMetrics());
        TEXT_SIZE_BIG = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, context.getResources().getDisplayMetrics());

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));
        blackPaint.setAlpha((int) (sharedPrefs.getInt("quick_peek_transparency", 100) * 2.5));

        strokePaint = new Paint(blackPaint);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setAlpha(50);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3);

        messageSentPaint = new TextPaint(messageReceivedPaint);
        messageSentPaint.setColor(Color.GRAY);
        messageSentPaint.setAlpha(SlideOverService.TOUCHED_ALPHA);

        namePaint = new Paint(messageReceivedPaint);
        namePaint.setAlpha(SlideOverService.TOUCHED_ALPHA);
        namePaint.setTextSize(TEXT_SIZE_BIG);
        namePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
    }


    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
        // do nothing here
    }
}
