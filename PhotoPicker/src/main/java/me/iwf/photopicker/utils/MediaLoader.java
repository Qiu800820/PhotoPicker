package me.iwf.photopicker.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/5.
 */

public class MediaLoader extends AsyncTaskLoader<List<Cursor>> {

    private PhotoDirectoryLoader photoDirectoryLoader;
    private VideoDirectoryLoader videoDirectoryLoader;
    private List<Cursor> cursors;


    public MediaLoader(Context context, boolean showGif, boolean showVideo) {
        super(context);
        photoDirectoryLoader = new PhotoDirectoryLoader(context, showGif);
        if(showVideo){
            videoDirectoryLoader = new VideoDirectoryLoader(context);
        } else{
           videoDirectoryLoader = null;
        }
    }

    @Override
    public List<Cursor> loadInBackground() {
        cursors = new ArrayList<>(2);
        Cursor photoCursor = photoDirectoryLoader.loadInBackground();
        cursors.add(photoCursor);
        if(videoDirectoryLoader != null){
            Cursor videoCursor = videoDirectoryLoader.loadInBackground();
            cursors.add(0, videoCursor);
        }
        return cursors;
    }
    @Override
    protected void onStartLoading() {
        if (cursors != null) {
            deliverResult(cursors);
        }
        if (takeContentChanged() || cursors == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<Cursor> cursors) {
        if(cursors != null){
            for(Cursor cursor : cursors){
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }

    @Override
    protected void onReset() {

        onStopLoading();
        if(cursors != null)
            for(Cursor mCursor : cursors){
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
        cursors = null;
    }

}
