package com.android.ruler.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.ruler.util.UiUtil;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

/**
 * @author Edger Lee
 */
public class RulerView2 extends View implements IRulerView {

    private static final String TAG = "RulerView2";

    /**
     * 画短、中刻度线的画笔，逢 0.1 和 0.5 时使用
     */
    private Paint mThinLinePaint;
    /**
     * 画长刻度线的画笔，逢 1 时使用
     */
    private Paint mBoldLinePaint;
    /**
     * 画刻度指示线的画笔
     */
    private Paint mIndicatorLinePaint;
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
    private float mLineHorizontalSpace = 20;
    /**
     * 左边第一根刻度线的坐标，指示值 FM：87.5 / AM：540.0
     */
    private float mFirstLineX;
    /**
     * 绘制刻度线的起始纵坐标
     */
    private float mLineStartY;
    /**
     * 用于保存刻度尺指示线的 X 轴坐标
     */
    private float mSelectorX = 0;
    /**
     * 刻度尺滑动监听器
     */
    private OnChannelChangeListener mOnChannelChangeListener;
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
     * 画长刻度线处的刻度值的位数，便于对 FM/AM 做不同的处理
     */
    private int mRemainderAtLongLine = 5;
    /**
     * 画中刻度线处的刻度值的位数，便于对 FM/AM 做不同的处理
     */
    private int mRemainderAtMiddleLine = 0;
    /**
     * 触发触摸事件时的 X 轴坐标
     */
    private float actionX;

    public RulerView2(Context context) {
        super(context);
        init();
    }

    public RulerView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mThinLinePaint = new Paint();
        mThinLinePaint.setColor(Color.parseColor("#9E9E9E"));
        mThinLinePaint.setStrokeWidth(LINE_WIDTH_THIN);

        mBoldLinePaint = new Paint();
        mBoldLinePaint.setColor(Color.parseColor("#909090"));
        mBoldLinePaint.setStrokeWidth(LINE_WIDTH_BOLD);

        mIndicatorLinePaint = new Paint();
        mIndicatorLinePaint.setColor(Color.parseColor("#E64D14"));
        mIndicatorLinePaint.setStrokeWidth(LINE_WIDTH_SELECTOR);

