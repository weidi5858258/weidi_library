package com.weidi.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.weidi.eventbus.EventBus;

/**
 * Created by root on 17-4-11.
 */

public abstract class BaseService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public abstract Object onEvent(int what, Object object);

}
