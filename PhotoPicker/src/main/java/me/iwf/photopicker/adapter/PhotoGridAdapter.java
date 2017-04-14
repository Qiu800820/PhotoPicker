package me.iwf.photopicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.R;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.entity.MediaDirectory;
import me.iwf.photopicker.event.OnCameraListener;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.event.OnPhotoClickListener;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;
import me.iwf.photopicker.utils.MediaStoreHelper;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder> {


    private OnItemCheckListener onItemCheckListener = null;
    private OnPhotoClickListener onPhotoClickListener = null;
    private OnCameraListener onCameraClickListener = null;

    public final static int ITEM_TYPE_CAMERA = 100;
    public final static int ITEM_TYPE_PHOTO = 101;
    public final static int ITEM_TYPE_VIDEO = 102;
    private final static int COL_NUMBER_DEFAULT = 3;

    private boolean hasCamera = true;
    private boolean hasVideo = true;
    private boolean previewEnable = true;

    private int imageSize;
    private int columnNumber = COL_NUMBER_DEFAULT;


    public PhotoGridAdapter(Context context, List<MediaDirectory> photoDirectories) {
        this.photoDirectories = photoDirectories;
        setColumnNumber(context, columnNumber);
    }

    public PhotoGridAdapter(Context context, List<MediaDirectory> photoDirectories, ArrayList<Media> orginalPhotos, int colNum) {
        this(context, photoDirectories);
        setColumnNumber(context, colNum);
        selectedPhotos = new ArrayList<>();
        if (orginalPhotos != null) selectedPhotos.addAll(orginalPhotos);
    }

    private void setColumnNumber(Context context, int columnNumber) {
        this.columnNumber = columnNumber;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNumber;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera() && position == 0)
            return ITEM_TYPE_CAMERA;
        List<Media> mediaList = getCurrentPhotos();
        Media media = mediaList.get(showCamera() ? position - 1 : position);

        return (media.getType() == Media.FILE_TYPE_VIDEO ? ITEM_TYPE_VIDEO : ITEM_TYPE_PHOTO);
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == ITEM_TYPE_VIDEO) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.__picker_item_video, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.__picker_item_photo, parent, false);
        }

        PhotoViewHolder holder = new PhotoViewHolder(itemView);
        if (viewType == ITEM_TYPE_CAMERA) {
            holder.vSelected.setVisibility(View.GONE);
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onCameraClickListener != null) {
                        onCameraClickListener.onClick(view, hasVideo);
                    }
                }
            });
        }

        return holder;
    }


    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {
        if (getItemViewType(position) != ITEM_TYPE_CAMERA) {

            List<Media> medias = getCurrentPhotos();
            final Media media;

            if (showCamera()) {
                media = medias.get(position - 1);
            } else {
                media = medias.get(position);
            }

            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

            if (canLoadImage && media != null && !TextUtils.isEmpty(media.getPath())) {
                PhotoPicker.getImageLoader().load(holder.ivPhoto.getContext(), new File(media.getPath()), holder.ivPhoto, imageSize, imageSize);

            }

            final boolean isChecked = isSelected(media);

            holder.vSelected.setSelected(isChecked);
            holder.ivPhoto.setSelected(isChecked);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPhotoClickListener != null) {
                        int pos = holder.getAdapterPosition();
                        if (previewEnable) {
                            onPhotoClickListener.onClick(view, pos, showCamera());
                        } else {
                            holder.vSelected.performClick();
                        }
                    }
                }
            });
            holder.vSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    boolean isEnable = true;

                    if (onItemCheckListener != null) {
                        isEnable = onItemCheckListener.onItemCheck(pos, media,
                                getSelectedPhotos().size() + (isSelected(media) ? -1 : 1));
                    }
                    if (isEnable) {
                        toggleSelection(media);
                        notifyItemChanged(pos);
                    }
                }
            });

        } else {
            holder.ivPhoto.setImageResource(R.drawable.__picker_camera);
        }
    }


    @Override
    public int getItemCount() {
        int photosCount =
                photoDirectories.size() == 0 ? 0 : getCurrentPhotos().size();
        if (showCamera()) {
            return photosCount + 1;
        }
        return photosCount;
    }


    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
        }
    }


    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }


    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }


    public void setOnCameraClickListener(OnCameraListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }


    public ArrayList<Media> getSelectedPhotoPaths() {
        return selectedPhotos;
    }


    public void setShowCamera(boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

    public void setShowVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public void setPreviewEnable(boolean previewEnable) {
        this.previewEnable = previewEnable;
    }

    public boolean showCamera() {
        return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }

    @Override
    public void onViewRecycled(PhotoViewHolder holder) {
        PhotoPicker.getImageLoader().clear(holder.ivPhoto);
        super.onViewRecycled(holder);
    }
}
