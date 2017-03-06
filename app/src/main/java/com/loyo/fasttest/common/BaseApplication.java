package com.loyo.fasttest.common;

import android.app.Application;

import org.xutils.x;

/**
 * Created by jie on 17/2/17.
 */

public class BaseApplication extends Application {
    private static BaseApplication mInstance;
    public static BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        x.Ext.init(this);//初始化xutils的框架
    }
}
