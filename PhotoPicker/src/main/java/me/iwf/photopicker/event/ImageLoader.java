package me.iwf.photopicker.event;

import android.content.Context;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Administrator on 2017/3/30.
 */

public interface ImageLoader {


    void load(Context context, File path, ImageView ivPhoto, int width, int height);

    void resumeRequests();

    void pauseRequests();

    void clear(ImageView ivPhoto);
}
