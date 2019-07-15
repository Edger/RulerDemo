package com.android.ruler.util;

import android.content.res.Resources;

import com.android.ruler.app.FmApplication;

public class UiUtil {

    public static float dp2px(int dp) {
        return dp * (FmApplication.getInstance().getResources().getDisplayMetrics().density) + 0.5f;
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

}
