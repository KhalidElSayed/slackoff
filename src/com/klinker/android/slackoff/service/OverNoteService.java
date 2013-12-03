package com.klinker.android.slackoff.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.klinker.android.slackoff.R;
import com.klinker.android.slackoff.ui.BrowserActivity;
import com.klinker.android.slackoff.utils.IOUtils;

/**
 * Service which controls overnote
 *
 * @author Jake and Luke Klinker
 */
public class OverNoteService extends Service {

    // just a random number for the id
    public final int FOREGROUND_SERVICE_ID = 2532;

    // some of the basic info i will use
    private Context mContext;
    private SharedPreferences sharedPrefs;
    private Vibrator v;
    private Display d;
    private int height;
    private int width;

    // detects the gestures so i know what the user is doing
    private GestureDetector mGestureDetector;

    private WindowManager.LayoutParams noteParamsUnfocused;
    private WindowManager.LayoutParams noteParamsFocused;
    private WindowManager noteWindow;
    private View noteView;

    private EditText name;
    private EditText content;
    private Button save;
    private Button discard;


    /**
     * first lifecycle event of the service which starts and creates everything we need
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("over_note", "service started");

        mContext = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // gets the display
        d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        // registers the intentfilter to kill the service when the class has ended
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.notes.STOP_NOTES");
        registerReceiver(stopNotes, filter);

        Notification notification = new Notification(R.drawable.ic_launcher, getResources().getString(R.string.app_name),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, BrowserActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getResources().getString(R.string.app_name),
                "Click to open", pendingIntent);

        // because ice cream sandwhich doesn't support this
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            notification.priority = Notification.PRIORITY_MIN;
        }

        // creates a notification so the system knows not to kill the service
        startForeground(FOREGROUND_SERVICE_ID, notification);

        // initializes the vibrator service so i can use it
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // more setup stuff for the view
        initialSetup();
        setUpTouchListeners(height, width);
    }

    /**
     * registers all of the necessary touch listeners on the window
     * @param height the height of the window
     * @param width the width of the window
     */
    private void setUpTouchListeners(final int height, final int width) {
        noteView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                // takes away the focus and changes the params
                name.clearFocus();
                content.clearFocus();

                //mGestureDetector.onTouchEvent(event);

                if (touchedNoteHandle(event)) {
                    final int type = event.getActionMasked();

                    switch (type) {
                        case MotionEvent.ACTION_DOWN:

                            // Vibrate

                            return true;

                        case MotionEvent.ACTION_MOVE:

                            // update my view and where it is at
                            noteParamsUnfocused.x = (int)event.getRawX();
                            noteParamsFocused.x = (int) event.getRawX();
                            noteWindow.updateViewLayout(noteView, noteParamsUnfocused);

                            return true;

                        case MotionEvent.ACTION_UP:

                            // set the view

                            return true;
                    }
                }

                return false;
            }
        });
    }

    /**
     * Controls when the handle to pull the note in and out has been touched
     * @param event the motion event to check agains
     * @return true if handle has been touched
     */
    private boolean touchedNoteHandle(MotionEvent event) {
        return event.getX() > noteView.getX() - 100 && event.getX() < noteView.getX() + 100;  // checks the x position within a range
    }

    /**
     * Sets up the initial window
     */
    private void initialSetup() {
        // creates the note from the resource file
        noteView = View.inflate(this, R.layout.over_note, null);

        // sets it up on the screen. it will start at the edge and the user will be able to swipe it out
        noteParamsUnfocused = new WindowManager.LayoutParams(
                (int) (width * .90),          // width of the note box
                (int) (height* .40),           // height of the note box
                width - 40,           // 15 density pixels shown on on the right side of the screen
                (int) (height * .08),        // starts 12.5% down the screen
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        noteParamsUnfocused.gravity = Gravity.TOP | Gravity.LEFT;
        noteParamsUnfocused.windowAnimations = android.R.style.Animation_Translucent;

        // needed as a workaround for focusing on the edit text boxes
        // as an alert dialog, it doesn't let you focus and still be able to watch for outside touches
        // so i use this to trick the system when i need that focus to enter text and bring up the keyboard
        noteParamsFocused = new WindowManager.LayoutParams(
                (int) (width * .90),          // width of the note box
                (int) (height* .40),           // height of the note box
                width - 40,           // 15 density pixels shown on on the right side of the screen
                (int) (height * .08),        // starts 12.5% down the screen
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        noteParamsFocused.gravity = Gravity.TOP | Gravity.LEFT;
        noteParamsFocused.windowAnimations = android.R.style.Animation_Translucent;

        // gets the system service
        noteWindow = (WindowManager) getSystemService(WINDOW_SERVICE);

        // sets up the attributes of the note
        name = (EditText) noteView.findViewById(R.id.name);
        content = (EditText) noteView.findViewById(R.id.content);
        discard = (Button) noteView.findViewById(R.id.discard);
        save = (Button) noteView.findViewById(R.id.save);

        // needed for the style of the edit texts
        name.clearFocus();
        name.setCursorVisible(false);
        content.clearFocus();
        content.setCursorVisible(false);

        name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // sets the focused params so that we can actually bring up the IME
                noteWindow.updateViewLayout(noteView, noteParamsFocused);

                // Brings up the IME and shows the cursor on the EditText
                name.requestFocus();
                name.setCursorVisible(true);

                return false;
            }
        });

        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                // sets the focused params so that we can actually bring up the IME
                noteWindow.updateViewLayout(noteView, noteParamsFocused);

                // Brings up the IME and shows the cursor on the EditText
                content.requestFocus();
                content.setCursorVisible(true);

                return false;
            }
        });

        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // discard clicked, we just want to push the window back to the side

                // set the x value on the window
                noteParamsFocused.x = width - 40;
                noteParamsUnfocused.x = width - 40;

                // make sure the focuses are cleared
                name.clearFocus();
                content.clearFocus();

                // clears out the text
                name.setText("");
                content.setText("");

                // here we are going to actually remove the view and re-add it
                // I do it this way because there are only system level animations that can be applied to alert dialogs
                // and this is the way to run that animation. simply updating the view doesn't do it
                noteWindow.removeView(noteView);
                noteWindow.addView(noteView, noteParamsUnfocused);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save has been clicked. we want to save the data, then simulate a discard clicked
                IOUtils.writeFile("Circuits", content.getText().toString(), name.getText().toString());

                // simulated discard to clear and reset window
                discard.performClick();
            }
        });

        // Adds the view to the window for the user
        noteWindow.addView(noteView, noteParamsUnfocused);
    }

    /**
     * Binds the service
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Receiver which will kill the service when we enter the app so that both aren't open at the same time
     */
    public BroadcastReceiver stopNotes = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            noteWindow.removeView(noteView);

            // kills the notification, stops the service, then cleans up and clears the receiver
            stopForeground(true);
            stopSelf();

            // the receiver will be unregisterd in the onDestroy
        }
    };

    /**
     * Kills the service and unregisters the receivers
     */
    @Override
    public void onDestroy() {
        // unregisters the receiver so we don't have it firing at weird times
        // and it isn't leaky and slowing stuff down
        unregisterReceiver(stopNotes);

        super.onDestroy();
    }
}
