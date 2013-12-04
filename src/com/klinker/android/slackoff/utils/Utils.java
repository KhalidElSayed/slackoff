package com.klinker.android.slackoff.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
}
