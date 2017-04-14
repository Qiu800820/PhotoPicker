package me.iwf.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.R;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private ArrayList<Media> paths = new ArrayList<>();

    public PhotoPagerAdapter(ArrayList<Media> paths) {
        this.paths = paths;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.__picker_picker_item_pager, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_pager);

        final Media path = paths.get(position);

        boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(context);

        if (canLoadImage && path != null && !TextUtils.isEmpty(path.getPath())) {
            PhotoPicker.getImageLoader().load(imageView.getContext(), new File(path.getPath()), imageView, imageView.getWidth(), imageView.getHeight());
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing()) {
                        ((Activity) context).onBackPressed();
                    }
                }
            }
        });

        container.addView(itemView);

        return itemView;
    }


    @Override
    public int getCount() {
        return paths.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
        PhotoPicker.getImageLoader().clear((ImageView) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
