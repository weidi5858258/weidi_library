package com.weidi.application;

import android.app.Application;
import android.content.Context;
import android.os.Looper;

import com.weidi.dbutil.SimpleDao;
import com.weidi.fragment.FragOperManager;
import com.weidi.log.MLog;
import com.weidi.eventbus.EventBusUtils;
import com.weidi.handler.HandlerUtils;
import com.weidi.utils.MyToast;
import com.weidi.wifi.WifiUtils;

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
        MLog.i(TAG, "onCreate()");

        // 数据库
        SimpleDao.setContext(this);
        // 先调用一下,把对象给创建好
        SimpleDao.getInstance();

        MyToast.setContext(this);
        MyToast.getInstance();

        HandlerUtils.init(Looper.getMainLooper());
        EventBusUtils.init();

        FragOperManager.getInstance();

        MLog.init();

        WifiUtils.init(this);

        mContext = getApplicationContext();
    }
}
