package com.weidi.application;

import android.app.Application;
import android.content.Context;
import android.os.Looper;

import com.weidi.dbutil.SimpleDao;
import com.weidi.log.Log;
import com.weidi.utils.EventBusUtils;
import com.weidi.utils.HandlerUtils;
import com.weidi.utils.MyToast;

/**
 * Created by root on 17-4-15.
 */

public class WeidiApplication extends Application {

    private static final String TAG = "WeidiApplication";
    private volatile static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");

        // 数据库
        SimpleDao.setContext(this);
        // 先调用一下,把对象给创建好
        SimpleDao.getInstance();

        MyToast.setContext(this);
        MyToast.getInstance();

        HandlerUtils.init(Looper.getMainLooper());
        EventBusUtils.init();

        Log.init();

        mContext = getApplicationContext();
    }
}
