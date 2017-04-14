package me.iwf.photopicker.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.R;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.entity.MediaDirectory;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.SIZE;

/**
 * Created by donglua on 15/5/31.
 */
public class MediaStoreHelper {

    public final static int INDEX_ALL_PHOTOS = 0;


    public static void getPhotoDirs(FragmentActivity activity, Bundle args, PhotosResultCallback resultCallback) {
        activity.getSupportLoaderManager()
                .initLoader(0, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    }

    private static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Cursor>> {

        private Context context;
        private PhotosResultCallback resultCallback;

        public PhotoDirLoaderCallbacks(Context context, PhotosResultCallback resultCallback) {
            this.context = context;
            this.resultCallback = resultCallback;
        }

        @Override
        public Loader<List<Cursor>> onCreateLoader(int id, Bundle args) {
            return new MediaLoader(context, args.getBoolean(PhotoPicker.EXTRA_SHOW_GIF, false), args.getBoolean(PhotoPicker.EXTRA_SHOW_VIDEO, false));
        }

        @Override
        public void onLoadFinished(Loader<List<Cursor>> loader, List<Cursor> datas) {
            if (datas == null) return;
            List<MediaDirectory> directories = new ArrayList<>();
            MediaDirectory photoDirectoryAll = new MediaDirectory();
            photoDirectoryAll.setName(context.getString(R.string.__picker_all_image));
            photoDirectoryAll.setId("ALL");
            for (Cursor data : datas) {
                if (data == null) continue;

                while (data.moveToNext()) {

                    int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
                    String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                    String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                    String path = data.getString(data.getColumnIndexOrThrow(DATA));
                    int type = data.getString(data.getColumnIndexOrThrow(MIME_TYPE)).startsWith("video")
                            ?Media.FILE_TYPE_VIDEO:Media.FILE_TYPE_IMAGE;
                    long size = data.getInt(data.getColumnIndexOrThrow(SIZE));

                    if (size < 1) continue;

                    MediaDirectory photoDirectory = new MediaDirectory();
                    photoDirectory.setId(bucketId);
                    photoDirectory.setName(name);

                    if (!directories.contains(photoDirectory)) {
                        photoDirectory.setCoverPath(path);
                        photoDirectory.addPhoto(imageId, path, type);
                        photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                        directories.add(photoDirectory);
                    } else {
                        directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, path, type);
                    }
                    photoDirectoryAll.addPhoto(imageId, path, type);
                }
                if (photoDirectoryAll.getPhotoPaths().size() > 0) {
                    photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotoPaths().get(0));
                }

            }
            directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);
            if (resultCallback != null) {
                resultCallback.onResultCallback(directories);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Cursor>> loader) {
            loader.reset();
        }
    }


    public interface PhotosResultCallback {
        void onResultCallback(List<MediaDirectory> directories);
    }

}
