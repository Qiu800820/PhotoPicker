package me.iwf.photopicker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import me.iwf.photopicker.R;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.utils.ImageCaptureManager;

public class MovieRecorderView extends LinearLayout implements OnErrorListener {

    private static final int DEFAULT_MAX_VIDEO_TIME = 10 * 1000;
    private boolean isRecordEnabled = true;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private RoundProgressBar mProgressBar;
    private ImageView swipeCamera;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private Timer mTimer;// 计时器
    private OnRecordFinishListener mOnRecordFinishListener;// 录制完成回调接口

    private int mWidth;// 视频分辨率宽度
    private int mHeight;// 视频分辨率高度
    private boolean isOpenCamera;// 是否一开始就打开摄像头
    private int mRecordMaxTime;// 一次拍摄最长时间
    private long mTimeCount;// 时间计数
    private File mRecordFile = null;// 文件

    private static final String TAG = "MovieRecorderView";
    private boolean mWaitForTakePhoto;

    public MovieRecorderView(Context context) {
        this(context, null);
    }

    public MovieRecorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MovieRecorderView, defStyle, 0);
        mWidth = a.getInteger(R.styleable.MovieRecorderView_video_width, 320);// 默认320
        mHeight = a.getInteger(R.styleable.MovieRecorderView_video_height, 240);// 默认240

        isOpenCamera = a.getBoolean(R.styleable.MovieRecorderView_is_open_camera, true);// 默认打开
        mRecordMaxTime = a.getInteger(R.styleable.MovieRecorderView_record_max_time, DEFAULT_MAX_VIDEO_TIME);// 默认为10

        LayoutInflater.from(context).inflate(R.layout.__picker_movie_recorder_view, this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mProgressBar = (RoundProgressBar) findViewById(R.id.recorder_progress_view);
        mProgressBar.setMax(mRecordMaxTime);// 设置进度条最大量

        swipeCamera = (ImageView) findViewById(R.id.swipe_camera);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mProgressBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        if(isRecordEnabled) {
            mProgressBar.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    try {
                        record();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });
        }

        mProgressBar.setLongClickEndListener(new RoundProgressBar.LongClickEndListener() {
            @Override
            public void OnLongClickEndListener(View v) {
                saveRecorder();
            }
        });

        swipeCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    v.setSelected(!v.isSelected());
                    initCamera(v.isSelected() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        a.recycle();

    }

    private class CustomCallBack implements Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            try {
                initCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            freeCameraResource();
        }

    }

    /**
     * 初始化摄像头
     */
    private void initCamera(int cameraId) throws IOException {
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = Camera.open(cameraId);
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            e.printStackTrace();
            freeCameraResource();
        }
        if (mCamera == null)
            return;

        setCameraParams();
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();


    }

    /**
     * 设置摄像头为竖屏
     */
    private void setCameraParams() {
        if (mCamera != null) {
            Parameters params = mCamera.getParameters();
            params.set("rotation", 90);
            params.set("orientation", "portrait");
            mCamera.setParameters(params);
        }
    }

    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private void takePicture() {
        if (mCamera == null || mWaitForTakePhoto) {
            return;
        }
        mWaitForTakePhoto = true;
        mCamera.startPreview();
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "=== takePicture ===");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mRecordFile);
                    fos.write(data);
                    fos.flush();
                    if (mOnRecordFinishListener != null) {
                        mRecordFile = ImageCaptureManager.renameSuffix(MovieRecorderView.this.mRecordFile, "jpg");
                        mOnRecordFinishListener.onRecordFinish(mRecordFile, Media.FILE_TYPE_IMAGE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "=== takePicture ===", e);
                } finally {
                    if (fos != null)
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                mWaitForTakePhoto = false;
            }
        });
    }


    /**
     * 初始化
     */
    private void initRecord() throws IOException {

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        if (mCamera != null) {
            mMediaRecorder.setCamera(mCamera);
            mCamera.unlock();
        }
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(VideoSource.CAMERA);// 视频源
        mMediaRecorder.setAudioSource(AudioSource.MIC);// 音频源
        mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);// 视频输出格式
        mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);// 音频格式
        mMediaRecorder.setVideoSize(mWidth, mHeight);    // 设置分辨率：
        mMediaRecorder.setVideoEncodingBitRate(512 * 1024);// 设置帧频率，然后就清晰了
        mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
        mMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);// 视频录制格式
        mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
        mMediaRecorder.prepare();

        mMediaRecorder.start();
        swipeCamera.setVisibility(View.GONE);
    }

    public void setOnRecordFinishListener(OnRecordFinishListener mOnRecordFinishListener) {
        this.mOnRecordFinishListener = mOnRecordFinishListener;
    }

    private void record() {

        try {
            if (!isOpenCamera)// 如果未打开摄像头，则打开
                initCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            initRecord();
            mTimeCount = 0;// 时间计数器重新赋值
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    mTimeCount += 58;
                    mProgressBar.setProgress(mTimeCount);// 设置进度条
                    if (mTimeCount > mRecordMaxTime) {// 达到指定时间，停止拍摄
                        saveRecorder();
                    }
                }
            }, 0, 58);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止拍摄
     */
    public void stop() {
        stopRecord();
        releaseRecord();
        freeCameraResource();
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mTimer != null)
            mTimer.cancel();
        if (mProgressBar != null)
            mProgressBar.setProgress(0);
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
                swipeCamera.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveRecorder() {

        stopRecord();
        releaseRecord();

        if (mOnRecordFinishListener != null) {
            mRecordFile = ImageCaptureManager.renameSuffix(mRecordFile, "mp4");
            mOnRecordFinishListener.onRecordFinish(mRecordFile, Media.FILE_TYPE_VIDEO);
        }
    }

    /**
     * 释放资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    public void setRecordMaxTime(int mRecordMaxTime) {
        if (mRecordMaxTime != 0)
            this.mRecordMaxTime = mRecordMaxTime * 1000;
    }

    public void setRecordFile(File file) {
        mRecordFile = file;
        Log.d(TAG, String.format("=== 文件地址： %s===", mRecordFile));
    }

    public void setRecordEnabled(boolean isRecordEnabled){
        this.isRecordEnabled = isRecordEnabled;
    }

    @Override
    public void onError(MediaRecorder mr, int arg1, int arg2) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录制完成回调接口
     */
    public interface OnRecordFinishListener {
        void onRecordFinish(File file, int type);
    }

}
