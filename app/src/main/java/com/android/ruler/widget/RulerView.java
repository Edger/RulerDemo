package com.android.ruler.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.ruler.R;
import com.android.ruler.util.DimensUtil;

/**
 * 刻度尺自定义 View
 * 刻度值逢 1 画长竖线，逢 0.5 画中竖线，逢 0.1 画短竖线
 * 刻度值每逢 1 画出刻度值
 *
 * @author Edger Lee
 */
public class RulerView extends View {

    private static final String TAG = "RulerView";
    /**
     * 画短、中刻度线的画笔，逢 0.1 和 0.5 时使用
     */
    private Paint mThinLinePaint;
    /**
     * 画长刻度线的画笔，逢 1 时使用
     */
    private Paint mBoldLinePaint;
    /**
     * 指示线画笔
     */
    private Paint mIndicatorLinePaint;
    /**
     * 指示数字画笔
     */
    private Paint mTextPaint;
    /**
     * 用于保存 mTextPaint 的字体规格
     */
    private Paint.FontMetrics mTextMetrics;
    /**
     * 中短刻度线的线宽
     */
    private final static int LINE_WIDTH_THIN = 2;
    /**
     * 长刻度线的线宽
     */
    private final static int LINE_WIDTH_BOLD = 3;
    /**
     * 刻度值指示线的线宽
     */
    private final static int LINE_WIDTH_SELECTOR = 5;
    /**
     * 刻度线与顶部的距离
     * 单位：px
     */
    private static int mLineMarginTop;
    /**
     * 最长刻度线的长度，中刻度线取该值的 0.66 倍，短刻度线取该值的 0.33 倍
     * 单位：px
     */
    private static int mLineVerticalHeight;
    /**
     * 刻度线的物理水平间隔
     * 单位：px
     */
    private float mLineHorizontalSpace = 8;
    /**
     * 左边第一根刻度线的坐标
     */
    private float mFirstLineX;
    /**
     * 绘制刻度线的起始纵坐标
     */
    private float mLineStartY;
    /**
     * 指示线的 X 轴坐标
     */
    private float mIndicatorX;
    /**
     * 最小频道值
     */
    private float mChannelMin = 87.5f;
    /**
     * 最大频道值
     */
    private float mChannelMax = 108.0f;
    /**
     * 频道的刻度值
     */
    private float mChannelScale = 0.1f;
    /**
     * 当前频道值
     */
    private float mCurrentChannel = mChannelMin;
    /**
     * 触发触摸事件时的 X 轴坐标
     */
    private float actionX;
    /**
     * 刻度尺滑动监听器
     */
    private OnChannelChangedListener mOnChannelChangedListener = null;

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

