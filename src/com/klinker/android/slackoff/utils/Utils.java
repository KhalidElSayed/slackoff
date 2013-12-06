package com.klinker.android.slackoff.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import com.klinker.android.slackoff.data.SchoolClass;
import com.klinker.android.slackoff.sql.SchoolData;
import com.klinker.android.slackoff.sql.SchoolHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Easy utils functions to be reused
 *
 * @author Jake and Luke Klinker
 */
public class Utils {

    /**
     * The extension that will be used for all note files created by slackoff
     */
    public static final String EXTENSION = ".klink";

    /**
     * Gets the mime type of the specified file so that it can be opened in correct app
     *
     * @param url the path to the file
     * @return the mimetype of specified file
     */
    public static String getMimeType(String url) {
        // escape white spaces
        url = url.replace(" ", "\\ ");
        String extention = url.substring(url.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extention);
        String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(mimeTypeMap);

        if (mimeType != null) {
            return mimeType;
        } else {
            return "file/" + extention.replace(".", "");
        }
    }

    /**
     * Converts pixels to density independent pixels
     *
     * @param px the number of pixels
     * @return the value of the density independent pixels
     */
    public static int toDP(Context context, double px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) px, context.getResources().getDisplayMetrics());
    }

    /**
     * The "recommended" android way of loading large bitmaps and scaling them down so as to not run out of memory
     *
     * @param context the context to load with
     * @param uri the uri to load the image from
     * @return the bitmap to be loaded
     * @throws IOException
     */
    public static Bitmap getThumbnail(Context context, Uri uri) throws IOException {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, context.getResources().getDisplayMetrics());

        // create an input stream of the bitmap to act on
        InputStream input = context.getContentResolver().openInputStream(uri);

        // decode the input stream with some given parameters
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        // get the original size of the image (whichever side is larger)
        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        // get the ratio between the sides, to be used for scaling correctly
        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;

        // once again, decode the bitmap respecting memory constraints
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        // return the final thumbnail (scaled down) bitmap
        return bitmap;
    }

    /**
     * Internal function used for resizing a bitmap appropriately
     *
     * @param ratio the ratio between the side of the bitmap
     * @return the power of two to be used when scaling the final bitmap
     */
    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    /**
     * Function to get the name of the ongoing class and return it as a string
     *
     * @param context Context of the app
     * @return ongoing class as a path
     */
    public static SchoolClass getCurrentClass(Context context) {
        String name = "";
        String days = "";
        long start = 0;
        long end = 0;

        // opens up the school database to read from
        SchoolData data = new SchoolData(context);
        data.open();

        // gets the cursor from that database
        Cursor cursor = data.getCursor();

        if (cursor.moveToFirst()) { // should always be true, because if there are no classes scheduled, then the overnote will never show up and this won't be called, but just in case
            boolean flag = true;

            do {
                // gets the data from the sql table for the class time
                long startTime = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_END_TIME));

                // creates some date objects from those times
                Date startDate = new Date(startTime);
                Date endDate = new Date(endTime);
                Date currDate = new Date();

                if (startDate.before(currDate) && endDate.after(currDate)) {
                    // this is the class that is currently going on
                    // break out of the loop at this spot so we can get the name of the class for the path
                    flag = false;
                }

            } while (flag && cursor.moveToNext());

            cursor.moveToPrevious();

            name = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_NAME));
            days = cursor.getString(cursor.getColumnIndex(SchoolHelper.COLUMN_DAYS));
            end = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_END_TIME));
            start = cursor.getLong(cursor.getColumnIndex(SchoolHelper.COLUMN_START_TIME));
        }

        // close the database
        data.close();
        cursor.close();

        SchoolClass mClass = new SchoolClass(name, start, end, days);

        // return the path no matter what, if it is still blank, then the note will be placed in the root.
        // it shouldn't be black though or there has been an error with the cursor
        return mClass;
    }
}
