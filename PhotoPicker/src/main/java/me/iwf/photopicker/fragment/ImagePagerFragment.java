package me.iwf.photopicker.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;

import me.iwf.photopicker.R;
import me.iwf.photopicker.adapter.PhotoPagerAdapter;
import me.iwf.photopicker.entity.Media;

/**
 * Created by donglua on 15/6/21.
 */
public class ImagePagerFragment extends Fragment {

    public final static String ARG_PATH = "PATHS";
    public final static String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";

    private ArrayList<Media> medias;
    private ViewPager mViewPager;
    private PhotoPagerAdapter mPagerAdapter;

    public final static long ANIM_DURATION = 200L;

    public final static String ARG_THUMBNAIL_TOP = "THUMBNAIL_TOP";
    public final static String ARG_THUMBNAIL_LEFT = "THUMBNAIL_LEFT";
    public final static String ARG_THUMBNAIL_WIDTH = "THUMBNAIL_WIDTH";
    public final static String ARG_THUMBNAIL_HEIGHT = "THUMBNAIL_HEIGHT";
    public final static String ARG_HAS_ANIM = "HAS_ANIM";

    private int thumbnailTop = 0;
    private int thumbnailLeft = 0;
    private int thumbnailWidth = 0;
    private int thumbnailHeight = 0;

    private boolean hasAnim = false;

    private final ColorMatrix colorizerMatrix = new ColorMatrix();

    private int currentItem = 0;


    public static ImagePagerFragment newInstance(ArrayList<Media> medias, int currentItem) {

        ImagePagerFragment f = new ImagePagerFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PATH, medias);
        args.putInt(ARG_CURRENT_ITEM, currentItem);
        args.putBoolean(ARG_HAS_ANIM, false);

        f.setArguments(args);

        return f;
    }


    public static ImagePagerFragment newInstance(ArrayList<Media> medias, int currentItem, int[] screenLocation, int thumbnailWidth, int thumbnailHeight) {

        ImagePagerFragment f = newInstance(medias, currentItem);

        f.getArguments().putInt(ARG_THUMBNAIL_LEFT, screenLocation[0]);
        f.getArguments().putInt(ARG_THUMBNAIL_TOP, screenLocation[1]);
        f.getArguments().putInt(ARG_THUMBNAIL_WIDTH, thumbnailWidth);
        f.getArguments().putInt(ARG_THUMBNAIL_HEIGHT, thumbnailHeight);
        f.getArguments().putBoolean(ARG_HAS_ANIM, false);

        return f;
    }


    public void setPhotos(ArrayList<Media> medias, int currentItem) {
        this.medias.clear();
        this.medias.addAll(medias);
        this.currentItem = currentItem;

        mViewPager.setCurrentItem(currentItem);
        mViewPager.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        medias = new ArrayList<>();

        Bundle bundle = getArguments();

        if (bundle != null) {
            ArrayList<Media> pathArr = bundle.getParcelableArrayList(ARG_PATH);
            medias.clear();
            if (pathArr != null) {
                medias = pathArr;
            }

            hasAnim = bundle.getBoolean(ARG_HAS_ANIM);
            currentItem = bundle.getInt(ARG_CURRENT_ITEM);
            thumbnailTop = bundle.getInt(ARG_THUMBNAIL_TOP);
            thumbnailLeft = bundle.getInt(ARG_THUMBNAIL_LEFT);
            thumbnailWidth = bundle.getInt(ARG_THUMBNAIL_WIDTH);
            thumbnailHeight = bundle.getInt(ARG_THUMBNAIL_HEIGHT);
        }

        mPagerAdapter = new PhotoPagerAdapter(medias);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.__picker_picker_fragment_image_pager, container, false);

        mViewPager = (ViewPager) rootView.findViewById(R.id.vp_photos);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(currentItem);
        mViewPager.setOffscreenPageLimit(5);

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null && hasAnim) {
            ViewTreeObserver observer = mViewPager.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    mViewPager.getLocationOnScreen(screenLocation);
                    thumbnailLeft = thumbnailLeft - screenLocation[0];
                    thumbnailTop = thumbnailTop - screenLocation[1];

                    runEnterAnimation();

                    return true;
                }
            });
        }


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                hasAnim = currentItem == position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return rootView;
    }


    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    private void runEnterAnimation() {
        final long duration = ANIM_DURATION;

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        mViewPager.setPivotX(0F);

        mViewPager.setPivotY(0);
        mViewPager.setScaleX((float) thumbnailWidth / mViewPager.getWidth());
        mViewPager.setScaleY((float) thumbnailHeight / mViewPager.getHeight());
        mViewPager.setTranslationX(thumbnailLeft);
        mViewPager.setTranslationY(thumbnailTop);

        // Animate scale and translation to go from thumbnail to full size
        mViewPager.animate()
                .setDuration(duration)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator());

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image from grayscale to full color.
        // This happens in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer = ObjectAnimator.ofFloat(ImagePagerFragment.this,
                "saturation", 0, 1);
        colorizer.setDuration(duration);
        colorizer.start();

    }


    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     *                  when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {

        if (!getArguments().getBoolean(ARG_HAS_ANIM, false) || !hasAnim) {
            endAction.run();
            return;
        }

        final long duration = ANIM_DURATION;

        // Animate image back to thumbnail size/location
        mViewPager.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .scaleX((float) thumbnailWidth / mViewPager.getWidth())
                .scaleY((float) thumbnailHeight / mViewPager.getHeight())
                .translationX(thumbnailLeft)
                .translationY(thumbnailTop)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        endAction.run();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image back to grayscale,
        // in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer =
                ObjectAnimator.ofFloat(ImagePagerFragment.this, "saturation", 1, 0);
        colorizer.setDuration(duration);
        colorizer.start();
    }


    /**
     * This is called by the colorizing animator. It sets a saturation factor that is then
     * passed onto a filter on the picture's drawable.
     *
     * @param value saturation
     */
    public void setSaturation(float value) {
        colorizerMatrix.setSaturation(value);
        ColorMatrixColorFilter colorizerFilter = new ColorMatrixColorFilter(colorizerMatrix);
        mViewPager.getBackground().setColorFilter(colorizerFilter);
    }


    public ViewPager getViewPager() {
        return mViewPager;
    }


    public ArrayList<Media> getMedia() {
        return medias;
    }


    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        medias = null;

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }
}
