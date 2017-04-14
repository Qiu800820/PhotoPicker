package me.iwf.photopicker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.iwf.photopicker.PhotoPicker;

/**
 * Created by donglua on 15/6/23.
 * <p>
 * <p>
 * http://developer.android.com/training/camera/photobasics.html
 */
public class ImageCaptureManager {

    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_TAKE_VIDEO = 2;
    public static final int REQUEST_TAKE_MEDIA = 3;

    private String mCurrentPhotoPath;
    private Context mContext;

    public ImageCaptureManager(Context mContext) {
        this.mContext = mContext;
    }

    private File createMediaFile(String suffix) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String mediaFileName = "media" + timeStamp + "." + suffix;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                Log.e("TAG", "Throwing Errors....");
                throw new IOException();
            }
        }

        File media = new File(storageDir, mediaFileName);

        mCurrentPhotoPath = media.getAbsolutePath();
        return media;
    }

    private Uri createMediaUri(File file) {
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = mContext.getApplicationInfo().packageName + ".provider";
            uri = FileProvider.getUriForFile(this.mContext.getApplicationContext(), authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public Intent dispatchTakeMediaIntent() throws IOException {
        Intent takeMediaIntent = new Intent(PhotoPicker.ACTION_MEDIA_CAPTURE);
        if (takeMediaIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File file = createMediaFile("media");
            Uri videoUri = Uri.fromFile(file);
            if (videoUri != null) {
                takeMediaIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                takeMediaIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            } else {
                throw new IOException("output is not empty");
            }
        } else {
            throw new IOException("not resolve activity");
        }


        return takeMediaIntent;
    }

    public Intent dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File file = createMediaFile("jpg");
            Uri photoUri = createMediaUri(file);

            if (photoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            } else {
                throw new IOException("output is not empty");
            }
        } else {
            throw new IOException("not resolve activity");
        }
        return takePictureIntent;
    }

    public Intent dispatchTakeVideoCaptureIntent() throws IOException {
        Intent takeVideoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoCaptureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File file = createMediaFile("mp4");
            Uri videoUri = createMediaUri(file);
            if (videoUri != null) {
                takeVideoCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                takeVideoCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                takeVideoCaptureIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            } else {
                throw new IOException("output is not empty");
            }
        } else {
            throw new IOException("not resolve activity");
        }
        return takeVideoCaptureIntent;
    }


    public void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        if (TextUtils.isEmpty(path)) {
            return;
        }

        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }


    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
    }

    public static File renameSuffix(File file, String suffix) {
        try {
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf(".")) + "." + suffix;
            File newFile = new File(file.getParent(), name);
            if (file.renameTo(newFile))
                return newFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

}
