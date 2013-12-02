package com.klinker.android.slackoff.utils;

import android.content.Context;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import java.io.File;

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
     * @param px the number of pixels
     * @return the value of the density independent pixels
     */
    public static int toDP(Context context, double px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) px, context.getResources().getDisplayMetrics());
    }

    /**
     * Recursive method to delete all underlying files before deleting the current file to be sure that it is gone
     * @param file the file to be deleted
     */
    public static void deleteDirectory(File file) {
        if (!file.isDirectory() || file.list().length <= 0) {
            file.delete();
        } else {
            for (File f : file.listFiles()) {
                deleteDirectory(f);
                f.delete();
            }
        }

        file.delete();
    }
}