        mLineMarginTop = (int) UiUtil.dp2px(8);
        mLineVerticalHeight = (int) UiUtil.dp2px(30);
        mLineStartY = mLineMarginTop;
    }

    @Override
    public void changeFrequencyBySteps(int stepsCount) {
        mCurrentChannel = formatChannelOfFloat(mCurrentChannel + stepsCount * mChannelScale);
        if (mCurrentChannel >= mChannelMax) {
            mCurrentChannel = mChannelMax;
        }
        if (mCurrentChannel <= mChannelMin) {
            mCurrentChannel = mChannelMin;
        }
        invalidate();
    }

    @Override
    public void setCurrentChannel(float channel) {
        if (channel < mChannelMin) {
            channel = mChannelMin;
        }
        if (channel > mChannelMax) {
            channel = mChannelMax;
        }

        mCurrentChannel = formatChannelOfFloat(channel);
        Log.d(TAG, String.format("before %f, after %f", channel, mCurrentChannel));

        float centerX = getWidth() / 2.0f;

        mFirstLineX =
                centerX - getTotalWidth() * (channel - mChannelMin) / (mChannelMax - mChannelMin);
        invalidate();
    }

    @Override
    public float getCurrentChannel() {
        return mCurrentChannel;
    }

    @Override
    public Range<Float> getChannelRange() {
        return new Range<>(mChannelMin, mChannelMax);
    }

    @Override
    public void setChannelChangedListener(OnChannelChangeListener listener) {
        mOnChannelChangeListener = listener;
    }

    /**
     * @param channel 传入的刻度值
     * @return 处理后只保留一位小数的刻度值
     */
    private float formatChannelOfFloat(float channel) {
        return (float) (Math.round(channel * 10)) / 10;
    }

    /**
     * 重写 onTouchEvent() 方法，根据滑动距离算出频道值的增减，并通过不断重新绘制来更新刻度尺
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                actionX = event.getX();
                break;
            case ACTION_MOVE:
                // 滑动距离
                float movingDistance = event.getX() - actionX;
                float channelIncrement =
                        movingDistance / getTotalWidth() * (mChannelMax - mChannelMin);
                mCurrentChannel -= channelIncrement;
                if (mCurrentChannel > mChannelMax) {
                    mCurrentChannel = mChannelMax;
                }
                if (mCurrentChannel < mChannelMin) {
                    mCurrentChannel = mChannelMin;
                }
                // 保存最新的触摸坐标，用于下一次移动的比较
                actionX = event.getX();
                invalidate();
                break;
            case ACTION_UP:
                // 使指示线只停在刻度线上，目前对 FM 有效
                setCurrentChannel(mCurrentChannel);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 重写 onDraw 方法来绘制刻度板和指针
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mSelectorX = getWidth() / 2.0f;

        mFirstLineX =
                mSelectorX - getTotalWidth() * (mCurrentChannel - mChannelMin) / (mChannelMax - mChannelMin);
        drawLines(canvas, mFirstLineX, mRemainderAtLongLine, mRemainderAtMiddleLine);

        canvas.drawLine(mSelectorX, 0, mSelectorX, getHeight(), mIndicatorLinePaint);

        mOnChannelChangeListener.onChannelChanged(mCurrentChannel);
    }

    /**
     * 该方法用于绘制刻度尺的刻度线
     * 由于刻度尺的理论宽度一般会大于控件的宽度（控件的宽度一般在 xml 布局文件中指定）
     * 为了节省资源，不绘制超出控件部分，即不可见的刻度线
     *
     * @param canvas     画布
     * @param firstLineX 刻度值理论上第一根刻度线的 X 轴坐标
     */
    private void drawLines(Canvas canvas, float firstLineX, int remainderAtLongLine,
                           int remainderAtMiddleLine) {

        float totalWidth = getTotalWidth();
        // 刻度尺左边界控制
        if (totalWidth > getWidth() && firstLineX > mSelectorX) {
            firstLineX = mSelectorX;
        }
        // 刻度尺右边界控制
        if (totalWidth > getWidth() && (firstLineX + totalWidth) < mSelectorX) {
            firstLineX = mSelectorX - totalWidth;
        }

        int index = 0;
        float lineX = firstLineX;

        while (index <= getLinesCount()) {
            // 只画可见的刻度线
            if (lineX > 0 || lineX < getWidth()) {
                if (index % 10 == remainderAtLongLine) {
                    canvas.drawLine(lineX, mLineStartY, lineX, mLineStartY + mLineVerticalHeight,
                            setPaintAlpha(mBoldLinePaint, lineX, 0, getWidth()));

                    mThinLinePaint.setTextSize(UiUtil.dp2px(9));
                    @SuppressLint("DefaultLocale")
                    String channelText = String.format("%.0f", mChannelMin + mChannelScale * index);
                    int textHeight = getTextSize(channelText, mThinLinePaint).height();
                    canvas.drawText(channelText, lineX + UiUtil.dp2px(3),
                            mLineStartY + mLineVerticalHeight - textHeight / 2.0f,
                            setPaintAlpha(mThinLinePaint, lineX, 0, getWidth()));
                } else if (index % 10 == remainderAtMiddleLine) {
                    canvas.drawLine(lineX, mLineStartY, lineX,
                            mLineStartY + mLineVerticalHeight * 0.67f,
                            setPaintAlpha(mThinLinePaint, lineX, 0, getWidth()));
                } else {
                    canvas.drawLine(lineX, mLineStartY, lineX,
                            mLineStartY + mLineVerticalHeight * 0.33f,
                            setPaintAlpha(mThinLinePaint, lineX, 0, getWidth()));
                }
            }
            lineX += mLineHorizontalSpace;
            index++;
        }
    }

    /**
     * 刻度线和刻度值的显示从中间往两侧逐渐变淡
     * 此方法用于改变画笔的透明度
     *
     * @param paint      画笔
     * @param x          X 轴坐标
     * @param leftBound  刻度尺可见部分的最小 X 坐标
     * @param rightBound 刻度尺可见部分的最大 X 坐标
     * @return Alpha 通道值，可以用于控制透明度
     */
    private Paint setPaintAlpha(Paint paint, float x, int leftBound, int rightBound) {
        float distance = Math.min(x - leftBound, rightBound - x);
        // 最大通道值，完全不透明
        int alpha = 255;
        // 刻度尺两边的通道值最小为 30
        int edgeAlpha = 30;
        // 仅对刻度尺两侧边缘占刻度尺十分之一长度的刻度线和文字做渐隐处理
        int fadingWidth = getWidth() / 10;
        if (distance <= fadingWidth) {
            // 离边缘越近通道值越小，透明度越大，最小通道值不小于 30
            alpha -= (alpha - edgeAlpha) * (fadingWidth - distance) / (fadingWidth);
        }
        paint.setAlpha(alpha);
        return paint;
    }

    /**
     * 获取刻度值文字的宽高
     *
     * @param str       刻度值字符串
     * @param textPaint 画刻度值的画笔
     * @return 文字大小
     */
    private Rect getTextSize(String str, Paint textPaint) {
        if (TextUtils.isEmpty(str)) {
            return new Rect();
        }
        Rect bounds = new Rect();
        textPaint.getTextBounds(str, 0, str.length(), bounds);
        return bounds;
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
     * 算出整个刻度尺的理论宽度
     *
     * @return 刻度尺的理论宽度
     */
    private float getTotalWidth() {
        return getLinesCount() * mLineHorizontalSpace;
    }

    /**
     * 监听刻度尺滑动的回调，用于更新刻度值的显示
     */
    public interface OnChannelChangeListener {
        /**
         * 滑动刻度尺改变频道的回调，用于更新显示频道值和收藏列表
         *
         * @param newChannel 新频道值
         */
        void onChannelChanged(float newChannel);
    }
}
