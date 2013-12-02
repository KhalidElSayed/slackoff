package com.klinker.android.slackoff.utils;

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
        String extention = url.substring(url.lastIndexOf("."));
        String mimeTypeMap =MimeTypeMap.getFileExtensionFromUrl(extention);
        String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(mimeTypeMap);

        if (mimeType != null) {
            return mimeType;
        } else {
            return "file/" + extention.replace(".", "");
        }
    }
}
