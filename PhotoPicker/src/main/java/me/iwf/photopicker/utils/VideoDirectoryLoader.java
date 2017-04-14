package me.iwf.photopicker.utils;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.CursorLoader;

import static android.provider.MediaStore.MediaColumns.MIME_TYPE;


/**
 * Created by 黄东鲁 on 15/6/28.
 */
public class VideoDirectoryLoader extends CursorLoader {

    final String[] IMAGE_PROJECTION = {
            Media._ID,
            Media.DATA,
            Media.BUCKET_ID,
            Media.BUCKET_DISPLAY_NAME,
            Media.DATE_ADDED,
            Media.MIME_TYPE,
            Media.SIZE
    };

    public VideoDirectoryLoader(Context context) {
        super(context);

        setProjection(IMAGE_PROJECTION);
        setUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        setSortOrder(Media.DATE_ADDED + " DESC");

        String selection = String.format("%s > 100000 and %s < 50000000 and ( %s =? or %s =? or %s =? or %s =?)", Media.SIZE, Media.SIZE,
                MIME_TYPE, MIME_TYPE, MIME_TYPE, MIME_TYPE);

        setSelection(selection);
        String[] selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg", "video/mp4"};

        setSelectionArgs(selectionArgs);
    }


    private VideoDirectoryLoader(Context context, Uri uri, String[] projection, String selection,
                                 String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }


}
