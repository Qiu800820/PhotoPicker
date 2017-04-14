package me.iwf.PhotoPickerDemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

public class VideoPlayActivity extends AppCompatActivity {

    private static final String EXTRA_KEY_VIDEO_URL = "video_url";
    private static final String EXTRA_KEY_SHOW_DELETE = "show_delete";
    private static final String EXTRA_KEY_SHOW_SAVE = "show_save";

    private String videoUrl;
    private boolean showDelete;
    private boolean showSave;


    VideoView videoPlay;
    ImageView deleteIv;
    ImageView saveIv;

    public static Intent getVideoPlayIntent(Context context, String videoUrl, boolean showDelete, boolean showSave){
        return new Intent(context, VideoPlayActivity.class)
                .putExtra(EXTRA_KEY_VIDEO_URL, videoUrl)
                .putExtra(EXTRA_KEY_SHOW_DELETE, showDelete)
                .putExtra(EXTRA_KEY_SHOW_SAVE, showSave);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        initVideoInfo();
        initVideoPlayView();
    }


    private void initVideoInfo() {
        videoUrl = getIntent().getStringExtra(EXTRA_KEY_VIDEO_URL);
        showDelete = getIntent().getBooleanExtra(EXTRA_KEY_SHOW_DELETE, false);
        showSave = getIntent().getBooleanExtra(EXTRA_KEY_SHOW_SAVE, false);
    }

    private void initVideoPlayView() {

        videoPlay = (VideoView) findViewById(R.id.video_play);
        deleteIv = (ImageView) findViewById(R.id.delete);
        saveIv = (ImageView) findViewById(R.id.save);


        deleteIv.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        saveIv.setVisibility(showSave ? View.VISIBLE : View.GONE);

        deleteIv.setOnClickListener(listener);
        saveIv.setOnClickListener(listener);

        videoPlay.setVideoPath(videoUrl);
        videoPlay.start();

    }


    View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.delete:
                    if(!showSave)
                        setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.save:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    };


}
