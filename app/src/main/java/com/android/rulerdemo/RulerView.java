package com.android.rulerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author yejue.li@fih-foxconn.com
 */
public class RulerView extends View {

    private static final String TAG = "RulerView";

    /**
     * 刻度线画笔
     */
    private Paint mLinePaint;

    /**
     * 指示数字画笔
     */
    private Paint mTextPaint;

    /**
     * 用于保存 mTextPaint 的字体规格
     */
    private Paint.FontMetrics mTextMetrics;

    /**
     * 指示线画笔
     */
    private Paint mIndicatorLinePaint;

    /**
     * 刻度尺滑动监听器
     */
    private OnMoveActionListener mOnMoveActionListener = null;

    /**
     * 将划线的横坐标初始化为最左边刻度线的横坐标
     */
    private int position = 20;

    /**
     * 最左边刻度线的横坐标
     */
    public static final int MIN_POSITION = 20;

    /**
     * 最右边刻度线的横坐标
     */
    public static final int MAX_POSITION = 1700;

    /**
     * FM 频道最小值位 87 * 10
     */
    private int minChannelValue = 870;

    /**
     * FM 频道最大值为 108 * 10
     */
    private int maxChannelValue = 1080;

    public RulerView(Context context) {
        super(context);
        init();
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.ruler_line_color, null));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);

        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.ruler_text_color, null));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(24);
        mTextMetrics = mTextPaint.getFontMetrics();

        mIndicatorLinePaint = new Paint();
        mIndicatorLinePaint.setColor(getResources().getColor(R.color.ruler_indicator_color, null));
        mIndicatorLinePaint.setAntiAlias(true);
        mIndicatorLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mIndicatorLinePaint.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(setMeasureWidth(widthMeasureSpec), setMeasureHeight(heightMeasureSpec));
    }

    private int setMeasureWidth(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        int result = Integer.MAX_VALUE;
        switch (mode) {
            case MeasureSpec.AT_MOST:
                size = Math.min(result, size);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                size = result;
                break;
        }
        return size;
    }

    private int setMeasureHeight(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        int result = Integer.MAX_VALUE;
        switch (mode) {
            case MeasureSpec.AT_MOST:
                size = Math.min(result, size);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                size = result;
                break;
        }
        return size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        for (int i = minChannelValue; i <= maxChannelValue; i++) {
            if (i % 10 == 0) {
                canvas.drawLine(20, 0, 20, 140, mLinePaint);

                String text = i / 10 + "";
                float textWidth = mTextPaint.measureText(text);
                canvas.drawText(text,
                        20 - textWidth / 2,
                        // 设置 text 的 baseline，此处使 text 的上边界刚好紧接刻度线的最下端
                        140 + mTextMetrics.bottom - mTextMetrics.top - mTextMetrics.descent,
                        mTextPaint);
            } else if (i % 5 == 0) {
                canvas.drawLine(20, 30, 20, 110, mLinePaint);
            } else {
                canvas.drawLine(20, 54, 20, 86, mLinePaint);
            }
            canvas.translate((float) 8, 0);
        }

        canvas.restore();

        canvas.drawLine(position, 0, position, 140, mIndicatorLinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                if (x < MIN_POSITION) {
                    setPosition(MIN_POSITION);
                } else if (x > MAX_POSITION) {
                    setPosition(MAX_POSITION);
                } else {
                    setPosition((int) x);
                }
                if (mOnMoveActionListener != null) {
                    mOnMoveActionListener.onMove(Double.parseDouble(String.format("%.1f",
                            getFmChannel())));
                }
                Log.d(TAG, "onTouchEvent: position: " + position);
                Log.d(TAG, "onTouchEvent: channel: " + getFmChannel());
                break;
            case MotionEvent.ACTION_CANCEL:
                setFmChannel(Double.parseDouble(String.format("%.1f", getFmChannel())));
                break;
            default:
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    public void setPosition(int pos) {
        position = pos;
        invalidate();
    }

    public void setFmChannel(double fmChannel) {
        int temp = (int) ((fmChannel - 87) * 80) + 20;
        setPosition(temp);
    }

    private double getFmChannel() {
        return ((position - 20.0) / 80.0 + 87.0);
    }

    public interface OnMoveActionListener {
        void onMove(double x);
    }

    public void setOnMoveActionListener(OnMoveActionListener onMoveActionListener) {
        mOnMoveActionListener = onMoveActionListener;
    }
}
