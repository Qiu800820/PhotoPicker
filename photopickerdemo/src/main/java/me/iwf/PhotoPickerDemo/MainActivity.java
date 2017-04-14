package me.iwf.PhotoPickerDemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.event.ImageLoader;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DELETE_VIDEO = 1001;

    private PhotoAdapter photoAdapter;

    private ArrayList<Media> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PhotoPicker.initImageLoader(new ImageLoader() {
            @Override
            public void clear(ImageView ivPhoto) {

                Glide.clear(ivPhoto);
            }

            @Override
            public void pauseRequests() {
                Glide.with(MainActivity.this).pauseRequests();
            }

            @Override
            public void resumeRequests() {
                Glide.with(MainActivity.this).resumeRequests();
            }

            @Override
            public void load(Context context, File path, ImageView ivPhoto, int width, int height) {

                DrawableTypeRequest drawableTypeRequest = Glide.with(context).load(path);

                if (width > 0 && height > 0) {
                    drawableTypeRequest.override(width, height);
                }

                drawableTypeRequest.into(ivPhoto);
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        photoAdapter = new PhotoAdapter(this, selectedPhotos);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(9)
                        .setGridColumnCount(3)
                        .setShowVideo(true)
                        .start(MainActivity.this);
            }
        });

        findViewById(R.id.button_no_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(7)
                        .setShowCamera(false)
                        .setPreviewEnabled(false)
                        .start(MainActivity.this);
            }
        });

        findViewById(R.id.button_one_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(1)
                        .start(MainActivity.this);
            }
        });

        findViewById(R.id.button_photo_gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setShowCamera(true)
                        .setShowGif(true)
                        .start(MainActivity.this);
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                            PhotoPicker.builder()
                                    .setPhotoCount(PhotoAdapter.MAX)
                                    .setShowCamera(true)
                                    .setPreviewEnabled(false)
                                    .setSelected(selectedPhotos)
                                    .setShowVideo(true)
                                    .start(MainActivity.this);
                        } else {
                            Media media = photoAdapter.getItem(position);
                            if (media != null && media.getType() == Media.FILE_TYPE_VIDEO) {
                                startActivityForResult(VideoPlayActivity.getVideoPlayIntent(view.getContext(), media.getPath(), true, false), REQUEST_CODE_DELETE_VIDEO);
                            } else {
                                PhotoPreview.builder()
                                        .setPhotos(selectedPhotos)
                                        .setCurrentItem(position)
                                        .start(MainActivity.this);
                            }

                        }


                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            ArrayList<Media> photos = null;
            if (data != null) {
                photos = data.getParcelableArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();

            if (photos != null) {

                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DELETE_VIDEO) {
            selectedPhotos.clear();
            photoAdapter.notifyDataSetChanged();
        }


    }

}
