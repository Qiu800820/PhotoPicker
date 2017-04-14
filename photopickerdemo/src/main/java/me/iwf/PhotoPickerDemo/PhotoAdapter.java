package me.iwf.PhotoPickerDemo;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private ArrayList<Media> photoPaths = new ArrayList<>();
    private LayoutInflater inflater;

    private Context mContext;

    final static int TYPE_ADD = 1;
    final static int TYPE_MEDIA = 2;

    final static int MAX = 9;

    public PhotoAdapter(Context mContext, ArrayList<Media> photoPaths) {
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);

    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case TYPE_ADD:
                itemView = inflater.inflate(me.iwf.PhotoPickerDemo.R.layout.item_add, parent, false);
                break;
            case TYPE_MEDIA:
                itemView = inflater.inflate(R.layout.item_media, parent, false);
                break;
        }
        return new PhotoViewHolder(itemView);
    }

    public Media getItem(int position){
        if(photoPaths == null || position >= photoPaths.size())
            return null;
        return photoPaths.get(position);
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        Media media = getItem(position);
        if (getItemViewType(position) == TYPE_MEDIA) {
            Uri uri = Uri.fromFile(new File(media.getPath()));

            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

            holder.play.setVisibility(media.getType() == Media.FILE_TYPE_VIDEO? View.VISIBLE:View.GONE);

            if (canLoadImage) {
                Glide.with(mContext)
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                        .error(R.drawable.__picker_ic_broken_image_black_48dp)
                        .into(holder.ivPhoto);
            }
        }
    }



    @Override
    public int getItemCount() {
        int count = photoPaths.size() + 1;
        if (count > MAX) {
            count = MAX;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == photoPaths.size() && position != MAX) ? TYPE_ADD : TYPE_MEDIA;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View play;


        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.image);
            play = itemView.findViewById(R.id.play);
        }
    }

}
