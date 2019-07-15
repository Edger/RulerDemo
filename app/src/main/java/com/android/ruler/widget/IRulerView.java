package com.android.ruler.widget;

import android.util.Range;

/**
 * 刻度尺接口，用于定义必要的方法
 *
 * @author Edger Lee
 */
public interface IRulerView {

    /**
     * 改变频道值，整型参数乘以分度值就是频道的增减量
     *
     * @param stepsCount 该参数乘以分度值就是频道的增减量
     */
    void changeFrequencyBySteps(int stepsCount);

    /**
     * 设置频道值
     *
     * @param channel 需要设置的频道值
     */
    void setCurrentChannel(float channel);

    /**
     * 获取当前刻度值
     *
     * @return 当前刻度值
     */
    float getCurrentChannel();

    /**
     * 获取当前频道值的范围
     *
     * @return 频道值的范围，FM/AM 的范围不同
     */
    Range<Float> getChannelRange();

    /**
     * 用于设置刻度尺滑动的监听
     *
     * @param listener 刻度尺滑动的监听回调
     */
    void setChannelChangedListener(RulerView2.OnChannelChangeListener listener);
}