    /**
     * 初始化画笔工具等
     */
    private void init() {
        mThinLinePaint = new Paint();
        mThinLinePaint.setColor(getResources().getColor(R.color.ruler_line_color, null));
        mThinLinePaint.setAntiAlias(true);
        mThinLinePaint.setStyle(Paint.Style.STROKE);
        mThinLinePaint.setStrokeWidth(getResources().getDimension(R.dimen.radio_channel_selector_thin_line_width));

        mBoldLinePaint = new Paint();
        mBoldLinePaint.setColor(getResources().getColor(R.color.ruler_line_color, null));
        mBoldLinePaint.setAntiAlias(true);
        mBoldLinePaint.setStyle(Paint.Style.STROKE);
        mBoldLinePaint.setStrokeWidth(getResources().getDimension(R.dimen.radio_channel_selector_thin_line_width));

        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.ruler_text_color, null));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(getResources().getDimension(R.dimen.radio_channel_selector_thin_line_width));
        mTextPaint.setTextSize(24);
        // 获取 mTextPaint 的字体规格
        mTextMetrics = mTextPaint.getFontMetrics();

        mIndicatorLinePaint = new Paint();
        mIndicatorLinePaint.setColor(getResources().getColor(R.color.ruler_indicator_color, null));
        mIndicatorLinePaint.setAntiAlias(true);
        mIndicatorLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mIndicatorLinePaint.setStrokeWidth(getResources().getDimension(R.dimen.radio_channel_selector_bold_line_width));

        mLineMarginTop = (int) DimensUtil.dp2px(8);
        mLineVerticalHeight = (int) DimensUtil.dp2px(30);
        mLineStartY = mLineMarginTop;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(setMeasureWidth(widthMeasureSpec),
                setMeasureHeight(heightMeasureSpec));
    }

    /**
     * 设置 View 的宽度
     *
     * @param spec 测量参数
     * @return View 的宽度
     */
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

    /**
     * 设置 View 的高度
     *
     * @param spec 测量参数
     * @return View 的高度
     */
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

        mFirstLineX = getFirstLineX();

        for (int i = 0; i <= getLinesCount(); i++) {
            if (i % 10 == 5) {
                // 绘制长刻度线
                canvas.drawLine(mFirstLineX, mLineStartY, mFirstLineX,
                        mLineStartY + mLineVerticalHeight, mBoldLinePaint);

                @SuppressLint("DefaultLocale")
                String text = String.format("%.0f", mChannelMin + mChannelScale * i);
                float textWidth = mTextPaint.measureText(text);
                canvas.drawText(text,
                        mFirstLineX - textWidth / 2,
                        // 设置 text 的 baseline，此处使 text 的上边界刚好紧接刻度线的最下端
                        mLineStartY + mLineVerticalHeight + mTextMetrics.bottom - mTextMetrics.top - mTextMetrics.descent,
                        mTextPaint);
            } else if (i % 10 == 0) {
                // 绘制中刻度线
                canvas.drawLine(mFirstLineX, mLineStartY, mFirstLineX,
                        mLineStartY + mLineVerticalHeight * 0.67f, mThinLinePaint);
            } else {
                // 绘制短刻度线
                canvas.drawLine(mFirstLineX, mLineStartY, mFirstLineX,
                        mLineStartY + mLineVerticalHeight * 0.33f, mThinLinePaint);
            }
            mFirstLineX += mLineHorizontalSpace;
        }

        canvas.restore();

        canvas.drawLine(mIndicatorX, 0, mIndicatorX, getHeight(), mIndicatorLinePaint);

        if (mOnChannelChangedListener != null) {
            mOnChannelChangedListener.onChannelChanged(getCurrentChannel());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 滑动距离
                float movingDistance = event.getX() - actionX;
                // 频道变化值
                float channelDelta =
                        movingDistance / getTotalWidth() * (mChannelMax - mChannelMin);
                mCurrentChannel += channelDelta;
                mIndicatorX += movingDistance;
                // 不要让指示线超出刻度尺的两端
                if (mIndicatorX < getFirstLineX()) {
                    setIndicatorX(getFirstLineX());
                    mCurrentChannel = mChannelMin;
                    break;
                }
                if (mIndicatorX > getLastLineX()) {
                    setIndicatorX(getLastLineX());
                    mCurrentChannel = mChannelMax;
                    break;
                }
                setIndicatorX(mIndicatorX);
                // 保存最新的触摸坐标，用于下一次移动的比较
                actionX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                // 使指示线只停在刻度线上
                setCurrentChannel(formatChannelOfFloat(mCurrentChannel));
                Log.i(TAG, String.format("ACTION_UP: channel %.1f", getCurrentChannel()));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 解决刻度尺和 ViewPager 的滑动冲突
        // 当滑动刻度尺时，告知父控件不要拦截事件， 交给子 View 处理
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    /**
     * 算出刻度线的理论总根数
     *
     * @return 刻度线总根数
     */
    private int getLinesCount() {
        return (int) ((mChannelMax - mChannelMin) / mChannelScale + 0.5f);
    }

    /**
     * 算出整个刻度尺的理论宽度，取屏幕宽度的 0.95 倍
     *
     * @return 刻度尺的理论宽度
     */
    private float getTotalWidth() {
        mLineHorizontalSpace = (float) getWidth() * 0.95f / getLinesCount();
        return getLinesCount() * mLineHorizontalSpace;
    }

    /**
     * 获取第一根刻度线的 X 轴坐标
     *
     * @return 第一根刻度线的 X 轴坐标
     */
    private float getFirstLineX() {
        mFirstLineX = getWidth() / 2.0f - getTotalWidth() / 2.0f;
        return mFirstLineX;
    }

    /**
     * 获取最后一根刻度线的 X 轴坐标
     *
     * @return 最后一根刻度线的 X 轴坐标
     */
    private float getLastLineX() {
        return mFirstLineX + getTotalWidth();
    }

    /**
     * 设置指示线的 X 轴坐标
     *
     * @param x X 轴坐标
     */
    public void setIndicatorX(float x) {
        mIndicatorX = x;
        invalidate();
    }

    /**
     * 设置频道值
     *
     * @param newChannel 待设置的频道值
     */
    public void setCurrentChannel(Float newChannel) {
        if (newChannel < mChannelMin) {
            newChannel = mChannelMin;
        }
        if (newChannel > mChannelMax) {
            newChannel = mChannelMax;
        }
        // 保留一位小数点
        mCurrentChannel = (float) (Math.round(newChannel * 10)) / 10;
        float temp =
                getFirstLineX() + (mCurrentChannel - mChannelMin) / (mChannelMax - mChannelMin) * getTotalWidth();
        setIndicatorX(temp);
    }

    /**
     * 获取当前的频道值
     *
     * @return 当前的频道值
     */
    private float getCurrentChannel() {
        return formatChannelOfFloat(mCurrentChannel);
    }

    /**
     * @param channel 传入的刻度值
     * @return 处理后只保留一位小数的刻度值
     */
    private float formatChannelOfFloat(float channel) {
        return (float) (Math.round(channel * 10)) / 10;
    }

    public interface OnChannelChangedListener {
        /**
         * 刻度尺滑动监听的回调
         *
         * @param newChannel 改变后后的刻度值
         */
        void onChannelChanged(float newChannel);
    }

    public void setOnChannelChangedListener(OnChannelChangedListener channelChangedListener) {
        mOnChannelChangedListener = channelChangedListener;
    }
}
