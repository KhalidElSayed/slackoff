package com.klinker.android.slackoff.utils;

import android.webkit.MimeTypeMap;

public class Utils {

    public static String getMimeType(String url) {
        url = url.replace(" ", "\\ ");
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
