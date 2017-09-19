package com.weidi.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
