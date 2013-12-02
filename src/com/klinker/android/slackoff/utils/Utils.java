package com.klinker.android.slackoff.utils;

import android.content.Context;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

/**
 * Easy utils functions to be reused
 *
 * @author Jake and Luke Klinker
 */
public class Utils {

    /**
     * Gets the mime type of the specified file so that it can be opened in correct app
     * @param url the path to the file
     * @return the mimetype of specified file
     */
    public static String getMimeType(String url) {
        // escape white spaces
        url = url.replace(" ", "\\ ");
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Converts pixels to density independent pixels
     * @param px the number of pixels
     * @return the value of the density independent pixels
     */
    public static int toDP(Context context, double px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) px, context.getResources().getDisplayMetrics());
    }
}
