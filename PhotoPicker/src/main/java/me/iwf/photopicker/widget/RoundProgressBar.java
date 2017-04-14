package me.iwf.photopicker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import me.iwf.photopicker.R;

/**
 * Created by Administrator on 2017/4/10.
 */
public class RoundProgressBar extends View {

    /**
     * 按钮变化速率(0.1/s) * [1, 100]
     */
    private static final float scaleRate = 40;

    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint buttonPaint;

    private int roundColor;
    private int roundProgressColor;
    private int roundButtonColor;

    private float roundWidth;
    private float roundProgressWidth;
    private float roundButtonWidth;

    private float max;
    private float progress;
    private RectF rect;

    private LongClickEndListener longClickEndListener;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        //获取自定义属性和默认值
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.GRAY);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 38);
        roundButtonColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundButtonColor, Color.WHITE);
        roundButtonWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundButtonWidth, 32);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
        roundProgressWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundProgressWidth, 4);
        max = mTypedArray.getFloat(R.styleable.RoundProgressBar_max, 100);

        initPaint();

        rect = new RectF();

        mTypedArray.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centre = getWidth() / 2;
        float newRoundWith = roundWidth + (centre - roundWidth) * Math.min(1, (progress / max) * scaleRate);
        canvas.drawCircle(centre, centre, newRoundWith, backgroundPaint);
        canvas.drawCircle(centre, centre, roundButtonWidth * Math.max(0.5F, (1 - (progress / max) * scaleRate)), buttonPaint);

        float left = centre - newRoundWith + roundProgressWidth / 2;
        float right = centre + newRoundWith - roundProgressWidth / 2;
        rect.set(left, left, right, right);

        canvas.drawArc(rect, 270, 360 * progress / max, false, progressPaint);  //根据进度画圆弧

    }

    public synchronized float getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            max = 100;
            Log.d("RoundProgressBar", "max is not less than 0");
        }
        this.max = max;
    }


    public void initPaint() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);  //消除锯齿
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(roundProgressWidth); //设置圆环的宽度
        progressPaint.setColor(roundProgressColor);  //设置进度的颜色

        backgroundPaint = new Paint();
        backgroundPaint.setColor(roundColor);

        buttonPaint = new Paint();
        buttonPaint.setStrokeWidth(roundButtonWidth);
        buttonPaint.setColor(roundButtonColor);
    }

    /**
     * 获取进度.需要同步
     */
    public synchronized float getProgress() {
        return progress;
    }

    /**
     * 此为线程安全控件
     */
    public synchronized void setProgress(float progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }

        if (progress > max) {
            progress = progress % max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }

    private boolean isLongClick;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isLongClick && longClickEndListener != null) {
                    setProgress(0);
                    longClickEndListener.OnLongClickEndListener(this);
                    return true;
                }
        }
        return super.onTouchEvent(event);
    }

    public void setLongClickEndListener(LongClickEndListener longClickEndListener) {
        this.longClickEndListener = longClickEndListener;
    }

    @Override
    public boolean performLongClick() {
        return isLongClick = super.performLongClick();
    }

    public interface LongClickEndListener {
        void OnLongClickEndListener(View v);
    }
}
