package me.iwf.photopicker.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.R;
import me.iwf.photopicker.adapter.PhotoGridAdapter;
import me.iwf.photopicker.adapter.PopupDirectoryListAdapter;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.entity.MediaDirectory;
import me.iwf.photopicker.event.OnCameraListener;
import me.iwf.photopicker.event.OnPhotoClickListener;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;
import me.iwf.photopicker.utils.ImageCaptureManager;
import me.iwf.photopicker.utils.MediaStoreHelper;
import me.iwf.photopicker.utils.PermissionsConstant;
import me.iwf.photopicker.utils.PermissionsUtils;

import static android.app.Activity.RESULT_OK;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_VIDEO;
import static me.iwf.photopicker.utils.MediaStoreHelper.INDEX_ALL_PHOTOS;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoPickerFragment extends Fragment {

    private ImageCaptureManager captureManager;
    private PhotoGridAdapter photoGridAdapter;

    private PopupDirectoryListAdapter listAdapter;
    //所有photos的路径
    private List<MediaDirectory> directories;
    //传入的已选照片
    private ArrayList<Media> originalPhotos;

    private int SCROLL_THRESHOLD = 30;
    int column;
    //目录弹出框的一次最多显示的目录数目
    public static int COUNT_MAX = 4;
    private final static String EXTRA_CAMERA = "camera";
    private final static String EXTRA_COLUMN = "column";
    private final static String EXTRA_COUNT = "count";
    private final static String EXTRA_GIF = "gif";
    private final static String EXTRA_VIDEO = "video";
    private final static String EXTRA_ORIGIN = "origin";
    private ListPopupWindow listPopupWindow;
    private boolean showVideo;

    public static PhotoPickerFragment newInstance(boolean showCamera, boolean showGif, boolean showVideo,
                                                  boolean previewEnable, int column, int maxCount, ArrayList<Media> originalPhotos) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_CAMERA, showCamera);
        args.putBoolean(EXTRA_GIF, showGif);
        args.putBoolean(EXTRA_VIDEO, showVideo);
        args.putBoolean(EXTRA_PREVIEW_ENABLED, previewEnable);
        args.putInt(EXTRA_COLUMN, column);
        args.putInt(EXTRA_COUNT, maxCount);
        args.putParcelableArrayList(EXTRA_ORIGIN, originalPhotos);
        PhotoPickerFragment fragment = new PhotoPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        directories = new ArrayList<>();
        originalPhotos = getArguments().getParcelableArrayList(EXTRA_ORIGIN);

        column = getArguments().getInt(EXTRA_COLUMN, DEFAULT_COLUMN_NUMBER);
        showVideo = getArguments().getBoolean(EXTRA_VIDEO);
        boolean showCamera = getArguments().getBoolean(EXTRA_CAMERA, true);
        boolean previewEnable = getArguments().getBoolean(EXTRA_PREVIEW_ENABLED, true);
        boolean showGif = getArguments().getBoolean(EXTRA_GIF);

        photoGridAdapter = new PhotoGridAdapter(getActivity(), directories, originalPhotos, column);
        photoGridAdapter.setShowCamera(showCamera);
        photoGridAdapter.setPreviewEnable(previewEnable);
        photoGridAdapter.setShowVideo(showVideo);


        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                final int index = showCamera ? position - 1 : position;

                ArrayList<Media> photos = photoGridAdapter.getCurrentPhotos();

                int[] screenLocation = new int[2];
                v.getLocationOnScreen(screenLocation);
                ImagePagerFragment imagePagerFragment =
                        ImagePagerFragment.newInstance(photos, index, screenLocation, v.getWidth(),
                                v.getHeight());

                ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);
            }
        });

        photoGridAdapter.setOnCameraClickListener(new OnCameraListener() {
            @Override
            public void onClick(View v, boolean showVideo) {
                if (!PermissionsUtils.checkCameraPermission(PhotoPickerFragment.this)) return;
                if (!PermissionsUtils.checkWriteStoragePermission(PhotoPickerFragment.this)) return;
                if (showVideo)
                    chooseCaptureType();
                else
                    openCamera();
            }
        });

        listAdapter = new PopupDirectoryListAdapter(directories);

        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, showGif);
        mediaStoreArgs.putBoolean(EXTRA_SHOW_VIDEO, showVideo);
        if (directories.isEmpty()) {
            MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                    new MediaStoreHelper.PhotosResultCallback() {
                        @Override
                        public void onResultCallback(List<MediaDirectory> dirs) {
                            //防止脏数据
                            if (!directories.isEmpty())
                                return;
                            directories.clear();
                            directories.addAll(dirs);
                            photoGridAdapter.notifyDataSetChanged();
                            listAdapter.notifyDataSetChanged();
                            adjustHeight();
                        }
                    });
        }
        captureManager = new ImageCaptureManager(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        Log.d("PhotoPicker", "onCreateView -> RecyclerView setAdapter");
        recyclerView.setAdapter(photoGridAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final Button btSwitchDirectory = (Button) rootView.findViewById(R.id.button);

        listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setAnchorView(btSwitchDirectory);
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);
        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();

                MediaDirectory directory = directories.get(position);

                btSwitchDirectory.setText(directory.getName());

                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });


        btSwitchDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    adjustHeight();
                    listPopupWindow.show();
                }
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    PhotoPicker.getImageLoader().pauseRequests();
                } else {
                    resumeRequestsIfNotDestroyed();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    resumeRequestsIfNotDestroyed();
                }
            }
        });

        return rootView;
    }

    private void chooseCaptureType() {
        try {
            Intent intent = captureManager.dispatchTakeMediaIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_MEDIA);
        } catch (Exception e) {
            new AlertDialog.Builder(getContext()).setSingleChoiceItems(getResources().getStringArray(R.array.capture_type), 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        openCamera();
                        dialog.dismiss();
                    } else {
                        openVideo();
                        dialog.dismiss();
                    }
                }
            }).show();
        }
    }

    private void openCamera() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openVideo() {
        try {
            Intent intent = captureManager.dispatchTakeVideoCaptureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_VIDEO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("PhotoPickerFragment", "requestCode :" + requestCode + "resultCode :" + resultCode);
        int type;
        String path;

        if (captureManager == null) {
            FragmentActivity activity = getActivity();
            captureManager = new ImageCaptureManager(activity);
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImageCaptureManager.REQUEST_TAKE_VIDEO:
                    type = Media.FILE_TYPE_VIDEO;
                    path = captureManager.getCurrentPhotoPath();
                    break;
                case ImageCaptureManager.REQUEST_TAKE_MEDIA:
                    type = data.getIntExtra(PhotoPicker.EXTRA_MEDIA_TYPE, 0);
                    path = data.getStringExtra(MediaStore.EXTRA_OUTPUT);
                    break;
                default:
                    type = Media.FILE_TYPE_IMAGE;
                    path = captureManager.getCurrentPhotoPath();
                    break;
            }

            captureManager.galleryAddPic(path);
            if (directories.size() > 0) {
                Log.d("PhotoPickerFragment", "path :" + path);
                MediaDirectory directory = directories.get(INDEX_ALL_PHOTOS);
                directory.getMedias().add(0, new Media(path.hashCode(), path, type));
                directory.setCoverPath(path);
                photoGridAdapter.notifyDataSetChanged();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PermissionsConstant.REQUEST_CAMERA:
                case PermissionsConstant.REQUEST_EXTERNAL_WRITE:
                    if (PermissionsUtils.checkWriteStoragePermission(this) &&
                            PermissionsUtils.checkCameraPermission(this)) {
                        if (showVideo)
                            chooseCaptureType();
                        else
                            openCamera();
                    }
                    break;
            }
        }
    }

    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    public void adjustHeight() {
        if (listAdapter == null) return;
        int count = listAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;
        if (listPopupWindow != null) {
            listPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (directories == null) {
            return;
        }

        for (MediaDirectory directory : directories) {
            directory.getPhotoPaths().clear();
            directory.getMedias().clear();
            directory.setMedias(null);
        }
        directories.clear();
        directories = null;
    }

    private void resumeRequestsIfNotDestroyed() {
        if (!AndroidLifecycleUtils.canLoadImage(this)) {
            return;
        }

        PhotoPicker.getImageLoader().resumeRequests();
    }
}
