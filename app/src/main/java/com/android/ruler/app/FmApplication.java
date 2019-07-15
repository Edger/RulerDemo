package com.android.ruler.app;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class FmApplication extends Application {

    private static final String TAG = "FmApplication";

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private static FmApplication fmApplication;

    public FmApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fmApplication = this;
        context = getApplicationContext();
    }

    public static FmApplication getInstance() {
        return fmApplication;
    }

    public static Context getContext() {
        return context;
    }

}
